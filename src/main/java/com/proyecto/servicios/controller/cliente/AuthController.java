package com.proyecto.servicios.controller.cliente;

import com.proyecto.servicios.model.cliente.LoginRequest;
import com.proyecto.servicios.model.cliente.LoginResponse;
import com.proyecto.servicios.model.cliente.RegistroLoginRequest;
import com.proyecto.servicios.service.cliente.AuthService;
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
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<Void> registrar(@Valid @RequestBody RegistroLoginRequest request) {
        authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
