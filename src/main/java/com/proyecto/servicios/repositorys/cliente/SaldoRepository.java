package com.proyecto.servicios.repositorys.cliente;

import com.proyecto.servicios.entity.cliente.Saldo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaldoRepository extends JpaRepository<Saldo, Long> {

    Optional<Saldo> findFirstByCuentaIdOrderByFechaDesc(Long cuentaId);

    List<Saldo> findByCuentaIdOrderByFechaDesc(Long cuentaId);
}
