package com.proyecto.servicios.model.cliente;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SaldoResponse {
    private String numeroCuenta;
    private BigDecimal saldoActual;
    private LocalDateTime fechaUltimoMovimiento;
}
