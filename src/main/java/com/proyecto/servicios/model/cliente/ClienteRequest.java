package com.proyecto.servicios.model.cliente;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClienteRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,50}$", message = "Solo letras y espacios, entre 2 y 50 caracteres")
    private String nombre;

    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{0,50}$", message = "Solo letras y espacios")
    private String segundoNombre;

    @NotBlank
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,50}$", message = "Solo letras y espacios, entre 2 y 50 caracteres")
    private String apellidoPaterno;

    @NotBlank
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,50}$", message = "Solo letras y espacios, entre 2 y 50 caracteres")
    private String apellidoMaterno;

    @NotNull
    @Past(message = "La fecha de nacimiento no puede ser futura")
    private LocalDate fechaNacimiento;

    @NotBlank
    @Pattern(
            regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$",
            message = "CURP invalida, debe tener 18 caracteres con el formato oficial"
    )
    private String curp;

    @NotBlank
    @Pattern(
            regexp = "^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{2,3}$",
            message = "RFC invalido, debe tener 12 o 13 caracteres con el formato oficial"
    )
    private String rfc;

    @NotBlank
    private String sexo;

    @NotBlank
    private String nacionalidad;

    @NotBlank
    private String estadoCivil;

    @NotBlank
    @Email(message = "Correo invalido")
    @Size(max = 100)
    private String correo;

    @NotBlank
    @Pattern(regexp = "^\\d{10}$", message = "El telefono debe contener exactamente 10 digitos")
    private String telefonoMovil;

    @Pattern(regexp = "^\\d{10}$", message = "El telefono debe contener exactamente 10 digitos")
    private String telefonoAlternativo;

    @Valid
    @NotNull
    private DomicilioRequest domicilio;

    @NotBlank
    private String ocupacion;

    @NotBlank
    private String empresa;

    @NotNull
    @DecimalMin(value = "0.01", message = "El ingreso mensual debe ser mayor a cero")
    private BigDecimal ingresoMensual;
}
