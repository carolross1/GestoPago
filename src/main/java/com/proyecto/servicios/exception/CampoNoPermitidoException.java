package com.proyecto.servicios.exception;

import lombok.Getter;

/**
 * El JSON de la solicitud trae un campo que no se puede enviar: lo controla
 * el servidor (id, activo, fechaRegistro, numeroCuenta...) o no existe.
 */
@Getter
public class CampoNoPermitidoException extends RuntimeException {

    private final String campo;

    public CampoNoPermitidoException(String campo) {
        super("El campo '" + campo + "' no esta permitido en esta solicitud: "
                + "no existe o es un valor que asigna el servidor");
        this.campo = campo;
    }
}
