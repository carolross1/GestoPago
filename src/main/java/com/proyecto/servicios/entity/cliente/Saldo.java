package com.proyecto.servicios.entity.cliente;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Historial de movimientos de saldo de una cuenta. Cada alta, deposito,
 * retiro o ajuste genera una fila nueva; el saldo "actual" es el de la
 * fila mas reciente para esa cuenta (mayor fecha). Los montos se
 * manejan como BigDecimal/NUMERIC, nunca como texto ni double, para no
 * perder precision en operaciones monetarias.
 */
@Entity
@Table(name = "saldos")
@Getter
@Setter
public class Saldo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;

    @Column(name = "monto", nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(name = "saldo_resultante", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoResultante;

    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    void onCreate() {
        fecha = LocalDateTime.now();
    }
}
