package com.proyecto.servicios.repositorys.cliente;

import com.proyecto.servicios.entity.cliente.Cliente;
import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.model.cliente.ClienteFiltroRequest;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Construye la consulta DINAMICA de clientes a partir de los filtros que
 * vengan llenos. Cada filtro agrega una condicion y todas se unen con AND,
 * asi un solo endpoint cubre cualquier combinacion de criterios.
 *
 * Las comparaciones contra columnas cifradas (nombre, CURP, RFC, correo,
 * etc.) funcionan porque Hibernate aplica el EncryptedStringConverter
 * tambien al parametro: el valor buscado se cifra igual que el guardado.
 */
public final class ClienteSpecifications {

    private ClienteSpecifications() {
    }

    public static Specification<Cliente> conFiltros(ClienteFiltroRequest f) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();

            if (f.getId() != null) condiciones.add(cb.equal(root.get("id"), f.getId()));
            if (f.getNombre() != null) condiciones.add(cb.equal(root.get("nombre"), f.getNombre()));
            if (f.getApellidoPaterno() != null) condiciones.add(cb.equal(root.get("apellidoPaterno"), f.getApellidoPaterno()));
            if (f.getApellidoMaterno() != null) condiciones.add(cb.equal(root.get("apellidoMaterno"), f.getApellidoMaterno()));
            if (f.getCurp() != null) condiciones.add(cb.equal(root.get("curp"), f.getCurp()));
            if (f.getRfc() != null) condiciones.add(cb.equal(root.get("rfc"), f.getRfc()));
            if (f.getCorreo() != null) condiciones.add(cb.equal(root.get("correo"), f.getCorreo()));
            if (f.getSexo() != null) condiciones.add(cb.equal(root.get("sexo"), f.getSexo()));
            if (f.getEstadoCivil() != null) condiciones.add(cb.equal(root.get("estadoCivil"), f.getEstadoCivil()));
            if (f.getNacionalidad() != null) condiciones.add(cb.equal(root.get("nacionalidad"), f.getNacionalidad()));
            if (f.getActivo() != null) condiciones.add(cb.equal(root.get("activo"), f.getActivo()));

            if (f.getFechaRegistroDesde() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaRegistro"),
                        f.getFechaRegistroDesde().atStartOfDay()));
            }
            if (f.getFechaRegistroHasta() != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("fechaRegistro"),
                        f.getFechaRegistroHasta().atTime(LocalTime.MAX)));
            }
            if (f.getIngresoMinimo() != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("ingresoMensual"), f.getIngresoMinimo()));
            }
            if (f.getIngresoMaximo() != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("ingresoMensual"), f.getIngresoMaximo()));
            }

            // El numero de cuenta vive en la tabla cuentas: subconsulta por cliente_id
            if (f.getNumeroCuenta() != null) {
                Subquery<Long> cuentas = query.subquery(Long.class);
                var cuenta = cuentas.from(Cuenta.class);
                cuentas.select(cuenta.get("clienteId"))
                        .where(cb.equal(cuenta.get("numeroCuenta"), f.getNumeroCuenta()));
                condiciones.add(root.get("id").in(cuentas));
            }

            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }
}
