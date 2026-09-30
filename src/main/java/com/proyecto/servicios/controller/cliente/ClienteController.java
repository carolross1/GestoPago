package com.proyecto.servicios.controller.cliente;

import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClienteRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.service.cliente.ClienteService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ClienteActualizaRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        clienteService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/curp/{curp}")
    public ResponseEntity<ClienteResponse> obtenerPorCurp(@PathVariable String curp) {
        return ResponseEntity.ok(clienteService.obtenerPorCurp(curp));
    }

    @GetMapping("/rfc/{rfc}")
    public ResponseEntity<ClienteResponse> obtenerPorRfc(@PathVariable String rfc) {
        return ResponseEntity.ok(clienteService.obtenerPorRfc(rfc));
    }

    @GetMapping("/correo/{correo}")
    public ResponseEntity<ClienteResponse> obtenerPorCorreo(@PathVariable String correo) {
        return ResponseEntity.ok(clienteService.obtenerPorCorreo(correo));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ClienteResponse>> listarActivos() {
        return ResponseEntity.ok(clienteService.listarActivos());
    }

    @GetMapping("/rango-fechas")
    public ResponseEntity<List<ClienteResponse>> listarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(clienteService.listarPorRangoFechas(desde, hasta));
    }
}
