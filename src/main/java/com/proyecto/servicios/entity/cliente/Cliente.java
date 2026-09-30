package com.proyecto.servicios.entity.cliente;

import com.proyecto.servicios.security.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cliente persona fisica. Los campos de texto identificables (nombre,
 * CURP, RFC, contacto, laborales) se guardan cifrados con AES via
 * EncryptedStringConverter. ingresoMensual se deja como NUMERIC/BigDecimal
 * sin cifrar, ya que es un dato numerico de negocio (no texto).
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "nombre", nullable = false, columnDefinition = "TEXT")
    private String nombre;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "segundo_nombre", columnDefinition = "TEXT")
    private String segundoNombre;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "apellido_paterno", nullable = false, columnDefinition = "TEXT")
    private String apellidoPaterno;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "apellido_materno", nullable = false, columnDefinition = "TEXT")
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "curp", nullable = false, unique = true, columnDefinition = "TEXT")
    private String curp;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "rfc", nullable = false, unique = true, columnDefinition = "TEXT")
    private String rfc;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "sexo", nullable = false, columnDefinition = "TEXT")
    private String sexo;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "nacionalidad", nullable = false, columnDefinition = "TEXT")
    private String nacionalidad;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "estado_civil", nullable = false, columnDefinition = "TEXT")
    private String estadoCivil;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "correo", nullable = false, unique = true, columnDefinition = "TEXT")
    private String correo;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "telefono_movil", nullable = false, columnDefinition = "TEXT")
    private String telefonoMovil;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "telefono_alternativo", columnDefinition = "TEXT")
    private String telefonoAlternativo;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "ocupacion", nullable = false, columnDefinition = "TEXT")
    private String ocupacion;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "empresa", nullable = false, columnDefinition = "TEXT")
    private String empresa;

    @Column(name = "ingreso_mensual", nullable = false, precision = 14, scale = 2)
    private BigDecimal ingresoMensual;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void onCreate() {
        fechaRegistro = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
