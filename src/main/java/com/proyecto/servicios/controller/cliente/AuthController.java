package com.proyecto.servicios.controller.cliente;

import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.cliente.LoginRequest;
import com.proyecto.servicios.model.cliente.LoginResponse;
import com.proyecto.servicios.model.cliente.RegistroLoginRequest;
import com.proyecto.servicios.service.cliente.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login y registro de acceso al portal para clientes YA existentes
 * (dados de alta previamente por un ejecutivo via POST /clientes).
 * Estas rutas NUNCA pasan por SesionActivaFilter (ver SeguridadConfig).
 */
@Tag(name = "Autenticacion", description = "Registro de contrasena e inicio de sesion con correo")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registrar contrasena del portal para un cliente ya dado de alta")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contrasena registrada"),
            @ApiResponse(responseCode = "400", description = "Correo con formato invalido o contrasena menor a 8 caracteres",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))),
            @ApiResponse(responseCode = "404", description = "El correo no pertenece a ningun cliente",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class),
                            examples = @ExampleObject(value = EjemplosError.CORREO_NO_ENCONTRADO))),
            @ApiResponse(responseCode = "409", description = "Ese correo ya tiene contrasena registrada",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class),
                            examples = @ExampleObject(value = EjemplosError.LOGIN_DUPLICADO)))
    })
    @PostMapping("/registro")
    public ResponseEntity<Void> registrar(@Valid @RequestBody RegistroLoginRequest request) {
        authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Iniciar sesion con correo y contrasena (regresa el token JWT)")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto, copia el token y usalo en Authorize"),
            @ApiResponse(responseCode = "400", description = "El correo no tiene un formato valido",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))),
            @ApiResponse(responseCode = "401", description = "La contrasena es incorrecta",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class),
                            examples = @ExampleObject(value = EjemplosError.CONTRASENA))),
            @ApiResponse(responseCode = "404", description = "El correo no fue encontrado o aun no tiene contrasena",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class), examples = {
                            @ExampleObject(name = "Correo no encontrado", value = EjemplosError.CORREO_NO_ENCONTRADO),
                            @ExampleObject(name = "Sin contrasena", value = EjemplosError.SIN_CONTRASENA)}))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
