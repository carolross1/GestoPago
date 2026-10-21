package com.proyecto.servicios.exception.cliente;

/**
 * Regla de negocio: un cliente dado de baja no se puede modificar ni volver
 * a dar de baja. Se responde con 409 (conflicto con el estado actual).
 */
public class ClienteInactivoException extends RuntimeException {
    public ClienteInactivoException(Long id, String accion) {
        super("El cliente con id " + id + " esta dado de baja y no se puede " + accion);
    }
}
