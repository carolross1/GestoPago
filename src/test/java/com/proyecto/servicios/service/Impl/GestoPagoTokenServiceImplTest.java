package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoAuthClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.mapper.GestoPagoTokenMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoAuthResponse;
import com.proyecto.servicios.repositorys.gestopago.GestoPagoTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestoPagoTokenServiceImplTest {

    @Mock
    private GestoPagoAuthClient authClient;

    @Mock
    private GestoPagoTokenRepository tokenRepository;

    @Mock
    private GestoPagoTokenMapper tokenMapper;

    @InjectMocks
    private GestoPagoTokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "idDistribuidor", 83);
        ReflectionTestUtils.setField(tokenService, "codigoDispositivo", "GPS83-TPV-17");
        ReflectionTestUtils.setField(tokenService, "password", "12345678");
    }

    @Test
    @DisplayName("renovarToken crea nuevo token cuando no existe en base de datos")
    void renovarTokenNuevo() {
        GestoPagoAuthResponse authResp = new GestoPagoAuthResponse();
        authResp.setToken("TOKEN-12345");

        when(authClient.authenticate(83, "GPS83-TPV-17", "12345678")).thenReturn(authResp);
        when(tokenRepository.findByIdDistribuidorAndCodigoDispositivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.empty());

        GestoPagoToken entity = new GestoPagoToken();
        when(tokenMapper.toEntity(authResp)).thenReturn(entity);

        tokenService.renovarToken();

        verify(tokenRepository).save(entity);
        assertEquals(83, entity.getIdDistribuidor());
        assertEquals("GPS83-TPV-17", entity.getCodigoDispositivo());
        assertTrue(entity.getActivo());
    }

    @Test
    @DisplayName("renovarToken actualiza token existente")
    void renovarTokenExistente() {
        GestoPagoAuthResponse authResp = new GestoPagoAuthResponse();
        authResp.setToken("NUEVO-TOKEN");

        GestoPagoToken existente = new GestoPagoToken();
        existente.setId(1);

        when(authClient.authenticate(83, "GPS83-TPV-17", "12345678")).thenReturn(authResp);
        when(tokenRepository.findByIdDistribuidorAndCodigoDispositivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(existente));

        tokenService.renovarToken();

        verify(tokenMapper).updateEntity(authResp, existente);
        verify(tokenRepository).save(existente);
    }

    @Test
    @DisplayName("renovarToken maneja respuesta nula sin lanzar excepcion")
    void renovarTokenRespuestaNula() {
        when(authClient.authenticate(any(), any(), any())).thenReturn(null);

        assertDoesNotThrow(() -> tokenService.renovarToken());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("renovarToken captura excepciones del cliente Feign")
    void renovarTokenCapturaError() {
        when(authClient.authenticate(any(), any(), any())).thenThrow(new RuntimeException("Error conexion"));

        assertDoesNotThrow(() -> tokenService.renovarToken());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("obtenerTokenActivo consulta el repositorio")
    void obtenerTokenActivo() {
        GestoPagoToken token = new GestoPagoToken();
        when(tokenRepository.findByIdDistribuidorAndCodigoDispositivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(token));

        Optional<GestoPagoToken> resultado = tokenService.obtenerTokenActivo(83, "GPS83-TPV-17");

        assertTrue(resultado.isPresent());
        assertSame(token, resultado.get());
    }
}
