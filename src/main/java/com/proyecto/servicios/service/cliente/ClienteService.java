package com.proyecto.servicios.service.cliente;

import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClienteFiltroRequest;
import com.proyecto.servicios.model.cliente.ClienteRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.model.comun.PaginaResponse;

public interface ClienteService {

    ClienteResponse crear(ClienteRequest request);

    /** El id del cliente viene dentro del request (no en la URL). */
    ClienteResponse actualizar(ClienteActualizaRequest request);

    /** Busqueda dinamica por cualquier combinacion de filtros. */
    PaginaResponse<ClienteResponse> buscar(ClienteFiltroRequest filtro);

    /** Baja logica; regresa el cliente ya con activo=false. */
    ClienteResponse darDeBaja(Long id);
}
