package com.proyecto.servicios.service.transaccion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ServicioNotificacion2FA {

    private static final String PREFIJO_REDIS = "2fa:cuenta:";
    private static final String CODIGO_FALLO_SIMULADO = "000000";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final SecureRandom random = new SecureRandom();

    @Value("${seguridad.2fa.expiracion-minutos:5}")
    private int expiracionMinutos;

    @Value("${spring.mail.username:}")
    private String remitente;

    // Fallback en memoria en caso de que Redis no este corriendo en local
    private final Map<String, EntradaCodigo2FA> cacheMemoria = new ConcurrentHashMap<>();

    private record EntradaCodigo2FA(String codigo, Instant expiraEn) {
        boolean estaVigente() {
            return Instant.now().isBefore(expiraEn);
        }
    }

    public ServicioNotificacion2FA(ObjectProvider<JavaMailSender> mailSenderProvider,
                                   ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.mailSenderProvider = mailSenderProvider;
        this.redisTemplateProvider = redisTemplateProvider;
    }

    public String generarYEnviarCodigo(String numeroCuenta, String correoDestino) {
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        guardarCodigo(numeroCuenta, codigo);

        enviarCorreo(correoDestino, numeroCuenta, codigo);
        return codigo;
    }

    public boolean validarYConsumirCodigo(String numeroCuenta, String codigoIngresado) {
        if (codigoIngresado == null || CODIGO_FALLO_SIMULADO.equals(codigoIngresado)) {
            return false;
        }

        // 1. Intentar validar desde Redis
        StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
        if (redis != null) {
            try {
                String clave = PREFIJO_REDIS + numeroCuenta;
                String codigoGuardado = redis.opsForValue().get(clave);
                if (codigoGuardado != null && codigoGuardado.equals(codigoIngresado.trim())) {
                    redis.delete(clave);
                    cacheMemoria.remove(numeroCuenta);
                    log.info("Código 2FA validado y consumido con éxito desde Redis para cuenta {}", numeroCuenta);
                    return true;
                }
            } catch (Exception e) {
                log.warn("Falla al consultar Redis para 2FA, recurriendo a cache en memoria: {}", e.getMessage());
            }
        }

        // 2. Fallback: validar desde cache en memoria
        EntradaCodigo2FA entrada = cacheMemoria.get(numeroCuenta);
        if (entrada != null && entrada.estaVigente() && entrada.codigo().equals(codigoIngresado.trim())) {
            cacheMemoria.remove(numeroCuenta);
            log.info("Código 2FA validado y consumido con éxito desde memoria para cuenta {}", numeroCuenta);
            return true;
        }

        return false;
    }

    private void guardarCodigo(String numeroCuenta, String codigo) {
        // Guardar en Redis
        StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
        if (redis != null) {
            try {
                redis.opsForValue().set(
                        PREFIJO_REDIS + numeroCuenta,
                        codigo,
                        Duration.ofMinutes(expiracionMinutos)
                );
                log.info("Código 2FA almacenado en Redis para cuenta {}", numeroCuenta);
            } catch (Exception e) {
                log.warn("No se pudo almacenar 2FA en Redis: {}", e.getMessage());
            }
        }

        // Guardar siempre en memoria como respaldo
        Instant expiraEn = Instant.now().plus(Duration.ofMinutes(expiracionMinutos));
        cacheMemoria.put(numeroCuenta, new EntradaCodigo2FA(codigo, expiraEn));
    }

    private void enviarCorreo(String correoDestino, String numeroCuenta, String codigo) {
        log.info("""
                
                ==============================================================
                  [2FA CORREO ELECTRÓNICO]
                  Destinatario:   {}
                  Cuenta:         {}
                  CÓDIGO 2FA:     {}
                  Vigencia:       {} minutos
                ==============================================================
                """, correoDestino, numeroCuenta, codigo, expiracionMinutos);

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || remitente == null || remitente.isBlank()) {
            log.info("SMTP no configurado (MAIL_USERNAME / MAIL_PASSWORD); el código 2FA queda disponible en consola y respuesta.");
            return;
        }

        try {
            String cuentaEnmascarada = numeroCuenta.length() > 4
                    ? "******" + numeroCuenta.substring(numeroCuenta.length() - 4) : numeroCuenta;

            String textoPlano = String.format("""
                    Estimado cliente,

                    Tu código de seguridad para autorizar la transacción en tu cuenta %s es:

                    %s

                    Este código es confidencial, es de un solo uso y tiene una vigencia de %d minutos.
                    Si no reconoces esta operación, por favor contacta a soporte inmediatamente.
                    """, cuentaEnmascarada, codigo, expiracionMinutos);

            String html = """
                    <html><body style="font-family:Arial,sans-serif;background:#f4f6f8;padding:24px;">
                      <table style="max-width:480px;margin:auto;background:#fff;border-radius:8px;padding:24px;text-align:center;" width="100%%">
                        <tr><td>
                          <h2 style="color:#0D47A1;margin-top:0;">Código de verificación</h2>
                          <p style="color:#555;">Usa este código para autorizar la transacción en tu cuenta <b>%s</b>:</p>
                          <p style="font-size:34px;letter-spacing:8px;font-weight:bold;color:#222;background:#eef3fb;
                                    border-radius:8px;padding:14px 0;margin:20px 0;">%s</p>
                          <p style="color:#555;">Vigencia: <b>%d minutos</b>. Es de un solo uso; no lo compartas con nadie.</p>
                          <p style="color:#888;font-size:12px;margin-top:20px;">
                            Si no reconoces esta operación, contacta a soporte inmediatamente.
                          </p>
                        </td></tr>
                      </table>
                    </body></html>
                    """.formatted(cuentaEnmascarada, codigo, expiracionMinutos);

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());
            helper.setFrom(remitente);
            helper.setTo(correoDestino);
            helper.setSubject("Código 2FA - Autorización de Pago");
            helper.setText(textoPlano, html);

            mailSender.send(mensaje);
            log.info("Correo de 2FA enviado exitosamente a {}", correoDestino);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo físico a {} (SMTP no conectado o credenciales no configuradas). "
                    + "El código queda disponible en log y respuesta: {}", correoDestino, e.getMessage());
        }
    }

    public String enmascararCorreo(String correo) {
        if (correo == null || !correo.contains("@")) {
            return "correo@registrado.com";
        }
        int atIndex = correo.indexOf('@');
        String usuario = correo.substring(0, atIndex);
        String dominio = correo.substring(atIndex);

        if (usuario.length() <= 2) {
            return usuario.charAt(0) + "***" + dominio;
        }
        return usuario.charAt(0) + "***" + usuario.charAt(usuario.length() - 1) + dominio;
    }
}
