package com.proyecto.servicios.exception.cliente;

/**
 * El correo si pertenece a un cliente, pero ese cliente todavia no ha
 * registrado su contrasena de portal (POST /auth/registro). Se responde con 404.
 */
public class LoginNoRegistradoException extends ClienteNoEncontradoException {
    public LoginNoRegistradoException() {
        super("El correo existe pero aun no tiene contrasena registrada, registrate primero en /auth/registro");
    }
}
