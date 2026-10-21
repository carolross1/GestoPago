package com.proyecto.servicios.util;

import com.proyecto.servicios.model.comun.RespuestaApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Arma las respuestas EXITOSAS con la misma estructura (RespuestaApi) que
 * usan los errores del GlobalExceptionHandler. Asi los controladores solo
 * indican el codigo, el mensaje y los datos.
 */
public final class Respuestas {

    private Respuestas() {
    }

    /** 200 OK */
    public static <T> ResponseEntity<RespuestaApi<T>> ok(String mensaje, T datos) {
        return con(HttpStatus.OK, mensaje, datos);
    }

    /** 201 Created */
    public static <T> ResponseEntity<RespuestaApi<T>> creado(String mensaje, T datos) {
        return con(HttpStatus.CREATED, mensaje, datos);
    }

    private static <T> ResponseEntity<RespuestaApi<T>> con(HttpStatus status, String mensaje, T datos) {
        return ResponseEntity.status(status)
                .body(RespuestaApi.exito(status.value(), mensaje, rutaActual(), datos));
    }

    private static String rutaActual() {
        var atributos = RequestContextHolder.getRequestAttributes();
        if (atributos instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest().getRequestURI();
        }
        return null;
    }
}
