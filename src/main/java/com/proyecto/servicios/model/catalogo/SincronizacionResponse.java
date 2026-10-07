package com.proyecto.servicios.model.catalogo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SincronizacionResponse {

    @Schema(description = "Paises recibidos de la API", example = "250")
    private int recibidos;

    @Schema(description = "Nacionalidades nuevas agregadas al catalogo", example = "250")
    private int nuevos;

    @Schema(description = "Nacionalidades existentes que se actualizaron", example = "0")
    private int actualizados;
}
