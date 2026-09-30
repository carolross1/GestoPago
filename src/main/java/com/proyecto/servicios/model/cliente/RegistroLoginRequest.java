package com.proyecto.servicios.model.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Registro de credenciales de portal para un cliente que YA existe
 * (dado de alta previamente por un ejecutivo via POST /clientes).
 */
@Data
public class RegistroLoginRequest {

    @NotBlank
    private String rfc;

    @NotBlank
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    private String password;
}
