package com.proyecto.servicios.repositorys.producto;

import com.proyecto.servicios.entity.producto.CatalogoProductosEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatalogoProductosRepository extends JpaRepository<CatalogoProductosEntity, Long> {

    Optional<CatalogoProductosEntity> findByIdProducto(Integer idProducto);
}
