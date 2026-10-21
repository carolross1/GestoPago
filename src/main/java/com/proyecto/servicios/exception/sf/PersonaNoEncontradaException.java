package com.proyecto.servicios.exception.sf;

public class PersonaNoEncontradaException extends RuntimeException {
    public PersonaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
