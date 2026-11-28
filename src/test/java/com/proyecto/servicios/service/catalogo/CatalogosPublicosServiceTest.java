package com.proyecto.servicios.service.catalogo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.model.catalogo.CatalogoProfesionesResponse;
import com.proyecto.servicios.model.catalogo.CodigoPostalResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogosPublicosServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> respZippo;

    @Mock
    private HttpResponse<String> respNominatim;

    @Test
    @DisplayName("CodigoPostalService rechaza CP con formato invalido")
    void rechazaCpInvalido() {
        CodigoPostalService service = new CodigoPostalService(httpClient, new ObjectMapper());
        assertThrows(ValidacionNegocioException.class, () -> service.consultarPorCp("123"));
        assertThrows(ValidacionNegocioException.class, () -> service.consultarPorCp("00050"));
        assertThrows(ValidacionNegocioException.class, () -> service.consultarPorCp("abcde"));
    }

    @Test
    @DisplayName("CodigoPostalService consulta Zippopotam y Nominatim, normaliza y cachea")
    @SuppressWarnings("unchecked")
    void consultaYCacheaCpValido() throws Exception {
        when(respZippo.statusCode()).thenReturn(200);
        when(respZippo.body()).thenReturn("""
                {"country":"Mexico","post code":"37800","places":[
                  {"place name":"Centro","state":"Guanajuato"},
                  {"place name":"Las Flores","state":"Guanajuato"}
                ]}""");

        when(respNominatim.statusCode()).thenReturn(200);
        when(respNominatim.body()).thenReturn("""
                [{"address":{"county":"Dolores Hidalgo","state":"Guanajuato","country":"México"}}]""");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(respZippo)
                .thenReturn(respNominatim);

        CodigoPostalService service = new CodigoPostalService(httpClient, new ObjectMapper());
        CodigoPostalResponse r1 = service.consultarPorCp("37800");

        assertEquals("37800", r1.getCodigoPostal());
        assertEquals("Mexico", r1.getPais());
        assertEquals("Guanajuato", r1.getEstado());
        assertEquals("Dolores Hidalgo", r1.getMunicipio());
        assertEquals(2, r1.getColonias().size());

        // Segunda llamada debe usar cache
        CodigoPostalResponse r2 = service.consultarPorCp("37800");
        assertSame(r1, r2);
        verify(httpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    @DisplayName("CodigoPostalService lanza ValidacionNegocioException si ninguna API encuentra el CP")
    @SuppressWarnings("unchecked")
    void lanzaExcepcionCuandoCpNoExiste() throws Exception {
        when(respZippo.statusCode()).thenReturn(404);
        when(respNominatim.statusCode()).thenReturn(200);
        when(respNominatim.body()).thenReturn("[]");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(respZippo)
                .thenReturn(respNominatim);

        CodigoPostalService service = new CodigoPostalService(httpClient, new ObjectMapper());
        assertThrows(ValidacionNegocioException.class, () -> service.consultarPorCp("99999"));
    }

    @Test
    @DisplayName("ProfesionService retorna catalogo oficial con tabuladores y profesiones")
    void catalogoProfesionesCompleto() {
        ProfesionService service = new ProfesionService();
        CatalogoProfesionesResponse res = service.obtenerCatalogo();

        assertNotNull(res);
        assertEquals(5, res.getTabuladores().size());
        assertTrue(res.getProfesiones().size() >= 25);
        assertEquals("TAB_1", res.getTabuladores().get(0).getId());
    }
}
