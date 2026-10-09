package com.proyecto.servicios.model.cliente;

import com.proyecto.servicios.util.CorreoUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.OptBoolean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.proyecto.servicios.model.cliente.ValidacionPatrones.*;

@Data
public class ClienteRequest {

    @NotBlank
    @Size(min = 2, max = 50, message = "Debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(example = "Juan")
    private String nombre;

    @Size(max = 50, message = "Debe tener maximo 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS_OPCIONAL, message = MSG_SOLO_LETRAS)
    @Schema(example = "Carlos")
    private String segundoNombre;

    @NotBlank
    @Size(min = 2, max = 50, message = "Debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(example = "Perez")
    private String apellidoPaterno;

    @NotBlank
    @Size(min = 2, max = 50, message = "Debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(example = "Lopez")
    private String apellidoMaterno;

    /**
     * Se recibe como "yyyy/MM/dd", por ejemplo "1999/01/31". La lectura es
     * estricta: fechas inexistentes como "1999/02/30" se rechazan.
     */
    @NotNull
    @Past(message = "La fecha de nacimiento no puede ser futura")
    @JsonFormat(pattern = FORMATO_FECHA_ESTRICTO, lenient = OptBoolean.FALSE)
    @Schema(type = "string", description = "Formato yyyy/MM/dd", example = "1999/01/31")
    private LocalDate fechaNacimiento;

    @NotBlank
    @Pattern(
            regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$",
            message = "CURP invalida, debe tener 18 caracteres con el formato oficial"
    )
    @Schema(example = "PELJ990131HGTRPN09")
    private String curp;

    @NotBlank
    @Pattern(
            regexp = "^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{2,3}$",
            message = "RFC invalido, debe tener 12 o 13 caracteres con el formato oficial"
    )
    @Schema(example = "PELJ990131AB1")
    private String rfc;

    @NotBlank
    @Pattern(regexp = SEXO, message = MSG_SEXO)
    @Schema(example = "H")
    private String sexo;

    @NotBlank
    @Pattern(regexp = CODIGO_NACIONALIDAD, message = MSG_NACIONALIDAD)
    @Schema(description = "Codigo del catalogo GET /catalogos/nacionalidades", example = "MEX")
    private String nacionalidad;

    /** Se recibe el codigo del catalogo; se pasa a mayusculas ("mex" -> "MEX"). */
    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad == null ? null : nacionalidad.trim().toUpperCase(java.util.Locale.ROOT);
    }

    @NotBlank
    @Pattern(regexp = ESTADO_CIVIL, message = MSG_ESTADO_CIVIL)
    @Schema(example = "SOLTERO")
    private String estadoCivil;

    @NotBlank
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 100)
    @Schema(example = "juan.perez@correo.com")
    private String correo;

    /** Capa 1: el correo se convierte a minusculas en cuanto llega en el JSON. */
    public void setCorreo(String correo) {
        this.correo = CorreoUtil.normalizar(correo);
    }

    @NotNull
    @Min(value = TELEFONO_MIN, message = MSG_TELEFONO)
    @Max(value = TELEFONO_MAX, message = MSG_TELEFONO)
    @Schema(example = "4181234567")
    private Long telefonoMovil;

    @Min(value = TELEFONO_MIN, message = MSG_TELEFONO)
    @Max(value = TELEFONO_MAX, message = MSG_TELEFONO)
    @Schema(example = "4187654321")
    private Long telefonoAlternativo;

    @Valid
    @NotNull
    private DomicilioRequest domicilio;

    @NotBlank
    @Size(max = 100, message = "Debe tener maximo 100 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(example = "Ingeniero")
    private String ocupacion;

    @NotBlank
    @Size(max = 100, message = "Debe tener maximo 100 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(example = "Tecnologias del Norte")
    private String empresa;

    @NotNull
    @DecimalMin(value = "0.01", message = "El ingreso mensual debe ser mayor a cero")
    @Digits(integer = 12, fraction = 2, message = "El ingreso mensual debe tener maximo 2 decimales (ej. 15000.00)")
    @Schema(implementation = String.class, format = "dinero", pattern = PATRON_DINERO, description = "Maximo 2 decimales. Se puede enviar como numero o texto", example = "15000.00")
    private BigDecimal ingresoMensual;
}
