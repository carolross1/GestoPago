package com.proyecto.servicios.model.cliente;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.proyecto.servicios.model.comun.SolicitudEstricta;
import com.proyecto.servicios.util.CorreoUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import static com.proyecto.servicios.model.cliente.ValidacionPatrones.*;

/**
 * Criterios de busqueda de clientes para el UNICO endpoint de consulta
 * (POST /clientes/buscar). Todos son opcionales y se combinan con AND:
 * los que vienen en null no filtran. Sin ningun criterio se regresan
 * todos los clientes, paginados.
 *
 * Los campos de texto se comparan por coincidencia EXACTA porque en la
 * base de datos se guardan cifrados (AES determinista): la BD no puede
 * hacer LIKE sobre texto cifrado, pero si comparar igualdad.
 */
@Getter
@Setter
@Schema(description = "Filtros combinables; envia solo los que necesites")
public class ClienteFiltroRequest extends SolicitudEstricta {

    @Positive(message = "El id debe ser mayor a cero")
    @Schema(example = "3")
    private Long id;

    @Size(min = 2, max = 50, message = "Debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(description = "Coincidencia exacta", example = "Carol")
    private String nombre;

    @Size(min = 2, max = 50, message = "Debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(description = "Coincidencia exacta", example = "Rios")
    private String apellidoPaterno;

    @Size(min = 2, max = 50, message = "Debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    @Schema(description = "Coincidencia exacta")
    private String apellidoMaterno;

    @Pattern(regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$",
            message = "CURP invalida, debe tener 18 caracteres con el formato oficial")
    @Schema(example = "RIRC050511MGTSSRA0")
    private String curp;

    @Pattern(regexp = "^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{2,3}$",
            message = "RFC invalido, debe tener 12 o 13 caracteres con el formato oficial")
    @Schema(example = "RIRC050511GP1")
    private String rfc;

    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 100, message = "El correo debe tener maximo 100 caracteres")
    @Schema(example = "carolrios@gmail.com")
    private String correo;

    @Pattern(regexp = SEXO, message = MSG_SEXO)
    @Schema(example = "M")
    private String sexo;

    @Pattern(regexp = ESTADO_CIVIL, message = MSG_ESTADO_CIVIL)
    @Schema(example = "SOLTERO")
    private String estadoCivil;

    @Pattern(regexp = CODIGO_NACIONALIDAD, message = MSG_NACIONALIDAD)
    @Schema(example = "MEX")
    private String nacionalidad;

    @Schema(description = "true = activos, false = dados de baja, null = ambos", example = "true")
    private Boolean activo;

    @Pattern(regexp = "^\\d{10}$", message = "El numero de cuenta debe tener exactamente 10 digitos")
    @Schema(example = "1452864812")
    private String numeroCuenta;

    @JsonFormat(pattern = FORMATO_FECHA_ESTRICTO, lenient = OptBoolean.FALSE)
    @Schema(type = "string", description = "Registrados desde esta fecha (inclusive), yyyy/MM/dd", example = "2026/01/01")
    private LocalDate fechaRegistroDesde;

    @JsonFormat(pattern = FORMATO_FECHA_ESTRICTO, lenient = OptBoolean.FALSE)
    @Schema(type = "string", description = "Registrados hasta esta fecha (inclusive), yyyy/MM/dd", example = "2026/12/31")
    private LocalDate fechaRegistroHasta;

    @DecimalMin(value = "0.00", message = "El ingreso minimo no puede ser negativo")
    @Digits(integer = 12, fraction = 2, message = "Maximo 2 decimales")
    @Schema(implementation = String.class, format = "dinero", example = "10000.00")
    private BigDecimal ingresoMinimo;

    @DecimalMin(value = "0.00", message = "El ingreso maximo no puede ser negativo")
    @Digits(integer = 12, fraction = 2, message = "Maximo 2 decimales")
    @Schema(implementation = String.class, format = "dinero", example = "50000.00")
    private BigDecimal ingresoMaximo;

    @Min(value = 0, message = "La pagina inicia en 0")
    @Schema(description = "Numero de pagina, inicia en 0", example = "0", defaultValue = "0")
    private Integer pagina = 0;

    @Min(value = 1, message = "El tamano de pagina debe ser al menos 1")
    @Max(value = 100, message = "El tamano de pagina debe ser maximo 100")
    @Schema(description = "Elementos por pagina (1 a 100)", example = "10", defaultValue = "10")
    private Integer tamano = 10;

    // ---- Reglas entre campos: tambien son Bean Validation, no codigo en el controlador ----

    @JsonIgnore
    @AssertTrue(message = "fechaRegistroDesde no puede ser posterior a fechaRegistroHasta")
    public boolean isRangoFechasValido() {
        return fechaRegistroDesde == null || fechaRegistroHasta == null
                || !fechaRegistroDesde.isAfter(fechaRegistroHasta);
    }

    @JsonIgnore
    @AssertTrue(message = "ingresoMinimo no puede ser mayor que ingresoMaximo")
    public boolean isRangoIngresoValido() {
        return ingresoMinimo == null || ingresoMaximo == null || ingresoMinimo.compareTo(ingresoMaximo) <= 0;
    }

    // ---- Normalizacion: igual que en el alta, para que la comparacion exacta coincida ----

    public void setCurp(String curp) {
        this.curp = mayusculas(curp);
    }

    public void setRfc(String rfc) {
        this.rfc = mayusculas(rfc);
    }

    public void setCorreo(String correo) {
        this.correo = CorreoUtil.normalizar(correo);
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = mayusculas(nacionalidad);
    }

    public void setPagina(Integer pagina) {
        this.pagina = pagina == null ? 0 : pagina;
    }

    public void setTamano(Integer tamano) {
        this.tamano = tamano == null ? 10 : tamano;
    }

    private static String mayusculas(String valor) {
        return valor == null ? null : valor.trim().toUpperCase(Locale.ROOT);
    }
}
