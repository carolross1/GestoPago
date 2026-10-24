package com.proyecto.servicios.exception.transaccion;

public class Codigo2faInvalidoException extends RuntimeException {
    public Codigo2faInvalidoException(String mensaje) {
        super(mensaje);
    }
}
