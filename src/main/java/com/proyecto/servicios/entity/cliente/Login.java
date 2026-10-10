package com.proyecto.servicios.entity.cliente;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Credenciales de acceso del cliente al portal. La contrasena NUNCA se
 * cifra de forma reversible: se guarda como hash BCrypt (una via), que
 * es la practica correcta para contrasenas (distinto del cifrado
 * reversible que se usa para CURP/RFC/domicilio, etc.).
 */
@Entity
@Table(name = "login")
@Getter
@Setter
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false, unique = true)
    private Long clienteId;

    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}
