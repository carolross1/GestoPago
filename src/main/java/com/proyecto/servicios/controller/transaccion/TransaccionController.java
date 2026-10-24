package com.proyecto.servicios.controller.transaccion;

import com.proyecto.servicios.model.comun.RespuestaApi;
import com.proyecto.servicios.model.transaccion.PagoRequest;
import com.proyecto.servicios.model.transaccion.PagoResponse;
import com.proyecto.servicios.service.transaccion.TransaccionService;
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

@Tag(name = "Transacciones", description = "Pagos y transferencias vía GestoPago (sendTx) con 2FA y validación de saldo")
@RestController
@RequestMapping("/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @Operation(
            summary = "Realizar pago o transferencia (vía GestoPago sendTx)",
            description = "Ejecuta una transacción diferenciada por bandera (tipoDestino: TELEFONO o TRANSFERENCIA). "
                    + "Valida que el cliente esté activo, cuente con saldo suficiente y valide el 2FA. "
                    + "Cifra el payload con AES-256-CBC, obtiene token y llama a GestoPago. Si es exitoso, descuenta el saldo."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago procesado y saldo descontado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Campos inválidos en la solicitud o datos incompletos según la bandera",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "401", description = "Código 2FA inválido o expirado",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente en la cuenta o cliente/cuenta inactivos",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "422", description = "Transacción rechazada por el proveedor GestoPago",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class)))
    })
    @PostMapping("/pago")
    public ResponseEntity<RespuestaApi<PagoResponse>> realizarPago(@Valid @RequestBody PagoRequest request) {
        PagoResponse response = transaccionService.procesarPago(request);
        return Respuestas.ok("Transacción realizada exitosamente", response);
    }
}
