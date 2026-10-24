package com.proyecto.servicios.model.cliente;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@Schema(description = "Respuesta del abono o depósito de saldo a la cuenta")
public class AbonoResponse {

    @Schema(description = "Número de cuenta beneficiaria", example = "1452864812")
    private String numeroCuenta;

    @Schema(description = "Monto abonado a la cuenta", example = "500.00")
    private BigDecimal montoAbonado;

    @Schema(description = "Saldo de la cuenta previo al depósito", example = "1000.00")
    private BigDecimal saldoAnterior;

    @Schema(description = "Nuevo saldo disponible en la cuenta", example = "1500.00")
    private BigDecimal nuevoSaldo;

    @Schema(description = "Concepto registrado", example = "Depósito en ventanilla")
    private String concepto;

    @JsonFormat(pattern = "yyyy/MM/dd")
    @Schema(description = "Fecha del movimiento", example = "2026/10/23")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(description = "Hora del movimiento", example = "18:55:00")
    private LocalTime hora;
}
