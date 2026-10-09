package com.proyecto.servicios.exception.cliente;

/**
 * El correo con el que se intenta iniciar sesion (o registrar acceso)
 * no pertenece a ningun cliente. Se responde con 404.
 */
public class CorreoNoEncontradoException extends ClienteNoEncontradoException {
    public CorreoNoEncontradoException() {
        super("El correo no fue encontrado, verifica que este bien escrito o que el cliente este dado de alta");
    }
}
