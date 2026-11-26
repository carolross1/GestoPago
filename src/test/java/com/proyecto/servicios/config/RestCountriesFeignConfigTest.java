package com.proyecto.servicios.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RestCountriesFeignConfigTest {

    @Test
    @DisplayName("restCountriesAuthInterceptor inyecta header Authorization si existe api key")
    void interceptorConApiKey() {
        RestCountriesFeignConfig config = new RestCountriesFeignConfig();
        ReflectionTestUtils.setField(config, "apiKey", "test-api-key");

        RequestInterceptor interceptor = config.restCountriesAuthInterceptor();
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertTrue(template.headers().get("Authorization").contains("Bearer test-api-key"));
    }
}
