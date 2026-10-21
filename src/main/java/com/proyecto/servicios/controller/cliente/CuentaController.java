package com.proyecto.servicios.controller.cliente;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta de cuentas con un unico endpoint de busqueda y filtros en el
 * cuerpo. El 401 lo agrega OpenApi.java a todos los endpoints con token.
 */
@Tag(name = "Cuentas", description = "Consulta de cuentas y su saldo actual")
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
}
