package com.proyecto.servicios.model.cliente;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CuentaResponse {
    private String numeroCuenta;
    private Long clienteId;
    private String estatus;
    private BigDecimal saldoActual;
    private LocalDateTime fechaApertura;
}
