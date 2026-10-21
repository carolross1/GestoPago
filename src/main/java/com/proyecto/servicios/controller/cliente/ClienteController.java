package com.proyecto.servicios.controller.cliente;

import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClienteFiltroRequest;
import com.proyecto.servicios.model.cliente.ClienteIdRequest;
import com.proyecto.servicios.model.cliente.ClienteRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.model.comun.PaginaResponse;
import com.proyecto.servicios.model.comun.RespuestaApi;
import com.proyecto.servicios.service.cliente.ClienteService;
import com.proyecto.servicios.util.Respuestas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administracion de clientes (usuarios del sistema).
 *
 * Reglas que cumple este controlador:
 * - Sin @PathVariable ni @RequestParam: todo llega en el cuerpo (@RequestBody).
 * - @Valid en cada Request DTO; no hay validaciones manuales ni try-catch:
 *   los errores los procesa GlobalExceptionHandler.
 * - Recibe Request DTO y regresa Response DTO dentro de RespuestaApi
 *   (nunca entidades JPA).
 * - Un unico endpoint de busqueda (POST /clientes/buscar) con filtros
 *   combinables, en lugar de un endpoint por campo.
 *
 * El 401 (falta token / sesion expirada) lo agrega OpenApi.java a todos
 * los endpoints que exigen token.
 */
@Tag(name = "Clientes", description = "Alta, busqueda, actualizacion y baja de clientes")
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Alta de cliente (no requiere token)",
            description = "Crea el cliente, su domicilio y una cuenta con saldo inicial. "
                    + "id, activo, fechaRegistro y numeroCuenta los asigna el servidor y no se pueden enviar.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Campos invalidos, con formato incorrecto o no permitidos",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class), examples = {
                            @ExampleObject(name = "Campos invalidos", value = EjemplosError.VALIDACION),
                            @ExampleObject(name = "Campo no permitido", value = EjemplosError.CAMPO_PROHIBIDO),
                            @ExampleObject(name = "Fecha invalida", value = EjemplosError.FECHA)})),
            @ApiResponse(responseCode = "409", description = "Ya existe un cliente con esa CURP, RFC o correo",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class), examples = {
                            @ExampleObject(name = "CURP duplicada", value = EjemplosError.CURP_DUPLICADA),
                            @ExampleObject(name = "Correo duplicado", value = EjemplosError.CORREO_DUPLICADO)})),
            @ApiResponse(responseCode = "422", description = "No cumple una regla de negocio",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class), examples = {
                            @ExampleObject(name = "Menor de edad", value = EjemplosError.MENOR_EDAD),
                            @ExampleObject(name = "Nacionalidad fuera de catalogo", value = EjemplosError.NACIONALIDAD)}))
    })
    @PostMapping
    public ResponseEntity<RespuestaApi<ClienteResponse>> crear(@Valid @RequestBody ClienteRequest request) {
        return Respuestas.creado("Cliente registrado correctamente", clienteService.crear(request));
    }

    @Operation(summary = "Buscar clientes (endpoint unico de consulta)",
            description = "Todos los filtros son opcionales y se combinan con AND. "
                    + "Ejemplos: {} = todos; {\"id\": 3}; {\"curp\": \"...\"}; "
                    + "{\"activo\": true, \"nacionalidad\": \"MEX\", \"fechaRegistroDesde\": \"2026/10/01\"}. "
                    + "Los textos se comparan exactos porque se guardan cifrados. Resultado paginado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagina de resultados (puede venir vacia)"),
            @ApiResponse(responseCode = "400", description = "Algun filtro tiene un formato invalido o un rango incoherente",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class),
                            examples = @ExampleObject(value = EjemplosError.RANGO)))
    })
    @PostMapping("/buscar")
    public ResponseEntity<RespuestaApi<PaginaResponse<ClienteResponse>>> buscar(
            @Valid @RequestBody ClienteFiltroRequest filtro) {
        PaginaResponse<ClienteResponse> pagina = clienteService.buscar(filtro);
        return Respuestas.ok("Se encontraron " + pagina.getTotalElementos() + " cliente(s)", pagina);
    }

    @Operation(summary = "Actualizar cliente",
            description = "El id va en el cuerpo junto con los campos a modificar; solo cambian los campos enviados. "
                    + "CURP, RFC, fecha de nacimiento, activo, fechaRegistro y numeroCuenta no se pueden modificar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado"),
            @ApiResponse(responseCode = "400", description = "Falta el id, hay campos invalidos o no permitidos",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class), examples = {
                            @ExampleObject(name = "Campos invalidos", value = EjemplosError.VALIDACION),
                            @ExampleObject(name = "Campo no permitido", value = EjemplosError.CAMPO_PROHIBIDO)})),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con ese id",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class),
                            examples = @ExampleObject(value = EjemplosError.CLIENTE_NO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "Correo usado por otro cliente o cliente dado de baja",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class), examples = {
                            @ExampleObject(name = "Correo duplicado", value = EjemplosError.CORREO_DUPLICADO),
                            @ExampleObject(name = "Cliente dado de baja", value = EjemplosError.CLIENTE_INACTIVO)})),
            @ApiResponse(responseCode = "422", description = "Nacionalidad fuera de catalogo",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class),
                            examples = @ExampleObject(value = EjemplosError.NACIONALIDAD)))
    })
    @PutMapping
    public ResponseEntity<RespuestaApi<ClienteResponse>> actualizar(@Valid @RequestBody ClienteActualizaRequest request) {
        return Respuestas.ok("Cliente actualizado correctamente", clienteService.actualizar(request));
    }

    @Operation(summary = "Dar de baja un cliente (baja logica)",
            description = "Cuerpo: {\"id\": 3}. Marca al cliente como inactivo y desactiva su cuenta; no borra datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente dado de baja; regresa el cliente con activo=false"),
            @ApiResponse(responseCode = "400", description = "Falta el id o no es valido",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con ese id",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class),
                            examples = @ExampleObject(value = EjemplosError.CLIENTE_NO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "El cliente ya estaba dado de baja",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class)))
    })
    @PatchMapping("/baja")
    public ResponseEntity<RespuestaApi<ClienteResponse>> darDeBaja(@Valid @RequestBody ClienteIdRequest request) {
        return Respuestas.ok("Cliente dado de baja correctamente", clienteService.darDeBaja(request.getId()));
    }
}
