package com.proyecto.servicios.service.transaccion;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Envío de correos transaccionales vía HTTPS (puerto 443).
 * Permite entregar correos reales desde plataformas gratuitas (como Render Free Tier)
 * donde los puertos SMTP salientes (25, 465, 587) están bloqueados.
 *
 * Soporta dos canales HTTPS sin depender de puertos SMTP:
 * 1) Webhook HTTPS propio (ej. Google Apps Script ligado a la cuenta de Gmail del proyecto).
 * 2) API REST de Brevo (https://api.brevo.com/v3/smtp/email).
 */
@Slf4j
final class EnviadorCorreoHttp {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private EnviadorCorreoHttp() {
    }

    static boolean enviarPorWebhook(String webhookUrl,
                                    String webhookSecret,
                                    String correoDestino,
                                    String asunto,
                                    String html) {
        return enviarPorWebhook(HTTP_CLIENT, webhookUrl, webhookSecret, correoDestino, asunto, html);
    }

    static boolean enviarPorWebhook(HttpClient client,
                                    String webhookUrl,
                                    String webhookSecret,
                                    String correoDestino,
                                    String asunto,
                                    String html) {
        if (webhookUrl == null || webhookUrl.isBlank()
                || correoDestino == null || correoDestino.isBlank()) {
            return false;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("to", correoDestino.trim());
            payload.put("subject", asunto);
            payload.put("html", html);
            if (webhookSecret != null && !webhookSecret.isBlank()) {
                payload.put("secret", webhookSecret.trim());
            }

            String jsonBody = OBJECT_MAPPER.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl.trim()))
                    .timeout(Duration.ofSeconds(12))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String body = response.body() != null ? response.body() : "";
                if (body.contains("\"ok\":false") || body.contains("\"error\"")) {
                    log.warn("Webhook de correo respondió error lógico para {}: {}", correoDestino, body);
                    return false;
                }
                log.info("Correo enviado exitosamente vía HTTPS (Webhook Gmail) a {}", correoDestino);
                return true;
            }
            log.warn("Webhook de correo respondió HTTP {} al enviar a {}: {}",
                    response.statusCode(), correoDestino, response.body());
            return false;
        } catch (Exception e) {
            log.warn("No se pudo enviar correo vía HTTPS (Webhook) a {}: {}", correoDestino, e.getMessage());
            return false;
        }
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
