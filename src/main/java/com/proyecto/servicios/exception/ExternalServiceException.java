package com.proyecto.servicios.exception;

import lombok.Getter;

/**
 * Excepcion generica para fallos al invocar un servicio externo:
 * respuestas no exitosas, errores de red, o cualquier fallo no controlado.
 */
@Getter
public class ExternalServiceException extends RuntimeException {

    private final String servicio;

    public ExternalServiceException(String servicio, String mensaje) {
        super(mensaje);
        this.servicio = servicio;
    }

    public ExternalServiceException(String servicio, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.servicio = servicio;
    }
}
