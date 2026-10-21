package com.proyecto.servicios.service.cliente;

import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.model.cliente.CuentaFiltroRequest;
import com.proyecto.servicios.model.cliente.CuentaResponse;
import com.proyecto.servicios.model.comun.PaginaResponse;

public interface CuentaService {

    Cuenta crearCuentaParaCliente(Long clienteId);

    /** Busqueda dinamica: combina los filtros que vengan llenos. */
    PaginaResponse<CuentaResponse> buscar(CuentaFiltroRequest filtro);

    void desactivarCuentaDeCliente(Long clienteId);
}
