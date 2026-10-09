package com.proyecto.servicios.repositorys.cliente;

import com.proyecto.servicios.entity.cliente.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SesionRepository extends JpaRepository<Sesion, Long> {

    Optional<Sesion> findByToken(String token);
}
