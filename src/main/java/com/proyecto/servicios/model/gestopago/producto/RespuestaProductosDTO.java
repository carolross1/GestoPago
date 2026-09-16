package com.proyecto.servicios.model.gestopago.producto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "RESPONSE")
public class RespuestaProductosDTO {

    @JacksonXmlProperty(localName = "MENSAJE")
    private MensajeDTO mensaje;

    @JacksonXmlElementWrapper(localName = "PRODUCTOS")
    @JacksonXmlProperty(localName = "producto")
    private List<ProductoDTO> productos;
}
