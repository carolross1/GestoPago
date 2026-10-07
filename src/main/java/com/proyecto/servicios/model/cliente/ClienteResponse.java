package com.proyecto.servicios.model.cliente;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static com.proyecto.servicios.model.cliente.ValidacionPatrones.FORMATO_FECHA;
import static com.proyecto.servicios.model.cliente.ValidacionPatrones.FORMATO_HORA;

@Data
public class ClienteResponse {
    private Long id;
    private String nombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;

    @JsonFormat(pattern = FORMATO_FECHA)
    @Schema(type = "string", description = "Formato yyyy/MM/dd", example = "1999/01/31")
    private LocalDate fechaNacimiento;

    private String curp;
    private String rfc;
    private String sexo;
    @Schema(description = "Codigo del catalogo de nacionalidades", example = "MEX")
    private String nacionalidad;

    @Schema(description = "Nombre del pais segun el catalogo", example = "México")
    private String nacionalidadNombre;

    private String estadoCivil;
    private String correo;
    @Schema(example = "4181234567")
    private Long telefonoMovil;

    @Schema(example = "4187654321")
    private Long telefonoAlternativo;

    private DomicilioRequest domicilio;
    private String ocupacion;
    private String empresa;

    /** Siempre con 2 decimales, ej. 15000.00 */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(implementation = String.class, format = "dinero", example = "15000.00")
    private BigDecimal ingresoMensual;

    private Boolean activo;

    /** Fecha de registro "yyyy/MM/dd" */
    @JsonFormat(pattern = FORMATO_FECHA)
    @Schema(type = "string", description = "Formato yyyy/MM/dd", example = "2026/10/02")
    private LocalDate fechaRegistro;

    /** Hora de registro "HH:mm:ss" (apartado separado de la fecha) */
    @JsonFormat(pattern = FORMATO_HORA)
    @Schema(type = "string", description = "Formato HH:mm:ss", example = "18:24:05")
    private LocalTime horaRegistro;

    private String numeroCuenta;
}
