package com.proyecto.servicios.model.restcountries;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Respuesta de GET https://api.restcountries.com/countries/v5
 *
 * <pre>
 * { "data": {
 *     "objects": [ { "codes": {"alpha_2":"MX","alpha_3":"MEX"},
 *                    "names": {"common":"Mexico",
 *                              "translations": {"spa": {"common":"Mexico"}}} } ],
 *     "meta": { "total": 250 } } }
 * </pre>
 * Solo se mapean los campos que usa el catalogo; el resto se ignora.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestCountriesResponse {

    private Datos data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Datos {
        private List<Pais> objects;
        private Meta meta;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private Integer total;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pais {
        private Codigos codes;
        private Nombres names;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Codigos {
        @com.fasterxml.jackson.annotation.JsonProperty("alpha_2")
        private String alpha2;

        @com.fasterxml.jackson.annotation.JsonProperty("alpha_3")
        private String alpha3;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nombres {
        private String common;
        /** Llave = codigo de idioma ISO 639-3 (spa, eng, fra...). */
        private Map<String, Traduccion> translations;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Traduccion {
        private String common;
        private String official;
    }
}
