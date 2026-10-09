package com.proyecto.servicios.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Igual que GestoPagoServiceFeignConfig: a proposito NO lleva
 * @Configuration, para que este interceptor solo se aplique a
 * RestCountriesClient y no a todas las llamadas Feign del proyecto.
 */
public class RestCountriesFeignConfig {

    @Value("${restcountries.api-key:}")
    private String apiKey;

    @Bean
    public RequestInterceptor restCountriesAuthInterceptor() {
        return requestTemplate -> {
            if (apiKey != null && !apiKey.isBlank()) {
                requestTemplate.header("Authorization", "Bearer " + apiKey);
            }
        };
    }
}
