package com.proyecto.servicios.exception.cliente;

import com.proyecto.servicios.model.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalClienteExceptionHandler {

    @ExceptionHandler({ClienteYaRegistradoException.class, LoginYaRegistradoException.class})
    public ResponseEntity<GenericResponse> handleDuplicado(RuntimeException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({ClienteNoEncontradoException.class, CuentaNoEncontradaException.class})
    public ResponseEntity<GenericResponse> handleNoEncontrado(RuntimeException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<GenericResponse> handleValidacionNegocio(ValidacionNegocioException ex) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

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

    private ResponseEntity<GenericResponse> responder(HttpStatus status, String mensaje) {
        GenericResponse body = new GenericResponse();
        body.setCodigo(status.value());
        body.setMensaje(mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
