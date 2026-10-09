package com.proyecto.servicios.service.catalogo;

import com.proyecto.servicios.client.RestCountriesClient;
import com.proyecto.servicios.entity.catalogo.Nacionalidad;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.model.catalogo.SincronizacionResponse;
import com.proyecto.servicios.model.restcountries.RestCountriesResponse;
import com.proyecto.servicios.repositorys.catalogo.NacionalidadRepository;
import com.proyecto.servicios.service.catalogo.Impl.NacionalidadServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NacionalidadServiceImplTest {

    @Mock
    private RestCountriesClient restCountriesClient;

    @Mock
    private NacionalidadRepository nacionalidadRepository;

    @InjectMocks
    private NacionalidadServiceImpl nacionalidadService;

    private RestCountriesResponse.Pais pais(String a2, String a3, String ingles, String espanol) {
        RestCountriesResponse.Codigos codigos = new RestCountriesResponse.Codigos();
        codigos.setAlpha2(a2);
        codigos.setAlpha3(a3);
        RestCountriesResponse.Nombres nombres = new RestCountriesResponse.Nombres();
        nombres.setCommon(ingles);
        if (espanol != null) {
            RestCountriesResponse.Traduccion spa = new RestCountriesResponse.Traduccion();
            spa.setCommon(espanol);
            nombres.setTranslations(Map.of("spa", spa));
        }
        RestCountriesResponse.Pais pais = new RestCountriesResponse.Pais();
        pais.setCodes(codigos);
        pais.setNames(nombres);
        return pais;
    }

    private RestCountriesResponse pagina(int total, RestCountriesResponse.Pais... paises) {
        RestCountriesResponse.Meta meta = new RestCountriesResponse.Meta();
        meta.setTotal(total);
        RestCountriesResponse.Datos datos = new RestCountriesResponse.Datos();
        datos.setObjects(List.of(paises));
        datos.setMeta(meta);
        RestCountriesResponse respuesta = new RestCountriesResponse();
        respuesta.setData(datos);
        return respuesta;
    }

    @Test
    void sincronizar_guardaNombreEnEspanolYCodigoAlfa3() {
        when(restCountriesClient.listarPaises(eq(100), eq(0), anyString()))
                .thenReturn(pagina(1, pais("MX", "MEX", "Mexico", "México")));
        when(nacionalidadRepository.findAll()).thenReturn(List.of());

        SincronizacionResponse resultado = nacionalidadService.sincronizar();

        ArgumentCaptor<Nacionalidad> captor = ArgumentCaptor.forClass(Nacionalidad.class);
        verify(nacionalidadRepository).save(captor.capture());
        assertEquals("MEX", captor.getValue().getCodigo());
        assertEquals("México", captor.getValue().getNombre());
        assertEquals(1, resultado.getNuevos());
    }

    @Test
    void sincronizar_recorreTodasLasPaginas() {
        when(restCountriesClient.listarPaises(eq(100), eq(0), anyString()))
                .thenReturn(pagina(2, pais("MX", "MEX", "Mexico", "México")));
        when(restCountriesClient.listarPaises(eq(100), eq(1), anyString()))
                .thenReturn(pagina(2, pais("CA", "CAN", "Canada", null)));
        when(nacionalidadRepository.findAll()).thenReturn(List.of());

        SincronizacionResponse resultado = nacionalidadService.sincronizar();

        assertEquals(2, resultado.getRecibidos());
        verify(nacionalidadRepository, times(2)).save(org.mockito.ArgumentMatchers.any(Nacionalidad.class));
    }

    @Test
    void validar_codigoInexistente_lanzaExcepcion() {
        when(nacionalidadRepository.count()).thenReturn(250L);
        when(nacionalidadRepository.findByCodigoAndActivoTrue("XYZ")).thenReturn(Optional.empty());

        assertThrows(ValidacionNegocioException.class, () -> nacionalidadService.validar("xyz"));
    }

    @Test
    void validar_catalogoVacio_lanzaExcepcion() {
        when(nacionalidadRepository.count()).thenReturn(0L);

        assertThrows(ValidacionNegocioException.class, () -> nacionalidadService.validar("MEX"));
    }
}
