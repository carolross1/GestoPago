package com.proyecto.servicios.model.transaccion;

import com.proyecto.servicios.model.comun.SolicitudEstricta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Solicitud para generar y enviar un código 2FA al correo del cliente")
public class Solicitar2faRequest extends SolicitudEstricta {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "El número de cuenta debe tener exactamente 10 dígitos")
    @Schema(description = "Número de cuenta de 10 dígitos del cliente", example = "1452864812")
    private String numeroCuenta;
}
