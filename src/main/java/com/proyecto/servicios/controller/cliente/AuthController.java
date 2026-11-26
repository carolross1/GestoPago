package com.proyecto.servicios.controller.cliente;

import com.proyecto.servicios.model.comun.RespuestaApi;
import com.proyecto.servicios.util.Respuestas;
import com.proyecto.servicios.model.cliente.LoginRequest;
import com.proyecto.servicios.model.cliente.LoginResponse;
import com.proyecto.servicios.model.cliente.RegistroLoginRequest;
import com.proyecto.servicios.service.cliente.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login y registro de acceso al portal para clientes YA existentes
 * en el sistema (dados de alta por PersonasController).
 */
@Tag(name = "03. Autenticación Portal", description = "Generación de contraseñas y obtención del token JWT para consumir el resto de la API")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registrar contraseña del portal para un cliente ya dado de alta")
    @SecurityRequirements
    @ApiResponse(responseCode = "201", description = "Contraseña registrada")
    @ApiResponse(responseCode = "400", description = "Correo con formato inválido o contraseña menor a 8 caracteres",
            content = @Content(schema = @Schema(implementation = RespuestaApi.class)))
    @ApiResponse(responseCode = "404", description = "El correo no pertenece a ningún cliente",
            content = @Content(schema = @Schema(implementation = RespuestaApi.class),
                    examples = @ExampleObject(value = EjemplosError.CORREO_NO_ENCONTRADO)))
    @ApiResponse(responseCode = "409", description = "Ese correo ya tiene contraseña registrada",
            content = @Content(schema = @Schema(implementation = RespuestaApi.class),
                    examples = @ExampleObject(value = EjemplosError.LOGIN_DUPLICADO)))
    @PostMapping("/registro")
    public ResponseEntity<RespuestaApi<Void>> registrar(@Valid @RequestBody RegistroLoginRequest request) {
        authService.registrar(request);
        return Respuestas.creado("Contraseña registrada correctamente; ya puedes iniciar sesión", null);
    }

    @Operation(summary = "Iniciar sesión con correo y contraseña (regresa el token JWT)")
    @SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Login correcto, copia el token y úsalo en Authorize")
    @ApiResponse(responseCode = "400", description = "El correo no tiene un formato válido",
            content = @Content(schema = @Schema(implementation = RespuestaApi.class)))
    @ApiResponse(responseCode = "401", description = "La contraseña es incorrecta",
            content = @Content(schema = @Schema(implementation = RespuestaApi.class),
                    examples = @ExampleObject(value = EjemplosError.CONTRASENA)))
    @ApiResponse(responseCode = "404", description = "El correo no fue encontrado o aún no tiene contraseña",
            content = @Content(schema = @Schema(implementation = RespuestaApi.class), examples = {
                    @ExampleObject(name = "Correo no encontrado", value = EjemplosError.CORREO_NO_ENCONTRADO),
                    @ExampleObject(name = "Sin contraseña", value = EjemplosError.SIN_CONTRASENA)}))
    @PostMapping("/login")
    public ResponseEntity<RespuestaApi<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return Respuestas.ok("Inicio de sesión correcto", authService.login(request));
    }
}
