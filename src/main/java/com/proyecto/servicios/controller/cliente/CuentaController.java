package com.proyecto.servicios.controller.cliente;

import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.cliente.CuentaResponse;
import com.proyecto.servicios.model.cliente.SaldoResponse;
import com.proyecto.servicios.service.cliente.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Cuentas", description = "Consulta de cuentas y saldos")
@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Operation(summary = "Consultar cuenta por numero")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese numero",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class),
                            examples = @ExampleObject(value = EjemplosError.CUENTA_NO_ENCONTRADA)))
    })
    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponse> obtenerPorNumeroCuenta(
            @Parameter(example = "1234567890") @PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtenerPorNumeroCuenta(numeroCuenta));
    }

    @Operation(summary = "Consultar saldo actual de una cuenta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo actual con 2 decimales"),
            @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese numero",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class),
                            examples = @ExampleObject(value = EjemplosError.CUENTA_NO_ENCONTRADA)))
    })
    @GetMapping("/{numeroCuenta}/saldo")
    public ResponseEntity<SaldoResponse> obtenerSaldo(
            @Parameter(example = "1234567890") @PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtenerSaldo(numeroCuenta));
    }

    @Operation(summary = "Listar cuentas activas")
    @ApiResponse(responseCode = "200", description = "Lista de cuentas activas (puede venir vacia)")
    @GetMapping("/activas")
    public ResponseEntity<List<CuentaResponse>> listarActivas() {
        return ResponseEntity.ok(cuentaService.listarActivas());
    }
}
