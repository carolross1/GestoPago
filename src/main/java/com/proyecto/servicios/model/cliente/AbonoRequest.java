package com.proyecto.servicios.model.cliente;

import com.proyecto.servicios.model.comun.SolicitudEstricta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Solicitud para abonar o depositar saldo a una cuenta")
public class AbonoRequest extends SolicitudEstricta {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "El número de cuenta debe tener exactamente 10 dígitos")
    @Schema(description = "Número de cuenta de 10 dígitos", example = "1452864812")
    private String numeroCuenta;

    @NotNull(message = "El monto a abonar es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Digits(integer = 12, fraction = 2, message = "El monto debe tener máximo 2 decimales (ej. 500.00)")
    @Schema(description = "Monto a depositar", example = "500.00")
    private BigDecimal monto;

    @Size(max = 100, message = "El concepto no puede superar los 100 caracteres")
    @Schema(description = "Concepto o motivo del depósito", example = "Depósito en ventanilla")
    private String concepto;
}
