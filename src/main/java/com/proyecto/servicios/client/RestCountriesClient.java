package com.proyecto.servicios.client;

import com.proyecto.servicios.config.RestCountriesFeignConfig;
import com.proyecto.servicios.model.restcountries.RestCountriesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * API publica de paises: https://restcountries.com (version v5).
 * Requiere API key gratuita (se agrega en RestCountriesFeignConfig).
 */
@FeignClient(name = "restCountries", url = "${restcountries.url}", configuration = RestCountriesFeignConfig.class)
public interface RestCountriesClient {

    @GetMapping("/countries/v5")
    RestCountriesResponse listarPaises(
            @RequestParam("limit") int limit,
            @RequestParam("offset") int offset,
            @RequestParam("response_fields") String campos
    );
}
