package com.proyecto.servicios.exception;

/**
 * El servicio externo no respondio dentro del tiempo configurado
 * (connect-timeout / read-timeout) o hubo un error de comunicacion/red.
 */
public class ExternalServiceTimeoutException extends ExternalServiceException {
    public ExternalServiceTimeoutException(String servicio, String mensaje) {
        super(servicio, mensaje);
    }
}
