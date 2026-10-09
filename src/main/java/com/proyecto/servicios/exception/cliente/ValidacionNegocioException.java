package com.proyecto.servicios.exception.cliente;

/**
 * Reglas de negocio que no son validaciones de formato de campo
 * (esas las cubre Bean Validation), como la mayoria de edad.
 */
public class ValidacionNegocioException extends RuntimeException {
    public ValidacionNegocioException(String mensaje) {
        super(mensaje);
    }
}
