package com.proyecto.servicios.model.cliente;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static com.proyecto.servicios.model.cliente.ValidacionPatrones.FORMATO_FECHA;
import static com.proyecto.servicios.model.cliente.ValidacionPatrones.FORMATO_HORA;

@Data
public class CuentaResponse {
    private String numeroCuenta;
    private Long clienteId;
    private String estatus;

    /** Siempre con 2 decimales, ej. 0.00 */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(implementation = String.class, format = "dinero", example = "0.00")
    private BigDecimal saldoActual;

    @JsonFormat(pattern = FORMATO_FECHA)
    @Schema(type = "string", description = "Formato yyyy/MM/dd", example = "2026/10/02")
    private LocalDate fechaApertura;

    @JsonFormat(pattern = FORMATO_HORA)
    @Schema(type = "string", description = "Formato HH:mm:ss", example = "18:24:05")
    private LocalTime horaApertura;
}
