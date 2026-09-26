package com.proyecto.servicios.entity.producto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

/**
 * Una fila por producto del catalogo de GestoPago (respaldo en base de
 * datos usado cuando Redis no esta disponible). Se reemplaza por
 * completo en cada sincronizacion diaria (borrar todo + insertar todo).
 *
 * Implementa Persistable para forzar a Spring Data JPA a usar INSERT
 * directo (persist) en vez de hacer un SELECT de verificacion por cada
 * fila (merge) -- como el id viene asignado manualmente (no autogenerado),
 * sin esto Spring Data asume que la entidad podria ya existir y duplica
 * el trabajo. Como siempre borramos la tabla justo antes de insertar,
 * es seguro asumir que toda fila es nueva.
 */
@Entity
@Table(name = "catalogo_productos")
@Getter
@Setter
public class CatalogoProductosEntity implements Persistable<Integer> {

    @Id
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "id_servicio", nullable = false)
    private Integer idServicio;

    @Column(name = "id_cat_tipo_servicio")
    private Integer idCatTipoServicio;

    @Column(name = "tipo_front", nullable = false)
    private Integer tipoFront;

    @Column(name = "servicio", length = 256)
    private String servicio;

    @Column(name = "producto", length = 256)
    private String producto;

    @Column(name = "precio", length = 20)
    private String precio;

    @Column(name = "tipo_referencia", length = 5)
    private String tipoReferencia;

    @Column(name = "legend", columnDefinition = "TEXT")
    private String legend;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Transient
    private boolean nueva = true;

    @Override
    public Integer getId() {
        return idProducto;
    }

    @Override
    public boolean isNew() {
        return nueva;
    }
}