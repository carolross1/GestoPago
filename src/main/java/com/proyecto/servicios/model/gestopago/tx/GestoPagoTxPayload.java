package com.proyecto.servicios.model.gestopago.tx;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GestoPagoTxPayload {

    private Integer idProducto;
    private Integer idServicio;
    private String telefono;
    private String referencia;
    private BigDecimal montoPago;
    private String horaLocal;
    private String upc;
    private String unidad;
}
