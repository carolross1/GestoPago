package com.proyecto.servicios.model.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import static com.proyecto.servicios.model.cliente.ValidacionPatrones.*;

@Data
public class DomicilioRequest {

    @NotBlank
    @Size(max = 100, message = "Debe tener maximo 100 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(example = "Hidalgo")
    private String calle;

    @NotNull
    @Min(value = 1, message = MSG_NUMERO_DOMICILIO)
    @Max(value = NUMERO_DOMICILIO_MAX, message = MSG_NUMERO_DOMICILIO)
    @Schema(example = "120")
    private Integer numeroExterior;

    @Min(value = 1, message = MSG_NUMERO_DOMICILIO)
    @Max(value = NUMERO_DOMICILIO_MAX, message = MSG_NUMERO_DOMICILIO)
    @Schema(example = "4")
    private Integer numeroInterior;

    @NotBlank
    @Size(max = 100, message = "Debe tener maximo 100 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(example = "Centro")
    private String colonia;

    @NotBlank
    @Size(max = 100, message = "Debe tener maximo 100 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(example = "Dolores Hidalgo")
    private String municipio;

    @NotBlank
    @Size(max = 50, message = "Debe tener maximo 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(example = "Guanajuato")
    private String estado;

    /** Numerico. Un CP como 06000 llega/sale como 6000 y se guarda completo como "06000". */
    @NotNull
    @Min(value = CP_MIN, message = MSG_CP)
    @Max(value = CP_MAX, message = MSG_CP)
    @Schema(example = "37800")
    private Integer codigoPostal;

    @NotBlank
    @Size(max = 50, message = "Debe tener maximo 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(example = "Mexico")
    private String pais;
}
