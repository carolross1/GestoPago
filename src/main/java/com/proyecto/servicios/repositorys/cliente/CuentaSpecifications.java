package com.proyecto.servicios.repositorys.cliente;

import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.model.cliente.CuentaFiltroRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Consulta dinamica de cuentas: solo se agregan los filtros que vienen llenos. */
public final class CuentaSpecifications {

    private CuentaSpecifications() {
    }

    public static Specification<Cuenta> conFiltros(CuentaFiltroRequest f) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (f.getNumeroCuenta() != null) condiciones.add(cb.equal(root.get("numeroCuenta"), f.getNumeroCuenta()));
            if (f.getClienteId() != null) condiciones.add(cb.equal(root.get("clienteId"), f.getClienteId()));
            if (f.getEstatus() != null) condiciones.add(cb.equal(root.get("estatus"), f.getEstatus()));
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }
}
