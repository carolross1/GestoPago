package com.proyecto.servicios.model.cliente;

import com.proyecto.servicios.util.CorreoUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

import static com.proyecto.servicios.model.cliente.ValidacionPatrones.*;

/**
 * Solo los campos que SI se pueden actualizar. CURP, RFC y numero de
 * cuenta nunca se exponen aqui a proposito: no se pueden modificar.
 * Todos son opcionales (null = no se modifica), pero si vienen deben
 * cumplir las mismas reglas que en el alta.
 */
@Data
public class ClienteActualizaRequest {

    @Size(min = 2, max = 50, message = "Debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(example = "Juan")
    private String nombre;

    @Size(max = 50, message = "Debe tener maximo 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS_OPCIONAL, message = MSG_SOLO_LETRAS)
    private String segundoNombre;

    @Size(min = 2, max = 50, message = "Debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    private String apellidoPaterno;

    @Size(min = 2, max = 50, message = "Debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    private String apellidoMaterno;

    @Pattern(regexp = SEXO, message = MSG_SEXO)
    private String sexo;

    @Pattern(regexp = CODIGO_NACIONALIDAD, message = MSG_NACIONALIDAD)
    @Schema(description = "Codigo del catalogo GET /catalogos/nacionalidades", example = "MEX")
    private String nacionalidad;

    /** Se recibe el codigo del catalogo; se pasa a mayusculas ("mex" -> "MEX"). */
    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad == null ? null : nacionalidad.trim().toUpperCase(java.util.Locale.ROOT);
    }

    @Pattern(regexp = ESTADO_CIVIL, message = MSG_ESTADO_CIVIL)
    @Schema(example = "CASADO")
    private String estadoCivil;

    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 100)
    private String correo;

    /** Capa 1: el correo se convierte a minusculas en cuanto llega en el JSON. */
    public void setCorreo(String correo) {
        this.correo = CorreoUtil.normalizar(correo);
    }

    @Min(value = TELEFONO_MIN, message = MSG_TELEFONO)
    @Max(value = TELEFONO_MAX, message = MSG_TELEFONO)
    @Schema(example = "4181112233")
    private Long telefonoMovil;

    @Min(value = TELEFONO_MIN, message = MSG_TELEFONO)
    @Max(value = TELEFONO_MAX, message = MSG_TELEFONO)
    private Long telefonoAlternativo;

    @Valid
    private DomicilioRequest domicilio;

    @Size(max = 100, message = "Debe tener maximo 100 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    private String ocupacion;

    @Size(max = 100, message = "Debe tener maximo 100 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    private String empresa;

    @DecimalMin(value = "0.01", message = "El ingreso mensual debe ser mayor a cero")
    @Digits(integer = 12, fraction = 2, message = "El ingreso mensual debe tener maximo 2 decimales (ej. 15000.00)")
    @Schema(implementation = String.class, format = "dinero", pattern = PATRON_DINERO, description = "Maximo 2 decimales. Se puede enviar como numero o texto", example = "18500.50")
    private BigDecimal ingresoMensual;
}
