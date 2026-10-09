package com.proyecto.servicios.repositorys.cliente;

import com.proyecto.servicios.entity.cliente.Login;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<Login, Long> {

    Optional<Login> findByClienteId(Long clienteId);

    boolean existsByClienteId(Long clienteId);
}
