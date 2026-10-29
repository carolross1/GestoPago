package com.proyecto.servicios.model.transaccion;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Datos de la compra que se envian por correo al cliente como comprobante. */
@Data
@Builder
public class ComprobantePagoCorreo {

    private String correoDestino;
    private String nombreCliente;
    private String numeroCuenta;
    private String servicio;
    private String producto;
    private String urlLogo;
    private String destino;
    private BigDecimal monto;
    private BigDecimal saldoRestante;
    private String idTx;
    private String numAutorizacion;
    private String upc;
    private LocalDateTime fechaHora;
}
