package com.proyecto.servicios.config;

import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.ExternalServiceAuthException;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GestoPagoServiceFeignConfigTest {

    @Test
    @DisplayName("gestoPagoAuthInterceptor inyecta token Bearer y API Key en los headers")
    void interceptorConToken() {
        GestoPagoServiceFeignConfig config = new GestoPagoServiceFeignConfig();
        ReflectionTestUtils.setField(config, "idDistribuidor", 83);
        ReflectionTestUtils.setField(config, "codigoDispositivo", "GPS83-TPV-17");
        ReflectionTestUtils.setField(config, "apiKey", "SECRET-KEY-123");

        GestoPagoTokenService tokenService = mock(GestoPagoTokenService.class);
        GestoPagoToken token = new GestoPagoToken();
        token.setToken("JWT-TOKEN-XYZ");

        when(tokenService.obtenerTokenActivo(83, "GPS83-TPV-17")).thenReturn(Optional.of(token));

        RequestInterceptor interceptor = config.gestoPagoAuthInterceptor(tokenService);
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().get("Authorization").contains("Bearer JWT-TOKEN-XYZ"));
        assertTrue(template.headers().get("X-API-Key").contains("SECRET-KEY-123"));
    }

    @Test
    @DisplayName("gestoPagoAuthInterceptor lanza ExternalServiceAuthException si no hay token")
    void interceptorSinToken() {
        GestoPagoServiceFeignConfig config = new GestoPagoServiceFeignConfig();
        ReflectionTestUtils.setField(config, "idDistribuidor", 83);
        ReflectionTestUtils.setField(config, "codigoDispositivo", "GPS83-TPV-17");

        GestoPagoTokenService tokenService = mock(GestoPagoTokenService.class);
        when(tokenService.obtenerTokenActivo(83, "GPS83-TPV-17")).thenReturn(Optional.empty());

        RequestInterceptor interceptor = config.gestoPagoAuthInterceptor(tokenService);
        RequestTemplate template = new RequestTemplate();

        assertThrows(ExternalServiceAuthException.class, () -> interceptor.apply(template));
    }
}
