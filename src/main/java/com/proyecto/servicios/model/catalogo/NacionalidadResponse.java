package com.proyecto.servicios.model.catalogo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NacionalidadResponse {

    @Schema(description = "Codigo ISO alfa-3. Es el valor que se envia en el campo nacionalidad del cliente", example = "MEX")
    private String codigo;

    @Schema(example = "MX")
    private String codigoIso2;

    @Schema(example = "México")
    private String nombre;
}
