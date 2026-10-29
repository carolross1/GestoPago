package com.proyecto.servicios.controller.transaccion;

import com.proyecto.servicios.model.comun.RespuestaApi;
import com.proyecto.servicios.model.transaccion.PagoRequest;
import com.proyecto.servicios.model.transaccion.PagoResponse;
import com.proyecto.servicios.model.transaccion.Solicitar2faRequest;
import com.proyecto.servicios.model.transaccion.Solicitar2faResponse;
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

@Tag(name = "Transacciones", description = "Pagos y transferencias vía GestoPago (sendTx) con 2FA por correo y validación de saldo")
@RestController
@RequestMapping("/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @Operation(summary = "Paso 1: Solicitar código 2FA por correo",
            description = "Genera un código de 6 dígitos con vigencia limitada (Redis, o memoria si Redis no está disponible) "
                    + "y lo envía al correo registrado del cliente dueño de la cuenta. El código es de un solo uso.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Código generado y enviado al correo"),
            @ApiResponse(responseCode = "400", description = "Número de cuenta con formato inválido",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "409", description = "Cliente dado de baja",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "422", description = "Cuenta inactiva o cliente sin correo registrado",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class)))
    })
    @PostMapping("/2fa")
    public ResponseEntity<RespuestaApi<Solicitar2faResponse>> solicitar2fa(@Valid @RequestBody Solicitar2faRequest request) {
        return Respuestas.ok("Código 2FA enviado al correo", transaccionService.solicitarCodigo2fa(request));
    }

    @Operation(summary = "Paso 2: Realizar pago o transferencia (vía GestoPago sendTx)",
            description = "Ejecuta una transacción diferenciada por bandera (tipoDestino: TELEFONO o TRANSFERENCIA). "
                    + "Valida cliente activo, saldo suficiente y el código 2FA recibido por correo. "
                    + "Cifra el payload con AES-256-CBC, agrega el token de GestoPago y llama a sendTx. "
                    + "Si el proveedor responde con éxito, descuenta el saldo.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago procesado y saldo descontado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Campos inválidos en la solicitud o datos incompletos según la bandera",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "401", description = "Código 2FA incorrecto, usado o expirado",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente o cliente dado de baja",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class))),
            @ApiResponse(responseCode = "422", description = "Cuenta inactiva o transacción rechazada por GestoPago",
                    content = @Content(schema = @Schema(implementation = RespuestaApi.class)))
    })
    @PostMapping("/pago")
    public ResponseEntity<RespuestaApi<PagoResponse>> realizarPago(@Valid @RequestBody PagoRequest request) {
        return Respuestas.ok("Transacción realizada exitosamente", transaccionService.procesarPago(request));
    }
}
