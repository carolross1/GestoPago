package com.proyecto.servicios.service.transaccion;

import com.proyecto.servicios.model.transaccion.ComprobantePagoCorreo;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Envia al cliente un comprobante (correo HTML) cuando un pago se procesa con exito.
 * El correo incluye el logo del producto comprado (URL armada con la plantilla
 * {@code productos.logo.url-plantilla}) y los datos principales de la compra.
 *
 * Un fallo al enviar el correo NUNCA revierte el pago: solo se registra en el log.
 */
@Slf4j
@Service
public class ServicioNotificacionPago {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:}")
    private String remitente;

    /**
     * Plantilla de la URL del logo. Marcadores soportados:
     * {idProducto}, {idServicio}, {servicio}, {producto}.
     */
    @Value("${productos.logo.url-plantilla:https://ui-avatars.com/api/?name={servicio}&size=128&background=0D47A1&color=fff&bold=true}")
    private String plantillaUrlLogo;

    public ServicioNotificacionPago(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public String construirUrlLogo(Integer idProducto, Integer idServicio, String servicio, String producto) {
        return plantillaUrlLogo
                .replace("{idProducto}", String.valueOf(idProducto))
                .replace("{idServicio}", String.valueOf(idServicio))
                .replace("{servicio}", codificar(servicio != null ? servicio : "Servicio"))
                .replace("{producto}", codificar(producto != null ? producto : "Producto"));
    }

    public void enviarComprobante(ComprobantePagoCorreo datos) {
        if (datos.getCorreoDestino() == null || datos.getCorreoDestino().isBlank()) {
            log.warn("El cliente de la cuenta {} no tiene correo; no se envía comprobante de pago", datos.getNumeroCuenta());
            return;
        }

        log.info("""

                ==============================================================
                  [COMPROBANTE DE PAGO POR CORREO]
                  Destinatario:   {}
                  Producto:       {} - {}
                  Logo:           {}
                  Monto:          ${}
                  IdTx / Autor.:  {} / {}
                  UPC:            {}
                ==============================================================
                """, datos.getCorreoDestino(), datos.getServicio(), datos.getProducto(), datos.getUrlLogo(),
                datos.getMonto(), datos.getIdTx(), datos.getNumAutorizacion(), datos.getUpc());

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || remitente == null || remitente.isBlank()) {
            log.info("SMTP no configurado (MAIL_USERNAME / MAIL_PASSWORD); el comprobante solo queda en el log.");
            return;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());
            helper.setFrom(remitente);
            helper.setTo(datos.getCorreoDestino());
            helper.setSubject("Comprobante de pago - " + valor(datos.getServicio()));
            helper.setText(construirTextoPlano(datos), construirHtml(datos));

            mailSender.send(mensaje);
            log.info("Comprobante de pago enviado a {}", datos.getCorreoDestino());
        } catch (Exception e) {
            log.warn("No se pudo enviar el comprobante de pago a {}: {}", datos.getCorreoDestino(), e.getMessage());
        }
    }

    private String construirHtml(ComprobantePagoCorreo d) {
        return """
                <html><body style="font-family:Arial,sans-serif;background:#f4f6f8;padding:24px;">
                  <table style="max-width:520px;margin:auto;background:#fff;border-radius:8px;padding:24px;" width="100%%">
                    <tr><td style="text-align:center;">
                      <img src="%s" alt="%s" width="96" height="96" style="border-radius:8px;"/>
                      <h2 style="color:#0D47A1;margin:12px 0 4px;">¡Pago realizado con éxito!</h2>
                      <p style="color:#555;margin:0;">Hola %s, este es el comprobante de tu compra.</p>
                    </td></tr>
                    <tr><td>
                      <table style="width:100%%;margin-top:20px;border-collapse:collapse;font-size:14px;">
                        %s
                      </table>
                      <p style="color:#888;font-size:12px;margin-top:20px;">
                        Si no reconoces esta operación, contacta a soporte inmediatamente.
                      </p>
                    </td></tr>
                  </table>
                </body></html>
                """.formatted(
                escapar(d.getUrlLogo()),
                escapar(d.getServicio()),
                escapar(d.getNombreCliente()),
                fila("Servicio", d.getServicio())
                        + fila("Producto", d.getProducto())
                        + fila("Destino", d.getDestino())
                        + fila("Monto", "$" + d.getMonto())
                        + fila("Cuenta", enmascararCuenta(d.getNumeroCuenta()))
                        + fila("Saldo restante", "$" + d.getSaldoRestante())
                        + fila("ID de transacción", d.getIdTx())
                        + fila("No. de autorización", d.getNumAutorizacion())
                        + fila("Folio (UPC)", d.getUpc())
                        + fila("Fecha", d.getFechaHora().format(FORMATO_FECHA))
                        + fila("Hora", d.getFechaHora().format(FORMATO_HORA)));
    }

    private String construirTextoPlano(ComprobantePagoCorreo d) {
        return """
                Hola %s,

                Tu pago se realizó con éxito.

                Servicio:            %s
                Producto:            %s
                Destino:             %s
                Monto:               $%s
                Cuenta:              %s
                Saldo restante:      $%s
                ID de transacción:   %s
                No. de autorización: %s
                Folio (UPC):         %s
                Fecha y hora:        %s %s
                Logo:                %s

                Si no reconoces esta operación, contacta a soporte inmediatamente.
                """.formatted(valor(d.getNombreCliente()), valor(d.getServicio()), valor(d.getProducto()),
                valor(d.getDestino()), d.getMonto(), enmascararCuenta(d.getNumeroCuenta()), d.getSaldoRestante(),
                valor(d.getIdTx()), valor(d.getNumAutorizacion()), valor(d.getUpc()),
                d.getFechaHora().format(FORMATO_FECHA), d.getFechaHora().format(FORMATO_HORA), d.getUrlLogo());
    }

    private String fila(String etiqueta, String valor) {
        return "<tr><td style=\"padding:6px 0;color:#777;border-bottom:1px solid #eee;\">" + escapar(etiqueta)
                + "</td><td style=\"padding:6px 0;text-align:right;font-weight:bold;border-bottom:1px solid #eee;\">"
                + escapar(valor) + "</td></tr>";
    }

    private static String enmascararCuenta(String cuenta) {
        if (cuenta == null || cuenta.length() < 4) {
            return valor(cuenta);
        }
        return "******" + cuenta.substring(cuenta.length() - 4);
    }

    private static String escapar(String texto) {
        return HtmlUtils.htmlEscape(valor(texto));
    }

    private static String valor(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }

    private static String codificar(String texto) {
        return URLEncoder.encode(texto, StandardCharsets.UTF_8);
    }
}
