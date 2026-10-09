package com.proyecto.servicios.exception.cliente;

public class LoginYaRegistradoException extends RuntimeException {
    public LoginYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
