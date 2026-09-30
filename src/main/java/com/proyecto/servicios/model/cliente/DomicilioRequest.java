package com.proyecto.servicios.model.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DomicilioRequest {

    @NotBlank
    private String calle;

    @NotBlank
    private String numeroExterior;

    private String numeroInterior;

    @NotBlank
    private String colonia;

    @NotBlank
    private String municipio;

    @NotBlank
    private String estado;

    @NotBlank
    @Pattern(regexp = "^\\d{5}$", message = "El codigo postal debe contener exactamente 5 digitos")
    private String codigoPostal;

    @NotBlank
    private String pais;
}
