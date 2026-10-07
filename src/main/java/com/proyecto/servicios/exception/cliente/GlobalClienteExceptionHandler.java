package com.proyecto.servicios.exception.cliente;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.proyecto.servicios.model.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalClienteExceptionHandler {

    @ExceptionHandler({ClienteYaRegistradoException.class, LoginYaRegistradoException.class})
    public ResponseEntity<GenericResponse> handleDuplicado(RuntimeException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Incluye CorreoNoEncontradoException y LoginNoRegistradoException (subclases). */
    @ExceptionHandler({ClienteNoEncontradoException.class, CuentaNoEncontradaException.class})
    public ResponseEntity<GenericResponse> handleNoEncontrado(RuntimeException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<GenericResponse> handleValidacionNegocio(ValidacionNegocioException ex) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Contrasena incorrecta en el login. */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<GenericResponse> handleCredenciales(CredencialesInvalidasException ex) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidacionCampos(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Error de validacion de campos: {}", detalle);
        return responder(HttpStatus.BAD_REQUEST, "Error de validacion: " + detalle);
    }

    /**
     * El JSON no se pudo leer: tipicamente una fecha que no viene como
     * yyyy/MM/dd o un numero que no es numerico.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse> handleJsonInvalido(HttpMessageNotReadableException ex) {
        String mensaje = "El cuerpo de la peticion no es un JSON valido";

        if (ex.getCause() instanceof InvalidFormatException ife) {
            String campo = nombreCampo(ife);
            Class<?> tipo = ife.getTargetType();
            if (LocalDate.class.equals(tipo)) {
                mensaje = "El campo " + campo + " debe tener formato yyyy/MM/dd (ej. 1999/01/31) y ser una fecha real";
            } else if (BigDecimal.class.equals(tipo)) {
                mensaje = "El campo " + campo + " debe ser una cantidad con maximo 2 decimales (ej. 15000.00)";
            } else if (Long.class.equals(tipo) || Integer.class.equals(tipo)) {
                mensaje = "El campo " + campo + " debe ser un numero entero, sin letras, espacios ni guiones";
            } else {
                mensaje = "El campo " + campo + " tiene un valor invalido";
            }
        } else if (ex.getCause() instanceof JsonMappingException jme && !jme.getPath().isEmpty()) {
            // p. ej. un numero demasiado grande o un decimal donde va un entero
            mensaje = "El campo " + nombreCampo(jme) + " tiene un valor invalido o fuera de rango";
        } else if (ex.getCause() instanceof com.fasterxml.jackson.core.exc.InputCoercionException) {
            mensaje = "Un campo numerico tiene un valor fuera de rango";
        }

        log.warn("JSON no legible: {}", mensaje);
        return responder(HttpStatus.BAD_REQUEST, mensaje);
    }

    /** Parametros de URL con tipo incorrecto, por ejemplo las fechas de /clientes/rango-fechas. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GenericResponse> handleParametroInvalido(MethodArgumentTypeMismatchException ex) {
        String mensaje = LocalDate.class.equals(ex.getRequiredType())
                ? "El parametro " + ex.getName() + " debe tener formato yyyy/MM/dd (ej. 2026/01/31)"
                : "El parametro " + ex.getName() + " tiene un valor invalido";
        return responder(HttpStatus.BAD_REQUEST, mensaje);
    }

    private String nombreCampo(JsonMappingException ex) {
        return ex.getPath().stream()
                .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                .collect(Collectors.joining("."));
    }

    private ResponseEntity<GenericResponse> responder(HttpStatus status, String mensaje) {
        GenericResponse body = new GenericResponse();
        body.setCodigo(status.value());
        body.setMensaje(mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
