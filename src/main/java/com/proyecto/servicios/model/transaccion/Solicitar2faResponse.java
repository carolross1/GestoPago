package com.proyecto.servicios.model.transaccion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Respuesta tras generar y enviar el código 2FA al correo registrado")
public class Solicitar2faResponse {

    @Schema(description = "Número de cuenta del cliente", example = "1452864812")
    private String numeroCuenta;

    @Schema(description = "Correo al que fue enviado el código (enmascarado para privacidad)", example = "c***s@gmail.com")
    private String correoEnmascarado;

    @Schema(description = "Mensaje descriptivo", example = "Código 2FA enviado al correo registrado. Válido por 5 minutos.")
    private String mensaje;

    @Schema(description = "Minutos de vigencia del código", example = "5")
    private int expiracionMinutos;

    @Schema(description = "Código 2FA generado (visible para facilitar pruebas en ambiente de desarrollo)", example = "482915")
    private String codigoParaPruebas;
}
