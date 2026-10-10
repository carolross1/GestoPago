package com.proyecto.servicios.service.catalogo.Impl;

import com.proyecto.servicios.client.RestCountriesClient;
import com.proyecto.servicios.entity.catalogo.Nacionalidad;
import com.proyecto.servicios.exception.ExternalServiceException;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.model.catalogo.NacionalidadResponse;
import com.proyecto.servicios.model.catalogo.SincronizacionResponse;
import com.proyecto.servicios.model.restcountries.RestCountriesResponse;
import com.proyecto.servicios.repositorys.catalogo.NacionalidadRepository;
import com.proyecto.servicios.service.catalogo.NacionalidadService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Catalogo de nacionalidades.
 *
 * Flujo:
 *  1. sincronizar() pide los paises a restcountries.com en paginas de 100
 *     (maximo del plan gratuito) y los guarda/actualiza en la tabla
 *     catalogo_nacionalidades. Una sincronizacion completa usa ~3 peticiones.
 *  2. El alta/actualizacion de clientes SOLO consulta la base de datos
 *     (validar), nunca la API, asi que si la API se cae los clientes se
 *     siguen registrando con el catalogo que ya esta guardado.
 */
@Slf4j
@Service
public class NacionalidadServiceImpl implements NacionalidadService {

    private static final String SERVICIO = "RestCountries";
    private static final int TAMANO_PAGINA = 100;
    private static final int MAX_PAGINAS = 10;
    private static final String CAMPOS = "codes.alpha_2,codes.alpha_3,names.common,names.translations.spa";

    private final RestCountriesClient restCountriesClient;
    private final NacionalidadRepository nacionalidadRepository;

    public NacionalidadServiceImpl(RestCountriesClient restCountriesClient,
                                   NacionalidadRepository nacionalidadRepository) {
        this.restCountriesClient = restCountriesClient;
        this.nacionalidadRepository = nacionalidadRepository;
    }

    @Override
    @Transactional
    public SincronizacionResponse sincronizar() {
        List<RestCountriesResponse.Pais> paises = descargarPaises();

        Map<String, Nacionalidad> existentes = nacionalidadRepository.findAll().stream()
                .collect(Collectors.toMap(Nacionalidad::getCodigo, n -> n));

        int nuevos = 0;
        int actualizados = 0;
        for (RestCountriesResponse.Pais pais : paises) {
            if (pais.getCodes() == null || !esCodigoValido(pais.getCodes().getAlpha3())) {
                continue;
            }
            String codigo = pais.getCodes().getAlpha3().toUpperCase(Locale.ROOT);
            String nombreIngles = pais.getNames() != null ? pais.getNames().getCommon() : null;
            String nombre = nombreEnEspanol(pais.getNames(), nombreIngles, codigo);

            Nacionalidad nacionalidad = existentes.get(codigo);
            if (nacionalidad == null) {
                nacionalidad = new Nacionalidad();
                nacionalidad.setCodigo(codigo);
                nuevos++;
            } else {
                actualizados++;
            }
            nacionalidad.setCodigoIso2(pais.getCodes().getAlpha2());
            nacionalidad.setNombre(recortar(nombre));
            nacionalidad.setNombreIngles(recortar(nombreIngles));
            nacionalidad.setActivo(true);
            nacionalidadRepository.save(nacionalidad);
        }

        log.info("Catalogo de nacionalidades sincronizado: {} recibidos, {} nuevos, {} actualizados",
                paises.size(), nuevos, actualizados);
        return new SincronizacionResponse(paises.size(), nuevos, actualizados);
    }

    @Override
    public List<NacionalidadResponse> listar() {
        return nacionalidadRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(n -> new NacionalidadResponse(n.getCodigo(), n.getCodigoIso2(), n.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    public Nacionalidad validar(String codigo) {
        if (nacionalidadRepository.count() == 0) {
            throw new ValidacionNegocioException("El catalogo de nacionalidades esta vacio. "
                    + "Configura restcountries.api-key y ejecuta POST /catalogos/nacionalidades/sincronizar");
        }
        return buscar(codigo).orElseThrow(() -> new ValidacionNegocioException(
                "La nacionalidad '" + codigo + "' no existe en el catalogo. "
                        + "Consulta los codigos validos en GET /catalogos/nacionalidades (ej. MEX)"));
    }

    @Override
    public Optional<Nacionalidad> buscar(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return nacionalidadRepository.findByCodigoAndActivoTrue(codigo.trim().toUpperCase(Locale.ROOT));
    }

    /** Recorre todas las paginas de la API (limit/offset) hasta completar meta.total. */
    private List<RestCountriesResponse.Pais> descargarPaises() {
        List<RestCountriesResponse.Pais> todos = new java.util.ArrayList<>();
        int offset = 0;
        for (int pagina = 0; pagina < MAX_PAGINAS; pagina++) {
            RestCountriesResponse respuesta;
            try {
                respuesta = restCountriesClient.listarPaises(TAMANO_PAGINA, offset, CAMPOS);
            } catch (FeignException.Unauthorized | FeignException.Forbidden e) {
                throw new ExternalServiceException(SERVICIO,
                        "API key de restcountries invalida, vencida o sin cuota (HTTP " + e.status() + ")", e);
            } catch (FeignException e) {
                throw new ExternalServiceException(SERVICIO,
                        "Error al consultar restcountries (HTTP " + e.status() + ")", e);
            }

            if (respuesta == null || respuesta.getData() == null || respuesta.getData().getObjects() == null) {
                break;
            }
            List<RestCountriesResponse.Pais> objetos = respuesta.getData().getObjects();
            todos.addAll(objetos);

            Integer total = respuesta.getData().getMeta() != null ? respuesta.getData().getMeta().getTotal() : null;
            offset += objetos.size();
            if (objetos.isEmpty() || total == null || offset >= total) {
                break;
            }
        }
        if (todos.isEmpty()) {
            throw new ExternalServiceException(SERVICIO, "restcountries no regreso paises");
        }
        return todos;
    }

    private String nombreEnEspanol(RestCountriesResponse.Nombres nombres, String respaldo, String codigo) {
        if (nombres != null && nombres.getTranslations() != null) {
            RestCountriesResponse.Traduccion spa = nombres.getTranslations().get("spa");
            if (spa != null && spa.getCommon() != null && !spa.getCommon().isBlank()) {
                return spa.getCommon();
            }
        }
        return respaldo != null && !respaldo.isBlank() ? respaldo : codigo;
    }

    private boolean esCodigoValido(String codigo) {
        return codigo != null && codigo.matches("^[A-Za-z]{3}$");
    }

    private String recortar(String texto) {
        return texto == null || texto.length() <= 150 ? texto : texto.substring(0, 150);
    }
}
