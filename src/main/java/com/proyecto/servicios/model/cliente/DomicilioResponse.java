package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Domicilio tal como se regresa en las respuestas (DTO de salida). */
@Data
public class DomicilioResponse {
    @Schema(example = "Hidalgo")
    private String calle;
    @Schema(example = "120")
    private Integer numeroExterior;
    @Schema(example = "4")
    private Integer numeroInterior;
    @Schema(example = "Centro")
    private String colonia;
    @Schema(example = "Dolores Hidalgo")
    private String municipio;
    @Schema(example = "Guanajuato")
    private String estado;
    @Schema(example = "37800")
    private Integer codigoPostal;
    @Schema(example = "Mexico")
    private String pais;
}
