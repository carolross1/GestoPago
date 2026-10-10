package com.proyecto.servicios.repositorys.catalogo;

import com.proyecto.servicios.entity.catalogo.Nacionalidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NacionalidadRepository extends JpaRepository<Nacionalidad, Long> {

    Optional<Nacionalidad> findByCodigo(String codigo);

    Optional<Nacionalidad> findByCodigoAndActivoTrue(String codigo);

    List<Nacionalidad> findByActivoTrueOrderByNombreAsc();
}
