package com.proyecto.servicios.service.transaccion;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Envío de correos transaccionales vía HTTPS (puerto 443) usando la API REST de Brevo.
 * Permite entregar correos reales desde plataformas gratuitas (como Render Free Tier)
 * donde los puertos SMTP salientes (25, 465, 587) están bloqueados.
 */
@Slf4j
final class EnviadorCorreoHttp {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private EnviadorCorreoHttp() {
    }

    static boolean enviarPorBrevo(String apiKey,
                                  String remitente,
                                  String correoDestino,
                                  String asunto,
                                  String html) {
        return enviarPorBrevo(HTTP_CLIENT, BREVO_API_URL, apiKey, remitente, correoDestino, asunto, html);
    }

    static boolean enviarPorBrevo(HttpClient client,
                                  String endpointUrl,
                                  String apiKey,
                                  String remitente,
                                  String correoDestino,
                                  String asunto,
                                  String html) {
        if (apiKey == null || apiKey.isBlank() || remitente == null || remitente.isBlank()
                || correoDestino == null || correoDestino.isBlank()) {
            return false;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", "GestoPago Banca Digital", "email", remitente.trim()),
                    "to", List.of(Map.of("email", correoDestino.trim())),
                    "subject", asunto,
                    "htmlContent", html
            );

            String jsonBody = OBJECT_MAPPER.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .timeout(Duration.ofSeconds(8))
                    .header("api-key", apiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Correo enviado exitosamente vía HTTPS (Brevo) a {}", correoDestino);
                return true;
            }
            log.warn("Brevo API respondió HTTP {} al enviar correo a {}: {}",
                    response.statusCode(), correoDestino, response.body());
            return false;
        } catch (Exception e) {
            log.warn("No se pudo enviar correo vía HTTPS (Brevo) a {}: {}", correoDestino, e.getMessage());
            return false;
        }
    }
}
