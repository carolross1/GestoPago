package com.proyecto.servicios.service.transaccion;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioNotificacion2FATest {

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ServicioNotificacion2FA servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioNotificacion2FA(mailSenderProvider, redisTemplateProvider);
        ReflectionTestUtils.setField(servicio, "expiracionMinutos", 5);
        ReflectionTestUtils.setField(servicio, "remitente", "soporte@banco.com");
    }

    @Test
    @DisplayName("generarYEnviarCodigo guarda en Redis y envia correo cuando ambos estan disponibles")
    void generarYEnviarCodigoConRedisYMail() {
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        String codigo = servicio.generarYEnviarCodigo("1234567890", "cliente@correo.com");

        assertNotNull(codigo);
        assertEquals(6, codigo.length());
        verify(valueOperations).set(eq("2fa:cuenta:1234567890"), eq(codigo), any(Duration.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("generarYEnviarCodigo funciona en modo fallback si Redis y SMTP fallan")
    void generarYEnviarCodigoFallback() {
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doThrow(new RuntimeException("SMTP timeout")).when(mailSender).send(any(MimeMessage.class));

        String codigo = servicio.generarYEnviarCodigo("1234567890", "cliente@correo.com");

        assertNotNull(codigo);
        // Debe guardarse en memoria como fallback y ser validable
        assertTrue(servicio.validarYConsumirCodigo("1234567890", codigo));
    }

    @Test
    @DisplayName("validarYConsumirCodigo valida exitosamente desde Redis")
    void validarDesdeRedis() {
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("2fa:cuenta:1234567890")).thenReturn("654321");

        boolean valido = servicio.validarYConsumirCodigo("1234567890", "654321");

        assertTrue(valido);
        verify(redisTemplate).delete("2fa:cuenta:1234567890");
    }

    @Test
    @DisplayName("validarYConsumirCodigo rechaza codigo nulo, simulado o incorrecto")
    void validarCodigosInvalidos() {
        assertFalse(servicio.validarYConsumirCodigo("1234567890", null));
        assertFalse(servicio.validarYConsumirCodigo("1234567890", "000000"));

        when(redisTemplateProvider.getIfAvailable()).thenReturn(null);
        assertFalse(servicio.validarYConsumirCodigo("1234567890", "999999"));
    }

    @Test
    @DisplayName("generarYEnviarCodigo con enviarPorCorreo=false guarda el codigo sin enviar correo")
    void generarCodigoSilenciosoSinCorreo() {
        when(redisTemplateProvider.getIfAvailable()).thenReturn(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String codigo = servicio.generarYEnviarCodigo("1234567890", "cliente@correo.com", false);

        assertNotNull(codigo);
        assertEquals(6, codigo.length());
        verify(valueOperations).set(eq("2fa:cuenta:1234567890"), eq(codigo), any(Duration.class));
        verify(mailSenderProvider, never()).getIfAvailable();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enmascararCorreo oculta caracteres centrales de acuerdo a la longitud")
    void enmascararCorreo() {
        assertEquals("c***s@correo.com", servicio.enmascararCorreo("carlos@correo.com"));
        assertEquals("a***@correo.com", servicio.enmascararCorreo("al@correo.com"));
        assertEquals("correo@registrado.com", servicio.enmascararCorreo("invalido"));
        assertEquals("correo@registrado.com", servicio.enmascararCorreo(null));
    }
}
