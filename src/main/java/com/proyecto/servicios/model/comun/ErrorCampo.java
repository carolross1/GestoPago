package com.proyecto.servicios.model.comun;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Detalle de un error ligado a un campo concreto de la solicitud. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorCampo {

    @Schema(description = "Campo de la solicitud que tiene el problema", example = "correo")
    private String campo;

    @Schema(description = "Que esta mal y como corregirlo", example = "El correo no tiene un formato valido")
    private String mensaje;
}
