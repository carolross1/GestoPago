package com.proyecto.servicios.service.catalogo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.model.catalogo.CodigoPostalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consulta direcciones de México por Código Postal combinando APIs públicas:
 * - Zippopotam (https://api.zippopotam.us/mx/{cp}) para colonias y estado.
 * - Nominatim OpenStreetMap para el municipio exacto.
 */
@Slf4j
@Service
public class CodigoPostalService {

    private static final String ZIPPOPOTAM_URL = "https://api.zippopotam.us/mx/";
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?country=Mexico&format=json&addressdetails=1&limit=1&postalcode=";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, CodigoPostalResponse> cache = new ConcurrentHashMap<>();

    public CodigoPostalService() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(), new ObjectMapper());
    }

    CodigoPostalService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public CodigoPostalResponse consultarPorCp(String cpRaw) {
        String cp = cpRaw == null ? "" : cpRaw.trim();
        if (!cp.matches("^\\d{5}$") || Integer.parseInt(cp) < 1000) {
            throw new ValidacionNegocioException("El código postal debe tener exactamente 5 dígitos válidos (01000 a 99999)");
        }

        CodigoPostalResponse cached = cache.get(cp);
        if (cached != null) {
            return cached;
        }

        String estado = "";
        String municipio = "";
        Set<String> colonias = new LinkedHashSet<>();

        // 1. Zippopotam: colonias y estado
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ZIPPOPOTAM_URL + cp))
                    .timeout(Duration.ofSeconds(6))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null) {
                JsonNode root = objectMapper.readTree(resp.body());
                JsonNode places = root.path("places");
                if (places.isArray()) {
                    for (JsonNode place : places) {
                        if (estado.isBlank()) {
                            estado = normalizarEstado(place.path("state").asText(""));
                        }
                        String col = limpiarTexto(place.path("place name").asText(""));
                        if (!col.isBlank()) {
                            colonias.add(col);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error consultando Zippopotam para CP {}: {}", cp, e.getMessage());
        }

        // 2. Nominatim OpenStreetMap: municipio exacto (y estado de respaldo)
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(NOMINATIM_URL + cp))
                    .timeout(Duration.ofSeconds(6))
                    .header("User-Agent", "GestoPagoBancaDigital/1.0")
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null) {
                JsonNode arr = objectMapper.readTree(resp.body());
                if (arr.isArray() && !arr.isEmpty()) {
                    JsonNode address = arr.get(0).path("address");
                    municipio = limpiarSoloLetras(primerNoVacio(
                            address.path("county").asText(""),
                            address.path("municipality").asText(""),
                            address.path("city").asText(""),
                            address.path("town").asText(""),
                            address.path("borough").asText("")
                    ));
                    if (estado.isBlank()) {
                        estado = normalizarEstado(address.path("state").asText(""));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error consultando Nominatim para CP {}: {}", cp, e.getMessage());
        }

        if (estado.isBlank() && colonias.isEmpty() && municipio.isBlank()) {
            throw new ValidacionNegocioException("No se encontró información para el código postal " + cp);
        }

        if (municipio.isBlank()) {
            municipio = estado;
        }

        CodigoPostalResponse resultado = CodigoPostalResponse.builder()
                .codigoPostal(cp)
                .pais("Mexico")
                .estado(estado)
                .municipio(municipio)
                .colonias(new ArrayList<>(colonias))
                .build();

        cache.put(cp, resultado);
        return resultado;
    }

    private String normalizarEstado(String raw) {
        String limpio = limpiarSoloLetras(raw);
        if ("Distrito Federal".equalsIgnoreCase(limpio)) {
            return "Ciudad de Mexico";
        }
        return limpio;
    }

    private String primerNoVacio(String... valores) {
        for (String v : valores) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return "";
    }

    private String limpiarSoloLetras(String valor) {
        if (valor == null) return "";
        return valor.replaceAll("[^A-Za-zÁÉÍÓÚÜáéíóúüÑñ ]", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String limpiarTexto(String valor) {
        if (valor == null) return "";
        return valor.replaceAll("[^A-Za-zÁÉÍÓÚÜáéíóúüÑñ0-9 .,-]", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }
}
