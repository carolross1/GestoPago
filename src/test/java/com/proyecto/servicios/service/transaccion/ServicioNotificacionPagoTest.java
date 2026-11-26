package com.proyecto.servicios.service.transaccion;

import com.proyecto.servicios.model.transaccion.ComprobantePagoCorreo;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioNotificacionPagoTest {

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private ServicioNotificacionPago servicio;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(servicio, "remitente", "notificaciones@banco.com");
        ReflectionTestUtils.setField(servicio, "plantillaUrlLogo",
                "https://cdn.empresa.com/{idServicio}/{idProducto}?srv={servicio}&prd={producto}");
    }

    @Test
    @DisplayName("construirUrlLogo reemplaza correctamente las variables")
    void construirUrlLogoExitoso() {
        String urlConocido = servicio.construirUrlLogo(582, 133, "Telcel", "Paquete Amigo 100");
        assertTrue(urlConocido.contains("133/582"));
        assertTrue(urlConocido.contains("srv=Telcel"));

        String urlGenerico = servicio.construirUrlLogo(582, 133, "ServicioPersonalizado", "Plan Especial");
        assertTrue(urlGenerico.contains("133/582"));
        assertTrue(urlGenerico.contains("srv=ServicioPersonalizado"));
        assertTrue(urlGenerico.contains("prd=Plan+Especial"));
    }

    @Test
    @DisplayName("construirUrlLogo maneja valores nulos sin fallar")
    void construirUrlLogoValoresNulos() {
        String url = servicio.construirUrlLogo(582, 133, null, null);
        assertTrue(url.contains("srv=Servicio"));
        assertTrue(url.contains("prd=Producto"));
    }

    @Test
    @DisplayName("enviarComprobante no hace nada si el correo destino es nulo o blanco")
    void enviarComprobanteCorreoVacio() {
        ComprobantePagoCorreo datos = ComprobantePagoCorreo.builder()
                .correoDestino("   ")
                .numeroCuenta("1234567890")
                .build();

        servicio.enviarComprobante(datos);
        verify(mailSenderProvider, never()).getIfAvailable();
    }

    @Test
    @DisplayName("enviarComprobante envia correo HTML exitosamente")
    void enviarComprobanteExitoso() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        ComprobantePagoCorreo datos = ComprobantePagoCorreo.builder()
                .correoDestino("cliente@correo.com")
                .nombreCliente("Ana Lopez")
                .numeroCuenta("1234567890")
                .servicio("CFE")
                .producto("Pago de Luz")
                .urlLogo("https://cdn.empresa.com/logo.png")
                .destino("5512345678")
                .monto(new BigDecimal("150.00"))
                .saldoRestante(new BigDecimal("850.00"))
                .idTx("TX998877")
                .numAutorizacion("AUTH123")
                .upc("ABCDE12345")
                .fechaHora(LocalDateTime.now())
                .build();

        servicio.enviarComprobante(datos);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarComprobante maneja excepciones de SMTP sin interrumpir el flujo")
    void enviarComprobanteManejaErrorSmtp() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doThrow(new RuntimeException("Error en conexion SMTP")).when(mailSender).send(any(MimeMessage.class));

        ComprobantePagoCorreo datos = ComprobantePagoCorreo.builder()
                .correoDestino("cliente@correo.com")
                .nombreCliente("Ana Lopez")
                .numeroCuenta("1234567890")
                .servicio("CFE")
                .producto("Pago de Luz")
                .urlLogo("https://cdn.empresa.com/logo.png")
                .destino("5512345678")
                .monto(new BigDecimal("50.00"))
                .saldoRestante(new BigDecimal("100.00"))
                .idTx("TX998877")
                .numAutorizacion("AUTH123")
                .upc("ABCDE12345")
                .fechaHora(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> servicio.enviarComprobante(datos));
    }
}
