package com.proyecto.servicios.service.transaccion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnviadorCorreoHttpTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @Test
    @DisplayName("Retorna false sin llamar a HTTP si faltan apiKey, remitente o destino")
    void retornaFalseSiFaltanDatos() throws Exception {
        assertFalse(EnviadorCorreoHttp.enviarPorBrevo(httpClient, "https://api.brevo.com/v3/smtp/email", "", "a@b.com", "c@d.com", "Asunto", "<p>Hola</p>"));
        assertFalse(EnviadorCorreoHttp.enviarPorBrevo(httpClient, "https://api.brevo.com/v3/smtp/email", "key", "", "c@d.com", "Asunto", "<p>Hola</p>"));
        assertFalse(EnviadorCorreoHttp.enviarPorBrevo(httpClient, "https://api.brevo.com/v3/smtp/email", "key", "a@b.com", null, "Asunto", "<p>Hola</p>"));
        verifyNoInteractions(httpClient);
    }

    @Test
    @DisplayName("Retorna true cuando Brevo responde HTTP 201")
    @SuppressWarnings("unchecked")
    void retornaTrueCuandoBrevoResponde2xx() throws Exception {
        when(httpResponse.statusCode()).thenReturn(201);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        boolean enviado = EnviadorCorreoHttp.enviarPorBrevo(
                httpClient,
                "https://api.brevo.com/v3/smtp/email",
                "xkeysib-test",
                "remitente@gmail.com",
                "destino@gmail.com",
                "Código 2FA",
                "<html><body>123456</body></html>"
        );

        assertTrue(enviado);
    }

    @Test
    @DisplayName("Retorna false cuando Brevo responde error HTTP o lanza excepción")
    @SuppressWarnings("unchecked")
    void retornaFalseCuandoBrevoFalla() throws Exception {
        when(httpResponse.statusCode()).thenReturn(401);
        when(httpResponse.body()).thenReturn("Unauthorized");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        assertFalse(EnviadorCorreoHttp.enviarPorBrevo(
                httpClient,
                "https://api.brevo.com/v3/smtp/email",
                "xkeysib-bad",
                "remitente@gmail.com",
                "destino@gmail.com",
                "Asunto",
                "<p>Test</p>"
        ));

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Timeout"));

        assertFalse(EnviadorCorreoHttp.enviarPorBrevo(
                httpClient,
                "https://api.brevo.com/v3/smtp/email",
                "xkeysib-bad",
                "remitente@gmail.com",
                "destino@gmail.com",
                "Asunto",
                "<p>Test</p>"
        ));
    }
}
