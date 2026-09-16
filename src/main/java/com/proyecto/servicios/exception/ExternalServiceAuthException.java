package com.proyecto.servicios.exception;

/**
 * El servicio externo rechazo la peticion por credenciales invalidas,
 * token vencido o no autorizado (HTTP 401/403), o no hay token
 * disponible localmente para autenticar la llamada.
 */
public class ExternalServiceAuthException extends ExternalServiceException {
    public ExternalServiceAuthException(String servicio, String mensaje) {
        super(servicio, mensaje);
    }
}
