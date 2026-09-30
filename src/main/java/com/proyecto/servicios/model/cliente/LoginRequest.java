package com.proyecto.servicios.model.cliente;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String rfc;

    @NotBlank
    private String password;
}
