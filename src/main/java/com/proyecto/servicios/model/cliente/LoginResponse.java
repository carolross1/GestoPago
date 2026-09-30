package com.proyecto.servicios.model.cliente;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tipo = "Bearer";
    private long expiracionMs;

    public LoginResponse(String token, long expiracionMs) {
        this.token = token;
        this.expiracionMs = expiracionMs;
    }
}
