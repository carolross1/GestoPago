package com.proyecto.servicios.model.transaccion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Solicitud para consultar el historial de pagos de una cuenta")
public class HistorialTransaccionesRequest {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "El número de cuenta debe tener exactamente 10 dígitos")
    @Schema(description = "Número de cuenta del cliente autenticado", example = "1452864812")
    private String numeroCuenta;
}
