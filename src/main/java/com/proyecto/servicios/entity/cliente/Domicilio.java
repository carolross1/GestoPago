package com.proyecto.servicios.entity.cliente;

import com.proyecto.servicios.security.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "domicilios")
@Getter
@Setter
public class Domicilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false, unique = true)
    private Long clienteId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "calle", nullable = false, columnDefinition = "TEXT")
    private String calle;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "numero_exterior", nullable = false, columnDefinition = "TEXT")
    private String numeroExterior;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "numero_interior", columnDefinition = "TEXT")
    private String numeroInterior;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "colonia", nullable = false, columnDefinition = "TEXT")
    private String colonia;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "municipio", nullable = false, columnDefinition = "TEXT")
    private String municipio;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "estado", nullable = false, columnDefinition = "TEXT")
    private String estado;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "codigo_postal", nullable = false, columnDefinition = "TEXT")
    private String codigoPostal;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "pais", nullable = false, columnDefinition = "TEXT")
    private String pais;
}
