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

    @Column(name = "concepto", length = 100)
    private String concepto;

    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(name = "folio", length = 50)
    private String folio;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "id_servicio")
    private Integer idServicio;

    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "monto", nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    @Column(name = "saldo_resultante", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoResultante;

    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "EXITOSA";
        }
    }
}
