package com.proyecto.servicios.repositorys.cliente;

import com.proyecto.servicios.entity.cliente.Domicilio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DomicilioRepository extends JpaRepository<Domicilio, Long> {

    Optional<Domicilio> findByClienteId(Long clienteId);
}
