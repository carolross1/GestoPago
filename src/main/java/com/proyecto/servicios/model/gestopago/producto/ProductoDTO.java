package com.proyecto.servicios.model.gestopago.producto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * Un <producto .../> dentro de <PRODUCTOS>. Todos los campos del XML
 * vienen como atributos, no como elementos hijos.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoDTO {

    @JacksonXmlProperty(isAttribute = true, localName = "servicio")
    private String servicio;

    @JacksonXmlProperty(isAttribute = true, localName = "producto")
    private String producto;

    @JacksonXmlProperty(isAttribute = true, localName = "idServicio")
    private Integer idServicio;

    @JacksonXmlProperty(isAttribute = true, localName = "idProducto")
    private Integer idProducto;

    @JacksonXmlProperty(isAttribute = true, localName = "idCatTipoServicio")
    private Integer idCatTipoServicio;

    @JacksonXmlProperty(isAttribute = true, localName = "tipoFront")
    private Integer tipoFront;

    @JacksonXmlProperty(isAttribute = true, localName = "precio")
    private String precio;

    @JacksonXmlProperty(isAttribute = true, localName = "tipoReferencia")
    private String tipoReferencia;

    @JacksonXmlProperty(localName = "legend")
    private String legend;
}
