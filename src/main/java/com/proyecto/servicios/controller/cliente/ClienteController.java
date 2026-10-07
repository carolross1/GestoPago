package com.proyecto.servicios.controller.cliente;

import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClienteRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.service.cliente.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Las respuestas de error de cada endpoint se documentan con @ApiResponses
 * para que Swagger muestre su descripcion y un ejemplo del cuerpo en vez
 * de "Undocumented". El 401 (falta token / sesion expirada) lo agrega
 * OpenApi.java automaticamente a todos los endpoints que exigen token.
 */
@Tag(name = "Clientes", description = "Alta, consulta, actualizacion y baja de clientes")
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Alta de cliente (no requiere token)",
            description = "Crea el cliente, su domicilio y una cuenta con saldo inicial.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Algun campo no cumple las validaciones",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class), examples = {
                            @ExampleObject(name = "Campo invalido", value = EjemplosError.VALIDACION),
                            @ExampleObject(name = "Fecha invalida", value = EjemplosError.FECHA),
                            @ExampleObject(name = "Menor de edad", value = EjemplosError.MENOR_EDAD)})),
            @ApiResponse(responseCode = "409", description = "Ya existe un cliente con esa CURP, RFC o correo",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class), examples = {
                            @ExampleObject(name = "CURP duplicada", value = EjemplosError.CURP_DUPLICADA),
                            @ExampleObject(name = "Correo duplicado", value = EjemplosError.CORREO_DUPLICADO)}))
    })
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @Operation(summary = "Listar todos los clientes")
    @ApiResponse(responseCode = "200", description = "Lista de clientes (puede venir vacia)")
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @Operation(summary = "Consultar cliente por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con ese id",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class),
                            examples = @ExampleObject(value = EjemplosError.CLIENTE_NO_ENCONTRADO)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @Operation(summary = "Actualizar cliente",
            description = "Solo se modifican los campos enviados. CURP, RFC y numero de cuenta no se pueden cambiar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado"),
            @ApiResponse(responseCode = "400", description = "Algun campo no cumple las validaciones",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class),
                            examples = @ExampleObject(value = EjemplosError.VALIDACION))),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con ese id",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class),
                            examples = @ExampleObject(value = EjemplosError.CLIENTE_NO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "El nuevo correo ya lo usa otro cliente",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class),
                            examples = @ExampleObject(value = EjemplosError.CORREO_DUPLICADO)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ClienteActualizaRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @Operation(summary = "Dar de baja un cliente (baja logica)", description = "Tambien desactiva su cuenta.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente dado de baja"),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con ese id",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class),
                            examples = @ExampleObject(value = EjemplosError.CLIENTE_NO_ENCONTRADO)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        clienteService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Consultar cliente por CURP")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con esa CURP",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class)))
    })
    @GetMapping("/curp/{curp}")
    public ResponseEntity<ClienteResponse> obtenerPorCurp(
            @Parameter(example = "PELJ990131HGTRPN09") @PathVariable String curp) {
        return ResponseEntity.ok(clienteService.obtenerPorCurp(curp));
    }

    @Operation(summary = "Consultar cliente por RFC")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con ese RFC",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class)))
    })
    @GetMapping("/rfc/{rfc}")
    public ResponseEntity<ClienteResponse> obtenerPorRfc(
            @Parameter(example = "PELJ990131AB1") @PathVariable String rfc) {
        return ResponseEntity.ok(clienteService.obtenerPorRfc(rfc));
    }

    @Operation(summary = "Consultar cliente por correo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con ese correo",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class)))
    })
    @GetMapping("/correo/{correo}")
    public ResponseEntity<ClienteResponse> obtenerPorCorreo(
            @Parameter(example = "juan.perez@correo.com") @PathVariable String correo) {
        return ResponseEntity.ok(clienteService.obtenerPorCorreo(correo));
    }

    @Operation(summary = "Listar clientes activos")
    @ApiResponse(responseCode = "200", description = "Lista de clientes activos (puede venir vacia)")
    @GetMapping("/activos")
    public ResponseEntity<List<ClienteResponse>> listarActivos() {
        return ResponseEntity.ok(clienteService.listarActivos());
    }

    /**
     * Clientes registrados entre dos fechas (inclusive), formato yyyy/MM/dd.
     * Ejemplo: /clientes/rango-fechas?desde=2026/01/01&hasta=2026/12/31
     */
    @Operation(summary = "Listar clientes registrados entre dos fechas (inclusive)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de clientes (puede venir vacia)"),
            @ApiResponse(responseCode = "400", description = "Fechas con formato invalido o 'desde' posterior a 'hasta'",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class),
                            examples = @ExampleObject(value = EjemplosError.RANGO)))
    })
    @GetMapping("/rango-fechas")
    public ResponseEntity<List<ClienteResponse>> listarPorRangoFechas(
            @Parameter(description = "Formato yyyy/MM/dd", example = "2026/01/01")
            @RequestParam @DateTimeFormat(pattern = "yyyy/MM/dd") LocalDate desde,
            @Parameter(description = "Formato yyyy/MM/dd", example = "2026/12/31")
            @RequestParam @DateTimeFormat(pattern = "yyyy/MM/dd") LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new ValidacionNegocioException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
        return ResponseEntity.ok(clienteService.listarPorRangoFechas(desde.atStartOfDay(), hasta.atTime(LocalTime.MAX)));
    }
}
