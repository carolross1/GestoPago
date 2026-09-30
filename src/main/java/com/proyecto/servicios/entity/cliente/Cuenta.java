package com.proyecto.servicios.entity.cliente;

import com.proyecto.servicios.security.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas")
@Getter
@Setter
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "numero_cuenta", nullable = false, unique = true, columnDefinition = "TEXT")
    private String numeroCuenta;

    /**
     * ACTIVA o INACTIVA. Solo los clientes activos pueden tener cuentas activas.
     */
    @Column(name = "estatus", nullable = false, length = 20)
    private String estatus;

    @Column(name = "fecha_apertura", nullable = false, updatable = false)
    private LocalDateTime fechaApertura;

    @PrePersist
    void onCreate() {
        fechaApertura = LocalDateTime.now();
    }
}
