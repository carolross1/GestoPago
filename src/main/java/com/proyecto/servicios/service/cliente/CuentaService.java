package com.proyecto.servicios.service.cliente;

import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.model.cliente.CuentaResponse;
import com.proyecto.servicios.model.cliente.SaldoResponse;

import java.util.List;

public interface CuentaService {

    Cuenta crearCuentaParaCliente(Long clienteId);

    CuentaResponse obtenerPorNumeroCuenta(String numeroCuenta);

    SaldoResponse obtenerSaldo(String numeroCuenta);

    List<CuentaResponse> listarActivas();

    void desactivarCuentaDeCliente(Long clienteId);
}
