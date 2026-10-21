package com.proyecto.servicios.model.comun;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Estructura UNICA de todas las respuestas de la API, tanto exitosas como
 * de error. Siempre trae las mismas llaves, para que el cliente de la API
 * las procese igual:
 *
 * <pre>
 * {
 *   "exito": true,
 *   "codigo": 200,
 *   "mensaje": "Cliente actualizado correctamente",
 *   "fecha": "2026/10/16",
 *   "hora": "18:30:05",
 *   "ruta": "/clientes",
 *   "datos": { ... },     // null cuando hay error
 *   "errores": [ ... ]    // vacio cuando todo salio bien
 * }
 * </pre>
 *
 * @param <T> tipo de los datos que regresa la operacion
 */
@Getter
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"exito", "codigo", "mensaje", "fecha", "hora", "ruta", "datos", "errores"})
public class RespuestaApi<T> {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Schema(description = "true si la operacion se completo", example = "true")
    private final boolean exito;

    @Schema(description = "Codigo HTTP de la respuesta", example = "200")
    private final int codigo;

    @Schema(description = "Descripcion del resultado", example = "Operacion realizada correctamente")
    private final String mensaje;

    @Schema(description = "Fecha de la respuesta, yyyy/MM/dd", example = "2026/10/16")
    private final String fecha;

    @Schema(description = "Hora de la respuesta, HH:mm:ss", example = "18:30:05")
    private final String hora;

    @Schema(description = "Ruta que se invoco", example = "/clientes")
    private final String ruta;

    @Schema(description = "Resultado de la operacion; null cuando hay error")
    private final T datos;

    @Schema(description = "Errores por campo; vacio cuando la operacion fue exitosa")
    private final List<ErrorCampo> errores;

    private RespuestaApi(boolean exito, int codigo, String mensaje, String ruta, T datos, List<ErrorCampo> errores) {
        LocalDateTime ahora = LocalDateTime.now();
        this.exito = exito;
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.fecha = ahora.format(FORMATO_FECHA);
        this.hora = ahora.format(FORMATO_HORA);
        this.ruta = ruta;
        this.datos = datos;
        this.errores = errores == null ? Collections.emptyList() : errores;
    }

    public static <T> RespuestaApi<T> exito(int codigo, String mensaje, String ruta, T datos) {
        return new RespuestaApi<>(true, codigo, mensaje, ruta, datos, null);
    }

    public static <T> RespuestaApi<T> error(int codigo, String mensaje, String ruta, List<ErrorCampo> errores) {
        return new RespuestaApi<>(false, codigo, mensaje, ruta, null, errores);
    }
}
