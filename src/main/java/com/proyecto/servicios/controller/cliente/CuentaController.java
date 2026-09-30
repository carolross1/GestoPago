package com.proyecto.servicios.controller.cliente;

import com.proyecto.servicios.model.cliente.CuentaResponse;
import com.proyecto.servicios.model.cliente.SaldoResponse;
import com.proyecto.servicios.service.cliente.CuentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponse> obtenerPorNumeroCuenta(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtenerPorNumeroCuenta(numeroCuenta));
    }

    @GetMapping("/{numeroCuenta}/saldo")
    public ResponseEntity<SaldoResponse> obtenerSaldo(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtenerSaldo(numeroCuenta));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CuentaResponse>> listarActivas() {
        return ResponseEntity.ok(cuentaService.listarActivas());
    }
}
