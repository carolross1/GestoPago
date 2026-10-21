package com.proyecto.servicios.service.cliente.Impl;

import com.proyecto.servicios.entity.cliente.Cliente;
import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.entity.cliente.Domicilio;
import com.proyecto.servicios.exception.cliente.ClienteInactivoException;
import com.proyecto.servicios.exception.cliente.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.cliente.CorreoDuplicadoException;
import com.proyecto.servicios.exception.cliente.CurpDuplicadaException;
import com.proyecto.servicios.exception.cliente.RfcDuplicadoException;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClienteFiltroRequest;
import com.proyecto.servicios.model.cliente.ClienteRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.model.cliente.DomicilioRequest;
import com.proyecto.servicios.model.cliente.DomicilioResponse;
import com.proyecto.servicios.model.comun.PaginaResponse;
import com.proyecto.servicios.repositorys.cliente.ClienteSpecifications;
import com.proyecto.servicios.repositorys.cliente.ClienteRepository;
import com.proyecto.servicios.repositorys.cliente.CuentaRepository;
import com.proyecto.servicios.repositorys.cliente.DomicilioRepository;
import com.proyecto.servicios.service.cliente.ClienteService;
import com.proyecto.servicios.service.catalogo.NacionalidadService;
import com.proyecto.servicios.service.cliente.CuentaService;
import com.proyecto.servicios.util.CorreoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Slf4j
@Service
public class ClienteServiceImpl implements ClienteService {

    private static final int EDAD_MINIMA = 18;

    private final ClienteRepository clienteRepository;
    private final DomicilioRepository domicilioRepository;
    private final CuentaRepository cuentaRepository;
    private final CuentaService cuentaService;
    private final NacionalidadService nacionalidadService;

    public ClienteServiceImpl(ClienteRepository clienteRepository,
                               DomicilioRepository domicilioRepository,
                               CuentaRepository cuentaRepository,
                               CuentaService cuentaService,
                               NacionalidadService nacionalidadService) {
        this.clienteRepository = clienteRepository;
        this.domicilioRepository = domicilioRepository;
        this.cuentaRepository = cuentaRepository;
        this.cuentaService = cuentaService;
        this.nacionalidadService = nacionalidadService;
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        validarMayoriaDeEdad(request.getFechaNacimiento());
        // La nacionalidad debe existir en el catalogo (tabla catalogo_nacionalidades)
        nacionalidadService.validar(request.getNacionalidad());
        request.setCorreo(CorreoUtil.normalizar(request.getCorreo()));

        if (clienteRepository.existsByCurp(request.getCurp())) {
            throw new CurpDuplicadaException(request.getCurp());
        }
        if (clienteRepository.existsByRfc(request.getRfc())) {
            throw new RfcDuplicadoException(request.getRfc());
        }
        if (clienteRepository.existsByCorreo(request.getCorreo())) {
            throw new CorreoDuplicadoException(request.getCorreo());
        }

        Cliente cliente = new Cliente();
        copiarCamposBasicos(request, cliente);
        cliente = clienteRepository.save(cliente);

        Domicilio domicilio = mapearDomicilio(request.getDomicilio(), cliente.getId());
        domicilioRepository.save(domicilio);

        Cuenta cuenta = cuentaService.crearCuentaParaCliente(cliente.getId());

        log.info("Cliente registrado id={} con cuenta {}", cliente.getId(), cuenta.getNumeroCuenta());
        return construirResponse(cliente, domicilio, cuenta.getNumeroCuenta());
    }

    /**
     * Actualizacion parcial sin @Transactional: todas las validaciones
     * (existencia, cliente activo, correo duplicado, nacionalidad) se hacen
     * con Optional ANTES de escribir, y cada cambio se guarda de forma
     * explicita con save() del repositorio.
     */
    @Override
    public ClienteResponse actualizar(ClienteActualizaRequest request) {
        Long id = request.getId();

        // 1. El cliente debe existir: findById regresa Optional -> si esta vacio, 404
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontro el cliente con id " + id));

        // 2. Regla de negocio: un cliente dado de baja no se modifica -> 409
        Optional.of(cliente)
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new ClienteInactivoException(id, "modificar"));

        // 3. Correo: solo si viene y es distinto al actual; no puede ser de otro cliente -> 409
        Optional.ofNullable(request.getCorreo())
                .map(CorreoUtil::normalizar)
                .filter(correo -> !correo.equals(cliente.getCorreo()))
                .ifPresent(correo -> {
                    if (clienteRepository.existsByCorreo(correo)) {
                        throw new CorreoDuplicadoException(correo);
                    }
                    cliente.setCorreo(correo);
                });

        // 4. Cada campo se aplica SOLO si viene en la solicitud (Optional vacio = no se modifica)
        Optional.ofNullable(request.getNombre()).ifPresent(cliente::setNombre);
        Optional.ofNullable(request.getSegundoNombre()).ifPresent(cliente::setSegundoNombre);
        Optional.ofNullable(request.getApellidoPaterno()).ifPresent(cliente::setApellidoPaterno);
        Optional.ofNullable(request.getApellidoMaterno()).ifPresent(cliente::setApellidoMaterno);
        Optional.ofNullable(request.getSexo()).ifPresent(cliente::setSexo);
        Optional.ofNullable(request.getEstadoCivil()).ifPresent(cliente::setEstadoCivil);
        Optional.ofNullable(request.getOcupacion()).ifPresent(cliente::setOcupacion);
        Optional.ofNullable(request.getEmpresa()).ifPresent(cliente::setEmpresa);
        Optional.ofNullable(request.getNacionalidad())
                .map(codigo -> nacionalidadService.validar(codigo).getCodigo())
                .ifPresent(cliente::setNacionalidad);
        Optional.ofNullable(request.getTelefonoMovil()).map(this::aTexto).ifPresent(cliente::setTelefonoMovil);
        Optional.ofNullable(request.getTelefonoAlternativo()).map(this::aTexto).ifPresent(cliente::setTelefonoAlternativo);
        Optional.ofNullable(request.getIngresoMensual()).map(this::dosDecimales).ifPresent(cliente::setIngresoMensual);

        clienteRepository.save(cliente);

        // 5. Domicilio: si viene, se actualiza el existente o se crea uno; si no viene, se conserva el actual
        Domicilio domicilio = Optional.ofNullable(request.getDomicilio())
                .map(nuevo -> domicilioRepository.findByClienteId(id)
                        .map(existente -> {
                            actualizarDomicilio(existente, nuevo);
                            return existente;
                        })
                        .orElseGet(() -> mapearDomicilio(nuevo, id)))
                .map(domicilioRepository::save)
                .orElseGet(() -> domicilioRepository.findByClienteId(id).orElse(null));

        String numeroCuenta = cuentaRepository.findByClienteId(id)
                .map(Cuenta::getNumeroCuenta)
                .orElse(null);

        return construirResponse(cliente, domicilio, numeroCuenta);
    }


    /**
     * Busqueda dinamica: un solo metodo para cualquier combinacion de
     * filtros (ver ClienteSpecifications). Resultado paginado y ordenado por id.
     */
    @Override
    public PaginaResponse<ClienteResponse> buscar(ClienteFiltroRequest filtro) {
        PageRequest pagina = PageRequest.of(filtro.getPagina(), filtro.getTamano(), Sort.by("id"));
        Page<ClienteResponse> resultado = clienteRepository
                .findAll(ClienteSpecifications.conFiltros(filtro), pagina)
                .map(this::construirResponseCompleto);
        return new PaginaResponse<>(resultado);
    }

    /**
     * Baja logica con Optional: primero se valida que el cliente exista (404)
     * y que siga activo (409); solo entonces se escribe.
     */
    @Override
    public ClienteResponse darDeBaja(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontro el cliente con id " + id));

        Optional.of(cliente)
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new ClienteInactivoException(id, "dar de baja nuevamente"));

        cliente.setActivo(false);
        clienteRepository.save(cliente);
        // Regla de negocio: solo clientes activos pueden tener cuentas activas
        cuentaService.desactivarCuentaDeCliente(id);
        log.info("Cliente id={} dado de baja (baja logica)", id);
        return construirResponseCompleto(cliente);
    }

    private void validarMayoriaDeEdad(LocalDate fechaNacimiento) {
        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < EDAD_MINIMA) {
            throw new ValidacionNegocioException("El cliente debe ser mayor de edad (18 anios o mas)");
        }
    }

    /*
     * Conversiones numero <-> texto. En la API telefonos, numero exterior/
     * interior y CP son numericos, pero en la base se guardan como texto
     * cifrado (el cifrado AES trabaja sobre texto).
     */
    private String aTexto(Number valor) {
        return valor == null ? null : String.valueOf(valor);
    }

    private Long aLong(String valor) {
        return (valor == null || valor.isBlank()) ? null : Long.valueOf(valor.trim());
    }

    private Integer aInteger(String valor) {
        return (valor == null || valor.isBlank()) ? null : Integer.valueOf(valor.trim());
    }

    /** Todas las cantidades se manejan con exactamente 2 decimales. */
    private BigDecimal dosDecimales(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }

    private void copiarCamposBasicos(ClienteRequest request, Cliente cliente) {
        cliente.setNombre(request.getNombre());
        cliente.setSegundoNombre(request.getSegundoNombre());
        cliente.setApellidoPaterno(request.getApellidoPaterno());
        cliente.setApellidoMaterno(request.getApellidoMaterno());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setCurp(request.getCurp());
        cliente.setRfc(request.getRfc());
        cliente.setSexo(request.getSexo());
        cliente.setNacionalidad(request.getNacionalidad());
        cliente.setEstadoCivil(request.getEstadoCivil());
        cliente.setCorreo(request.getCorreo());
        cliente.setTelefonoMovil(aTexto(request.getTelefonoMovil()));
        cliente.setTelefonoAlternativo(aTexto(request.getTelefonoAlternativo()));
        cliente.setOcupacion(request.getOcupacion());
        cliente.setEmpresa(request.getEmpresa());
        cliente.setIngresoMensual(dosDecimales(request.getIngresoMensual()));
        cliente.setActivo(true);
    }

    private Domicilio mapearDomicilio(DomicilioRequest request, Long clienteId) {
        Domicilio domicilio = new Domicilio();
        domicilio.setClienteId(clienteId);
        actualizarDomicilio(domicilio, request);
        return domicilio;
    }

    private void actualizarDomicilio(Domicilio domicilio, DomicilioRequest request) {
        domicilio.setCalle(request.getCalle());
        domicilio.setNumeroExterior(aTexto(request.getNumeroExterior()));
        domicilio.setNumeroInterior(aTexto(request.getNumeroInterior()));
        domicilio.setColonia(request.getColonia());
        domicilio.setMunicipio(request.getMunicipio());
        domicilio.setEstado(request.getEstado());
        // El CP se guarda siempre con 5 digitos: 6000 -> "06000"
        domicilio.setCodigoPostal(String.format("%05d", request.getCodigoPostal()));
        domicilio.setPais(request.getPais());
    }

    private ClienteResponse construirResponseCompleto(Cliente cliente) {
        Domicilio domicilio = domicilioRepository.findByClienteId(cliente.getId()).orElse(null);
        String numeroCuenta = cuentaRepository.findByClienteId(cliente.getId())
                .map(Cuenta::getNumeroCuenta)
                .orElse(null);
        return construirResponse(cliente, domicilio, numeroCuenta);
    }

    private ClienteResponse construirResponse(Cliente cliente, Domicilio domicilio, String numeroCuenta) {
        ClienteResponse response = new ClienteResponse();
        response.setId(cliente.getId());
        response.setNombre(cliente.getNombre());
        response.setSegundoNombre(cliente.getSegundoNombre());
        response.setApellidoPaterno(cliente.getApellidoPaterno());
        response.setApellidoMaterno(cliente.getApellidoMaterno());
        response.setFechaNacimiento(cliente.getFechaNacimiento());
        response.setCurp(cliente.getCurp());
        response.setRfc(cliente.getRfc());
        response.setSexo(cliente.getSexo());
        response.setNacionalidad(cliente.getNacionalidad());
        nacionalidadService.buscar(cliente.getNacionalidad())
                .ifPresent(n -> response.setNacionalidadNombre(n.getNombre()));
        response.setEstadoCivil(cliente.getEstadoCivil());
        response.setCorreo(cliente.getCorreo());
        response.setTelefonoMovil(aLong(cliente.getTelefonoMovil()));
        response.setTelefonoAlternativo(aLong(cliente.getTelefonoAlternativo()));
        response.setOcupacion(cliente.getOcupacion());
        response.setEmpresa(cliente.getEmpresa());
        response.setIngresoMensual(dosDecimales(cliente.getIngresoMensual()));
        response.setActivo(cliente.getActivo());
        if (cliente.getFechaRegistro() != null) {
            // Fecha y hora se regresan en apartados separados
            response.setFechaRegistro(cliente.getFechaRegistro().toLocalDate());
            response.setHoraRegistro(cliente.getFechaRegistro().toLocalTime().withNano(0));
        }
        response.setNumeroCuenta(numeroCuenta);

        if (domicilio != null) {
            DomicilioResponse domicilioDto = new DomicilioResponse();
            domicilioDto.setCalle(domicilio.getCalle());
            domicilioDto.setNumeroExterior(aInteger(domicilio.getNumeroExterior()));
            domicilioDto.setNumeroInterior(aInteger(domicilio.getNumeroInterior()));
            domicilioDto.setColonia(domicilio.getColonia());
            domicilioDto.setMunicipio(domicilio.getMunicipio());
            domicilioDto.setEstado(domicilio.getEstado());
            domicilioDto.setCodigoPostal(aInteger(domicilio.getCodigoPostal()));
            domicilioDto.setPais(domicilio.getPais());
            response.setDomicilio(domicilioDto);
        }

        return response;
    }
}
