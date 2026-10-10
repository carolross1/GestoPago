package com.proyecto.servicios.exception.cliente;

public class CorreoDuplicadoException extends ClienteYaRegistradoException {
    public CorreoDuplicadoException(String correo) {
        super("Ya existe un cliente registrado con el correo proporcionado");
    }
}
