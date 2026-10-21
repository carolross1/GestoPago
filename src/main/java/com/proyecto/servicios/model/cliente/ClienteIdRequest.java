package com.proyecto.servicios.model.cliente;

import com.proyecto.servicios.model.comun.SolicitudEstricta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/** Identifica al cliente sobre el que se hace una accion (ej. la baja). */
@Getter
@Setter
public class ClienteIdRequest extends SolicitudEstricta {

    @NotNull(message = "El id del cliente es obligatorio")
    @Positive(message = "El id debe ser mayor a cero")
    @Schema(example = "3")
    private Long id;
}
