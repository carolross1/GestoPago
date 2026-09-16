package com.proyecto.servicios.config;

import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.ExternalServiceAuthException;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * IMPORTANTE: a proposito NO lleva @Configuration (mismo motivo que
 * OpenApi.java): si la llevara, el component-scan del proyecto la
 * registraria como bean global y este interceptor se agregaria a TODAS
 * las llamadas Feign, incluido GestoPagoAuthClient. Al dejarla como
 * clase plana, solo Feign la usa (via el atributo "configuration" del
 * @FeignClient), de forma aislada.
 *
 * Al ser un contexto hijo de Feign, SI puede inyectar beans del
 * contexto principal de Spring (como GestoPagoTokenService).
 */
public class GestoPagoServiceFeignConfig {

    @Value("${gestopago.auth.id-distribuidor}")
    private Integer idDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo}")
    private String codigoDispositivo;

    @Value("${gestopago.service.api-key:}")
    private String apiKey;

    @Bean
    public RequestInterceptor gestoPagoAuthInterceptor(GestoPagoTokenService gestoPagoTokenService) {
        return requestTemplate -> {
            String token = gestoPagoTokenService
                    .obtenerTokenActivo(idDistribuidor, codigoDispositivo)
                    .map(GestoPagoToken::getToken)
                    .orElseThrow(() -> new ExternalServiceAuthException(
                            "GestoPago",
                            "No hay un token activo disponible para el distribuidor configurado"));

            requestTemplate.header("Authorization", "Bearer " + token);
            if (apiKey != null && !apiKey.isBlank()) {
                requestTemplate.header("X-API-Key", apiKey);
            }
        };
    }
}
