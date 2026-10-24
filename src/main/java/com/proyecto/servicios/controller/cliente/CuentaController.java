package com.proyecto.servicios.controller.cliente;

import com.proyecto.servicios.model.cliente.AbonoRequest;
import com.proyecto.servicios.model.cliente.AbonoResponse;
import com.proyecto.servicios.model.cliente.CuentaFiltroRequest;
import com.proyecto.servicios.model.cliente.CuentaResponse;
import com.proyecto.servicios.model.comun.PaginaResponse;
import com.proyecto.servicios.model.comun.RespuestaApi;
import com.proyecto.servicios.service.cliente.CuentaService;
import com.proyecto.servicios.util.Respuestas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta y administracion de saldo de cuentas con endpoints en el
 * cuerpo (sin @PathVariable ni @RequestParam).
 */
@Tag(name = "Cuentas", description = "Consulta de cuentas, saldo actual y abonos")
@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Operation(summary = "Buscar cuentas (endpoint unico de consulta)",
            description = "Filtros opcionales y combinables: numeroCuenta, clienteId, estatus. "
                    + "Cada cuenta incluye su saldo actual con 2 decimales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagina de resultados (puede venir vacia)"),
            @ApiResponse(responseCode = "400", description = "Algun filtro tiene un formato invalido",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class)))
    })
    @PostMapping("/buscar")
    public ResponseEntity<RespuestaApi<PaginaResponse<CuentaResponse>>> buscar(
            @Valid @RequestBody CuentaFiltroRequest filtro) {
        PaginaResponse<CuentaResponse> pagina = cuentaService.buscar(filtro);
        return Respuestas.ok("Se encontraron " + pagina.getTotalElementos() + " cuenta(s)", pagina);
    }

    @Operation(summary = "Abonar / depositar saldo a una cuenta",
            description = "Abona un monto en pesos a la cuenta especificada. "
                    + "Registra el movimiento en el historial y devuelve el saldo actualizado.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Abono aplicado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de abono inválidos (monto <= 0 o cuenta sin formato correcto)",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "404", description = "No existe la cuenta especificada",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "409", description = "La cuenta está inactiva y no puede recibir abonos",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class)))
    })
    @PostMapping("/abono")
    public ResponseEntity<RespuestaApi<AbonoResponse>> abonar(@Valid @RequestBody AbonoRequest request) {
        AbonoResponse response = cuentaService.abonar(request);
        return Respuestas.ok("Abono aplicado correctamente", response);
    }
}
