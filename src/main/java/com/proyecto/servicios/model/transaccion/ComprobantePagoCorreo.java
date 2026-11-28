package com.proyecto.servicios.model.transaccion;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Datos de la operación (aprobada o rechazada) que se envían por correo al cliente como ticket/comprobante. */
@Data
@Builder
public class ComprobantePagoCorreo {

    private String correoDestino;
    private String nombreCliente;
    private String numeroCuenta;
    private String servicio;
    private String producto;
    private Integer idServicio;
    private Integer idProducto;
    private String idDispositivo;
    private String urlLogo;
    private String destino;
    private BigDecimal monto;
    private BigDecimal saldoRestante;
    private String idTx;
    private String numAutorizacion;
    private String upc;
    private String horaLocal;
    private String leyenda;
    @Builder.Default
    private boolean aprobada = true;
    private String codigoRespuesta;
    private String mensajeOperacion;
    private LocalDateTime fechaHora;
}
