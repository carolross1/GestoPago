package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones de integracion a respuestas HTTP consistentes
 * con el GenericResponse ya usado en el resto de la app, sin exponer
 * detalles internos (stacktraces, tokens, URLs) al cliente de la API.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalServiceAuthException.class)
    public ResponseEntity<GenericResponse> handleAuth(ExternalServiceAuthException ex) {
        GenericResponse body = new GenericResponse();
        body.setCodigo(HttpStatus.UNAUTHORIZED.value());
        body.setMensaje("No fue posible autenticar con " + ex.getServicio());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(ExternalServiceTimeoutException.class)
    public ResponseEntity<GenericResponse> handleTimeout(ExternalServiceTimeoutException ex) {
        GenericResponse body = new GenericResponse();
        body.setCodigo(HttpStatus.GATEWAY_TIMEOUT.value());
        body.setMensaje("Tiempo de espera agotado al consultar " + ex.getServicio());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(body);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<GenericResponse> handleExternal(ExternalServiceException ex) {
        GenericResponse body = new GenericResponse();
        body.setCodigo(HttpStatus.BAD_GATEWAY.value());
        body.setMensaje("Error al comunicarse con " + ex.getServicio());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
}
