package com.proyecto.servicios.repositorys.producto;

import com.proyecto.servicios.entity.producto.CatalogoProductosEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogoProductosRepository extends JpaRepository<CatalogoProductosEntity, Long> {
}
