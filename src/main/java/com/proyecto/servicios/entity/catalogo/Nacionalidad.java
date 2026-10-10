package com.proyecto.servicios.entity.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro del catalogo de nacionalidades. Se llena/actualiza desde la API
 * de restcountries.com (ver NacionalidadServiceImpl.sincronizar()).
 */
@Entity
@Table(name = "catalogo_nacionalidades")
@Getter
@Setter
public class Nacionalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ISO 3166-1 alfa-3, ej. MEX. Es lo que se guarda en el cliente. */
    @Column(name = "codigo", nullable = false, unique = true, length = 3)
    private String codigo;

    /** ISO 3166-1 alfa-2, ej. MX. */
    @Column(name = "codigo_iso2", length = 2)
    private String codigoIso2;

    /** Nombre del pais en espanol, ej. Mexico. */
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "nombre_ingles", length = 150)
    private String nombreIngles;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    void alGuardar() {
        fechaActualizacion = LocalDateTime.now();
    }
}
