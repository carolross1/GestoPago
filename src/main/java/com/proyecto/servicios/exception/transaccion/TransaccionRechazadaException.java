package com.proyecto.servicios.exception.transaccion;

public class TransaccionRechazadaException extends RuntimeException {

    private final String codigoProveedor;

    public TransaccionRechazadaException(String codigoProveedor, String mensaje) {
        super(String.format("La transacción fue rechazada por GestoPago [Código: %s]: %s",
                codigoProveedor != null ? codigoProveedor : "DESCONOCIDO", mensaje));
        this.codigoProveedor = codigoProveedor;
    }

    public String getCodigoProveedor() {
        return codigoProveedor;
    }
}
