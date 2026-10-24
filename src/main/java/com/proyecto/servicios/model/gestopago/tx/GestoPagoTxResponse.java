package com.proyecto.servicios.model.gestopago.tx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.proyecto.servicios.model.gestopago.producto.MensajeDTO;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "RESPONSE")
public class GestoPagoTxResponse {

    @JacksonXmlProperty(localName = "ID_TX")
    private String idTx;

    @JacksonXmlProperty(localName = "NUM_AUTORIZACION")
    private String numAutorizacion;

    @JacksonXmlProperty(localName = "SALDO")
    private String saldo;

    @JacksonXmlProperty(localName = "COMISION")
    private String comision;

    @JacksonXmlProperty(localName = "SALDO_F")
    private String saldoF;

    @JacksonXmlProperty(localName = "COMISION_F")
    private String comisionF;

    @JacksonXmlProperty(localName = "FECHA")
    private String fecha;

    @JacksonXmlProperty(localName = "MONTO")
    private String monto;

    @JacksonXmlProperty(localName = "MENSAJE")
    private MensajeDTO mensaje;
}
