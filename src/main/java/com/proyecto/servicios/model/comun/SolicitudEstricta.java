package com.proyecto.servicios.model.comun;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.proyecto.servicios.exception.CampoNoPermitidoException;

/**
 * Base de los Request DTO. Si el JSON trae un campo que el DTO no declara
 * (por ejemplo "id", "activo", "fechaRegistro" o "numeroCuenta" en el alta),
 * la solicitud se rechaza con 400 en lugar de ignorarlo en silencio.
 *
 * Asi el cliente de la API no puede intentar modificar campos que controla
 * el servidor y se entera de inmediato si escribio mal un campo.
 */
public abstract class SolicitudEstricta {

    @JsonAnySetter
    public void rechazarCampoDesconocido(String campo, Object valor) {
        throw new CampoNoPermitidoException(campo);
    }
}
