package com.proyecto.servicios.service.cliente;

import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClienteRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ClienteService {

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(Long id, ClienteActualizaRequest request);

    ClienteResponse obtenerPorId(Long id);

    List<ClienteResponse> listarTodos();

    ClienteResponse obtenerPorCurp(String curp);

    ClienteResponse obtenerPorRfc(String rfc);

    ClienteResponse obtenerPorCorreo(String correo);

    List<ClienteResponse> listarActivos();

    List<ClienteResponse> listarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta);

    void darDeBaja(Long id);

    ClienteResponse obtenerPorNumeroCuenta(String numeroCuenta);
}
