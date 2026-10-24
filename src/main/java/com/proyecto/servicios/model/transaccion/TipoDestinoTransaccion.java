package com.proyecto.servicios.model.transaccion;

/**
 * Bandera solicitada para diferenciar transacciones:
 * - TELEFONO: Pago / recarga referenciada a número de teléfono (10 dígitos).
 * - TRANSFERENCIA: Pago de servicio o transferencia referenciada a código/recibo.
 */
public enum TipoDestinoTransaccion {
    TELEFONO,
    TRANSFERENCIA
}
