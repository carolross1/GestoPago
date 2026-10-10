package com.proyecto.servicios.exception.cliente;

public class RfcDuplicadoException extends ClienteYaRegistradoException {
    public RfcDuplicadoException(String rfc) {
        super("Ya existe un cliente registrado con el RFC proporcionado");
    }
}
