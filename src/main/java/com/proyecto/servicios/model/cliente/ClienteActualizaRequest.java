package com.proyecto.servicios.model.cliente;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Solo los campos que SI se pueden actualizar. CURP, RFC y numero de
 * cuenta nunca se exponen aqui a proposito: no se pueden modificar.
 */
@Data
public class ClienteActualizaRequest {

    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,50}$")
    private String nombre;

    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{0,50}$")
    private String segundoNombre;

    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,50}$")
    private String apellidoPaterno;

    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,50}$")
    private String apellidoMaterno;

    private String sexo;
    private String nacionalidad;
    private String estadoCivil;

    @Email
    @Size(max = 100)
    private String correo;

    @Pattern(regexp = "^\\d{10}$")
    private String telefonoMovil;

    @Pattern(regexp = "^\\d{10}$")
    private String telefonoAlternativo;

    private DomicilioRequest domicilio;

    private String ocupacion;
    private String empresa;

    @DecimalMin(value = "0.01")
    private BigDecimal ingresoMensual;
}
