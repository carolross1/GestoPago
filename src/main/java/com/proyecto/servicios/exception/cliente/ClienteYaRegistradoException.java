package com.proyecto.servicios.exception.cliente;

/**
 * Excepcion base para cualquier intento de registrar un cliente que ya
 * existe en el sistema. CurpDuplicadaException, RfcDuplicadoException y
 * CorreoDuplicadoException son casos especificos de esta (indican EN
 * QUE campo esta el conflicto), pero comparten este tipo comun para que
 * el codigo cliente pueda manejarlas de forma generica si lo necesita.
 */
public class ClienteYaRegistradoException extends RuntimeException {
    public ClienteYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
