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

    @Value("${mail.brevo.api-key:}")
    private String brevoApiKey;

    @Value("${mail.webhook.url:}")
    private String mailWebhookUrl;

    @Value("${mail.webhook.secret:}")
    private String mailWebhookSecret;

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
        String plantilla = (plantillaUrlLogo != null && !plantillaUrlLogo.isBlank())
                ? plantillaUrlLogo
                : "https://gestopago.portalventas.net/sistema/images/gestopago/servicios/{idServicio}.png";

        String idProdStr = idProducto != null ? String.valueOf(idProducto) : "0";
        String idServStr = (idServicio != null && idServicio > 0) ? String.valueOf(idServicio) : "166";
        String servStr = (servicio != null && !servicio.isBlank()) ? servicio.trim() : "Servicio";
        String prodStr = (producto != null && !producto.isBlank()) ? producto.trim() : "Producto";

        return plantilla
                .replace("{idProducto}", idProdStr)
                .replace("{idServicio}", idServStr)
                .replace("{servicio}", codificar(servStr))
                .replace("{producto}", codificar(prodStr));
    }

    public void enviarComprobante(ComprobantePagoCorreo datos) {
        if (datos.getCorreoDestino() == null || datos.getCorreoDestino().isBlank()) {
            log.warn("El cliente de la cuenta {} no tiene correo; no se envía comprobante de pago", datos.getNumeroCuenta());
            return;
        }

        boolean aprobada = datos.isAprobada();
        String asunto = aprobada
                ? "Comprobante de compra - " + datos.getServicio() + " [UPC: " + datos.getUpc() + "]"
                : "OPERACIÓN RECHAZADA - " + datos.getServicio() + " [UPC: " + datos.getUpc() + "]";
        String cuerpoHtml = construirCuerpoHtml(datos);

        if (EnviadorCorreoHttp.enviarPorWebhook(mailWebhookUrl, mailWebhookSecret, datos.getCorreoDestino(), asunto, cuerpoHtml)) {
            return;
        }

        if (EnviadorCorreoHttp.enviarPorBrevo(brevoApiKey, remitente, datos.getCorreoDestino(), asunto, cuerpoHtml)) {
            return;
        }

        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("JavaMailSender no disponible; se omite el envío del comprobante a {}", datos.getCorreoDestino());
            return;
        }

        try {
            MimeMessage mensaje = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());

            if (remitente != null && !remitente.isBlank()) {
                helper.setFrom(remitente);
            }
            helper.setTo(datos.getCorreoDestino());
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);

            sender.send(mensaje);
            log.info("Comprobante de compra enviado exitosamente a {}", datos.getCorreoDestino());
        } catch (Exception e) {
            log.error("Fallo al enviar comprobante de pago a {}: {}", datos.getCorreoDestino(), e.getMessage(), e);
        }
    }

    private String construirCuerpoHtml(ComprobantePagoCorreo datos) {
        boolean aprobada = datos.isAprobada();
        String nombre = HtmlUtils.htmlEscape(datos.getNombreCliente() != null ? datos.getNombreCliente() : "Cliente");
        String cuentaRaw = datos.getNumeroCuenta() != null ? datos.getNumeroCuenta() : "";
        String cuentaEnmascarada = cuentaRaw.length() > 4
                ? "•••• " + cuentaRaw.substring(cuentaRaw.length() - 4)
                : cuentaRaw;
        String cuenta = HtmlUtils.htmlEscape(cuentaEnmascarada);
        String servicio = HtmlUtils.htmlEscape(datos.getServicio() != null ? datos.getServicio() : "Servicio");
        String producto = HtmlUtils.htmlEscape(datos.getProducto() != null ? datos.getProducto() : "Producto");
        String destino = HtmlUtils.htmlEscape(datos.getDestino() != null ? datos.getDestino() : "-");
        String autorizacion = HtmlUtils.htmlEscape(datos.getNumAutorizacion() != null ? datos.getNumAutorizacion() : "-");
        String upc = HtmlUtils.htmlEscape(datos.getUpc() != null ? datos.getUpc() : "-");
        String leyenda = HtmlUtils.htmlEscape(
                datos.getLeyenda() != null && !datos.getLeyenda().isBlank()
                        ? datos.getLeyenda()
                        : "Conserva este comprobante y tu folio UPC de 10 dígitos para cualquier aclaración o reclamación.");
        String codigoResp = HtmlUtils.htmlEscape(
                datos.getCodigoRespuesta() != null ? datos.getCodigoRespuesta() : (aprobada ? "01" : "-1"));
        String mensajeOp = HtmlUtils.htmlEscape(
                datos.getMensajeOperacion() != null && !datos.getMensajeOperacion().isBlank()
                        ? datos.getMensajeOperacion()
                        : (aprobada ? "Operación realizada con éxito" : "Transacción no válida"));
        String urlLogo = datos.getUrlLogo();

        String fecha = datos.getFechaHora() != null ? datos.getFechaHora().format(FORMATO_FECHA) : "";
        String hora = datos.getFechaHora() != null ? datos.getFechaHora().format(FORMATO_HORA) : "";

        String headerBg = aprobada
                ? "linear-gradient(135deg, #2E1065 0%, #5B21B6 55%, #7C3AED 100%)"
                : "linear-gradient(135deg, #7F1D1D 0%, #B91C1C 55%, #DC2626 100%)";
        String titulo = aprobada ? "Comprobante de Pago" : "OPERACIÓN RECHAZADA";
        String subtitulo = aprobada
                ? "GestoPago &bull; Transacción exitosa"
                : "GestoPago &bull; " + mensajeOp;
        String intro = aprobada
                ? "Tu pago ha sido procesado correctamente. A continuación te presentamos el ticket oficial de la operación:"
                : "<strong style='color:#B91C1C;'>Transacción no válida.</strong> GestoPago no pudo procesar esta operación y <strong>no se realizó ningún cargo a tu saldo</strong>. Conserva este ticket con tu UPC, fecha y leyenda del producto para cualquier reclamación:";
        String cajaBg = aprobada ? "#F5F3FF" : "#FEF2F2";
        String cajaBorder = aprobada ? "#DDD6FE" : "#FECACA";
        String cajaColor = aprobada ? "#4C1D95" : "#991B1B";

        return "<!DOCTYPE html>"
                + "<html lang='es'>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif; background-color: #f3f4f6; margin: 0; padding: 24px; color: #1f2937;'>"
                + "  <table width='100%' cellpadding='0' cellspacing='0' style='max-width: 580px; margin: 0 auto; background-color: #ffffff; border-radius: 14px; overflow: hidden; box-shadow: 0 4px 16px rgba(0,0,0,0.08);'>"
                + "    <tr>"
                + "      <td style='background: " + headerBg + "; padding: 28px 24px; text-align: center; color: #ffffff;'>"
                + "        <img src='" + urlLogo + "' alt='" + servicio + "' width='64' height='64' style='display: block; margin: 0 auto 12px auto; border-radius: 12px; background: #ffffff; object-fit: contain; padding: 6px; box-sizing: border-box;' />"
                + "        <h1 style='margin: 0; font-size: 20px; font-weight: 800; letter-spacing: 0.5px;'>" + titulo + "</h1>"
                + "        <p style='margin: 6px 0 0 0; font-size: 13px; opacity: 0.92;'>" + subtitulo + "</p>"
                + "      </td>"
                + "    </tr>"
                + "    <tr>"
                + "      <td style='padding: 24px;'>"
                + "        <p style='margin: 0 0 12px 0; font-size: 15px;'>Hola, <strong>" + nombre + "</strong>:</p>"
                + "        <p style='margin: 0 0 18px 0; font-size: 13.5px; line-height: 1.5; color: #4b5563;'>" + intro + "</p>"
                + "        <table width='100%' cellpadding='0' cellspacing='0' style='border-collapse: collapse; margin-bottom: 18px; font-size: 13px;'>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 9px 0; color: #6b7280;'>Estado:</td><td style='padding: 9px 0; font-weight: 700; color: " + (aprobada ? "#047857" : "#DC2626") + "; text-align: right;'>" + (aprobada ? "EXITOSA" : "OPERACIÓN RECHAZADA") + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 9px 0; color: #6b7280;'>Mensaje:</td><td style='padding: 9px 0; font-weight: 600; text-align: right;'>" + mensajeOp + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 9px 0; color: #6b7280;'>UPC (10 dígitos):</td><td style='padding: 9px 0; font-family: monospace; font-size: 14px; font-weight: 800; color: #5B21B6; text-align: right;'>" + upc + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 9px 0; color: #6b7280;'>Fecha y hora:</td><td style='padding: 9px 0; text-align: right;'>" + fecha + " " + hora + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 9px 0; color: #6b7280;'>Servicio:</td><td style='padding: 9px 0; font-weight: 600; text-align: right;'>" + servicio + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 9px 0; color: #6b7280;'>Producto:</td><td style='padding: 9px 0; text-align: right;'>" + producto + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 9px 0; color: #6b7280;'>Destino / Referencia:</td><td style='padding: 9px 0; font-weight: 600; text-align: right;'>" + destino + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 9px 0; color: #6b7280;'>Cuenta origen:</td><td style='padding: 9px 0; text-align: right;'>" + cuenta + "</td></tr>"
                + (aprobada
                    ? "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 9px 0; color: #6b7280;'>No. de autorización:</td><td style='padding: 9px 0; font-weight: 600; text-align: right;'>" + autorizacion + "</td></tr>"
                    : "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 9px 0; color: #6b7280;'>Código de respuesta:</td><td style='padding: 9px 0; font-family: monospace; font-weight: 700; color: #DC2626; text-align: right;'>" + codigoResp + "</td></tr>")
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 9px 0; color: #6b7280; font-weight: 600;'>" + (aprobada ? "Monto pagado:" : "Monto intentado:") + "</td><td style='padding: 9px 0; font-size: 16px; font-weight: 700; color: " + (aprobada ? "#047857" : "#DC2626") + "; text-align: right;'>$" + datos.getMonto() + " MXN</td></tr>"
                + "          <tr><td style='padding: 9px 0; color: #6b7280;'>Saldo en cuenta:</td><td style='padding: 9px 0; font-weight: 600; text-align: right;'>$" + datos.getSaldoRestante() + " MXN</td></tr>"
                + "        </table>"
                + "        <div style='background-color: " + cajaBg + "; border: 1px solid " + cajaBorder + "; border-radius: 10px; padding: 14px; margin-top: 14px; font-size: 12px; color: " + cajaColor + "; line-height: 1.5;'>"
                + "          <strong>Dudas y aclaraciones:</strong><br/>" + leyenda
                + "        </div>"
                + "      </td>"
                + "    </tr>"
                + "    <tr>"
                + "      <td style='background-color: #f9fafb; padding: 16px 24px; text-align: center; font-size: 11px; color: #9ca3af; border-top: 1px solid #e5e7eb;'>"
                + "        Este es un mensaje automático, por favor no respondas a este correo.<br/>"
                + "        &copy; 2026 GestoPago. Todos los derechos reservados."
                + "      </td>"
                + "    </tr>"
                + "  </table>"
                + "</body>"
                + "</html>";
    }

    private String codificar(String texto) {
        return URLEncoder.encode(texto, StandardCharsets.UTF_8);
    }
}
