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

        String asunto = "Comprobante de compra - " + datos.getServicio() + " [UPC: " + datos.getUpc() + "]";
        String cuerpoHtml = construirCuerpoHtml(datos);

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
        String nombre = HtmlUtils.htmlEscape(datos.getNombreCliente() != null ? datos.getNombreCliente() : "Cliente");
        String cuentaRaw = datos.getNumeroCuenta() != null ? datos.getNumeroCuenta() : "";
        String cuentaEnmascarada = cuentaRaw.length() > 4
                ? "•••• " + cuentaRaw.substring(cuentaRaw.length() - 4)
                : cuentaRaw;
        String cuenta = HtmlUtils.htmlEscape(cuentaEnmascarada);
        String servicio = HtmlUtils.htmlEscape(datos.getServicio());
        String producto = HtmlUtils.htmlEscape(datos.getProducto());
        String destino = HtmlUtils.htmlEscape(datos.getDestino());
        String idTx = HtmlUtils.htmlEscape(datos.getIdTx());
        String autorizacion = HtmlUtils.htmlEscape(datos.getNumAutorizacion());
        String upc = HtmlUtils.htmlEscape(datos.getUpc());
        String urlLogo = datos.getUrlLogo();

        String fecha = datos.getFechaHora() != null ? datos.getFechaHora().format(FORMATO_FECHA) : "";
        String hora = datos.getFechaHora() != null ? datos.getFechaHora().format(FORMATO_HORA) : "";

        return "<!DOCTYPE html>"
                + "<html lang='es'>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif; background-color: #f3f4f6; margin: 0; padding: 24px; color: #1f2937;'>"
                + "  <table width='100%' cellpadding='0' cellspacing='0' style='max-width: 560px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);'>"
                + "    <tr>"
                + "      <td style='background: linear-gradient(135deg, #0d47a1 0%, #1976d2 100%); padding: 28px 24px; text-align: center; color: #ffffff;'>"
                + "        <img src='" + urlLogo + "' alt='" + servicio + "' width='64' height='64' style='display: block; margin: 0 auto 12px auto; border-radius: 8px; background: #ffffff; object-fit: contain; padding: 4px; box-sizing: border-box;' />"
                + "        <h1 style='margin: 0; font-size: 20px; font-weight: 700; letter-spacing: 0.5px;'>Comprobante de Pago</h1>"
                + "        <p style='margin: 4px 0 0 0; font-size: 13px; opacity: 0.9;'>GestoPago &bull; Transacción exitosa</p>"
                + "      </td>"
                + "    </tr>"
                + "    <tr>"
                + "      <td style='padding: 24px;'>"
                + "        <p style='margin: 0 0 16px 0; font-size: 15px;'>Hola, <strong>" + nombre + "</strong>:</p>"
                + "        <p style='margin: 0 0 20px 0; font-size: 14px; line-height: 1.5; color: #4b5563;'>Tu pago ha sido procesado correctamente. A continuación te presentamos el resumen oficial de la transacción:</p>"
                + "        <table width='100%' cellpadding='0' cellspacing='0' style='border-collapse: collapse; margin-bottom: 20px; font-size: 13px;'>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 10px 0; color: #6b7280;'>Servicio:</td><td style='padding: 10px 0; font-weight: 600; text-align: right;'>" + servicio + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 10px 0; color: #6b7280;'>Producto:</td><td style='padding: 10px 0; text-align: right;'>" + producto + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 10px 0; color: #6b7280;'>Destino / Referencia:</td><td style='padding: 10px 0; font-weight: 600; text-align: right;'>" + destino + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 10px 0; color: #6b7280;'>Cuenta origen:</td><td style='padding: 10px 0; text-align: right;'>" + cuenta + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 10px 0; color: #6b7280;'>Folio UPC:</td><td style='padding: 10px 0; font-family: monospace; font-size: 14px; font-weight: 700; color: #0d47a1; text-align: right;'>" + upc + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 10px 0; color: #6b7280;'>No. de autorización:</td><td style='padding: 10px 0; text-align: right;'>" + autorizacion + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 10px 0; color: #6b7280;'>ID Transacción GestoPago:</td><td style='padding: 10px 0; text-align: right;'>" + idTx + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 10px 0; color: #6b7280;'>Fecha y hora:</td><td style='padding: 10px 0; text-align: right;'>" + fecha + " " + hora + "</td></tr>"
                + "          <tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 10px 0; color: #6b7280; font-weight: 600;'>Monto pagado:</td><td style='padding: 10px 0; font-size: 16px; font-weight: 700; color: #047857; text-align: right;'>$" + datos.getMonto() + " MXN</td></tr>"
                + "          <tr><td style='padding: 10px 0; color: #6b7280;'>Saldo restante:</td><td style='padding: 10px 0; font-weight: 600; text-align: right;'>$" + datos.getSaldoRestante() + " MXN</td></tr>"
                + "        </table>"
                + "        <div style='background-color: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 8px; padding: 12px; margin-top: 16px; font-size: 12px; color: #065f46; text-align: center;'>"
                + "          Conserva este comprobante para cualquier aclaración o consulta sobre tu pago."
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
