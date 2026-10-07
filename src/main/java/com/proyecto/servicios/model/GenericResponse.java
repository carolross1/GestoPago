package com.proyecto.servicios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GenericResponse {
    @Schema(description = "Codigo HTTP", example = "400")
    private Integer codigo;

    @Schema(description = "Descripcion del error o resultado", example = "Error de validacion: nombre: Solo se permiten letras y espacios, sin numeros ni caracteres especiales")
    private String mensaje;
}
