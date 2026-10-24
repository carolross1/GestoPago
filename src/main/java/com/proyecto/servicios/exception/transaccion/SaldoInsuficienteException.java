package com.proyecto.servicios.exception.transaccion;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends RuntimeException {

    private final BigDecimal saldoActual;
    private final BigDecimal montoRequerido;

    public SaldoInsuficienteException(BigDecimal saldoActual, BigDecimal montoRequerido) {
        super(String.format("Saldo insuficiente en la cuenta. Saldo actual: $%s, Monto requerido: $%s",
                saldoActual != null ? saldoActual.setScale(2).toPlainString() : "0.00",
                montoRequerido != null ? montoRequerido.setScale(2).toPlainString() : "0.00"));
        this.saldoActual = saldoActual;
        this.montoRequerido = montoRequerido;
    }

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public BigDecimal getMontoRequerido() {
        return montoRequerido;
    }
}
