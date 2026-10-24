package com.proyecto.servicios.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.proyecto.servicios.exception.cliente.ClienteInactivoException;
import com.proyecto.servicios.exception.cliente.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.cliente.ClienteYaRegistradoException;
import com.proyecto.servicios.exception.cliente.CredencialesInvalidasException;
import com.proyecto.servicios.exception.cliente.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.cliente.LoginYaRegistradoException;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.exception.sf.PersonaNoEncontradaException;
import com.proyecto.servicios.exception.transaccion.Codigo2faInvalidoException;
import com.proyecto.servicios.exception.transaccion.SaldoInsuficienteException;
import com.proyecto.servicios.exception.transaccion.TransaccionRechazadaException;
import com.proyecto.servicios.model.comun.ErrorCampo;
import com.proyecto.servicios.model.comun.RespuestaApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manejador GLOBAL y UNICO de errores de la API.
 *
 * - Los controladores no tienen try-catch ni validaciones manuales: las
 *   excepciones suben hasta aqui y cada tipo tiene su @ExceptionHandler.
 * - Todas las respuestas de error usan la misma estructura (RespuestaApi)
 *   que las respuestas exitosas, con exito=false y la lista de errores.
 * - Nunca se envian al cliente trazas, SQL ni nombres de clases internas:
 *   el detalle tecnico solo va al log del servidor.
 *
 * Codigos que se usan:
 *  400 datos de entrada invalidos       404 recurso inexistente
 *  401 credenciales / sesion            405 metodo HTTP no soportado
 *  409 duplicados o estado en conflicto 415 Content-Type no soportado
 *  422 regla de negocio no cumplida     500 error interno no previsto
 *  502/504 falla de un servicio externo (GestoPago, RestCountries)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ======================= 400: validacion de entrada =======================

    /** @Valid sobre un Request DTO: se regresan TODOS los campos con error. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaApi<Void>> validacionDeCampos(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request) {
        List<ErrorCampo> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(this::aErrorCampo)
                .collect(Collectors.toList());
        ex.getBindingResult().getGlobalErrors().forEach(g ->
                errores.add(new ErrorCampo(g.getObjectName(), g.getDefaultMessage())));

        log.warn("Validacion fallida en {}: {}", request.getRequestURI(), errores);
        return responder(HttpStatus.BAD_REQUEST,
                "La solicitud tiene " + errores.size() + " campo(s) con datos invalidos", request, errores);
    }

    /** El JSON no se pudo leer: falta el cuerpo, esta mal formado, un tipo no coincide o trae campos prohibidos. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaApi<Void>> cuerpoIlegible(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        CampoNoPermitidoException campoProhibido = buscarCausa(ex, CampoNoPermitidoException.class);
        if (campoProhibido != null) {
            return responder(HttpStatus.BAD_REQUEST, "La solicitud contiene campos que no se pueden enviar", request,
                    List.of(new ErrorCampo(campoProhibido.getCampo(), campoProhibido.getMessage())));
        }

        InvalidFormatException formato = buscarCausa(ex, InvalidFormatException.class);
        if (formato != null) {
            String campo = nombreCampo(formato);
            return responder(HttpStatus.BAD_REQUEST, "El campo " + campo + " tiene un formato invalido", request,
                    List.of(new ErrorCampo(campo, mensajeDeFormato(formato.getTargetType()))));
        }

        JsonMappingException mapeo = buscarCausa(ex, JsonMappingException.class);
        if (mapeo != null && !mapeo.getPath().isEmpty()) {
            String campo = nombreCampo(mapeo);
            return responder(HttpStatus.BAD_REQUEST, "El campo " + campo + " tiene un valor invalido", request,
                    List.of(new ErrorCampo(campo, "El valor no corresponde al tipo de dato esperado o esta fuera de rango")));
        }

        String mensaje = ex.getMessage() != null && ex.getMessage().startsWith("Required request body is missing")
                ? "El cuerpo de la solicitud es obligatorio: envia un JSON con los datos"
                : "El cuerpo de la solicitud no es un JSON valido; revisa comillas, comas y llaves";
        return responder(HttpStatus.BAD_REQUEST, mensaje, request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaApi<Void>> parametroInvalido(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest request) {
        String mensaje = "El parametro '" + ex.getName() + "' tiene un valor invalido";
        return responder(HttpStatus.BAD_REQUEST, mensaje, request, List.of());
    }

    // ======================= 401: autenticacion =======================

    @ExceptionHandler({CredencialesInvalidasException.class, Codigo2faInvalidoException.class})
    public ResponseEntity<RespuestaApi<Void>> credenciales(RuntimeException ex, HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, List.of());
    }

    // ======================= 404: no encontrado =======================

    /** Incluye CorreoNoEncontradoException y LoginNoRegistradoException (subclases), y PersonaNoEncontradaException. */
    @ExceptionHandler({ClienteNoEncontradoException.class, CuentaNoEncontradaException.class, PersonaNoEncontradaException.class})
    public ResponseEntity<RespuestaApi<Void>> noEncontrado(RuntimeException ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RespuestaApi<Void>> rutaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND,
                "La ruta " + request.getRequestURI() + " no existe; consulta los endpoints disponibles en Swagger",
                request, List.of());
    }

    // ======================= 405 / 415: forma de la peticion =======================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RespuestaApi<Void>> metodoNoSoportado(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest request) {
        String permitidos = ex.getSupportedHttpMethods() == null ? "" :
                " Metodos permitidos: " + ex.getSupportedHttpMethods();
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "El metodo " + ex.getMethod() + " no esta permitido en " + request.getRequestURI() + "." + permitidos,
                request, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<RespuestaApi<Void>> tipoNoSoportado(HttpMediaTypeNotSupportedException ex,
                                                              HttpServletRequest request) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "El cuerpo debe enviarse como JSON con el encabezado Content-Type: application/json",
                request, List.of());
    }

    // ======================= 409: conflictos =======================

    /** CURP, RFC o correo duplicados; contrasena ya registrada. */
    @ExceptionHandler({ClienteYaRegistradoException.class, LoginYaRegistradoException.class})
    public ResponseEntity<RespuestaApi<Void>> duplicado(RuntimeException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler({ClienteInactivoException.class, SaldoInsuficienteException.class})
    public ResponseEntity<RespuestaApi<Void>> conflictoEstado(RuntimeException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    /** Una restriccion de la BD (UNIQUE, FK, CHECK) que no se detecto antes. Nunca se envia el SQL. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaApi<Void>> integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violacion de integridad en {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT,
                "Los datos enviados chocan con informacion ya registrada (un valor unico repetido o una referencia inexistente)",
                request, List.of());
    }

    // ======================= 422: reglas de negocio =======================

    /** Mayoria de edad, nacionalidad fuera de catalogo, rango de fechas, etc. */
    @ExceptionHandler({ValidacionNegocioException.class, TransaccionRechazadaException.class})
    public ResponseEntity<RespuestaApi<Void>> reglaDeNegocio(RuntimeException ex, HttpServletRequest request) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request, List.of());
    }

    // ======================= 502 / 504: servicios externos =======================

    @ExceptionHandler(ExternalServiceTimeoutException.class)
    public ResponseEntity<RespuestaApi<Void>> externoTimeout(ExternalServiceTimeoutException ex, HttpServletRequest request) {
        log.error("Timeout con {}: {}", ex.getServicio(), ex.getMessage());
        return responder(HttpStatus.GATEWAY_TIMEOUT,
                "El servicio externo " + ex.getServicio() + " no respondio a tiempo; intenta de nuevo en unos minutos",
                request, List.of());
    }

    /** Incluye ExternalServiceAuthException (subclase). */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<RespuestaApi<Void>> externo(ExternalServiceException ex, HttpServletRequest request) {
        log.error("Falla con {}: {}", ex.getServicio(), ex.getMessage());
        return responder(HttpStatus.BAD_GATEWAY,
                "No fue posible comunicarse con el servicio externo " + ex.getServicio(), request, List.of());
    }

    // ======================= 500: cualquier otro error =======================

    /**
     * Ultima red de seguridad. El cliente recibe un folio para reportar el
     * problema; la traza completa queda SOLO en el log, junto con ese folio.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaApi<Void>> noPrevisto(Exception ex, HttpServletRequest request) {
        String folio = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.error("Error no previsto [folio {}] en {} {}", folio, request.getMethod(), request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo completar la operacion por un error interno del servidor. "
                        + "Reporta el folio " + folio + " al administrador si el problema continua",
                request, List.of());
    }

    // ======================= utilidades =======================

    private ResponseEntity<RespuestaApi<Void>> responder(HttpStatus status, String mensaje,
                                                         HttpServletRequest request, List<ErrorCampo> errores) {
        return ResponseEntity.status(status)
                .body(RespuestaApi.error(status.value(), mensaje, request.getRequestURI(), errores));
    }

    private ErrorCampo aErrorCampo(FieldError error) {
        return new ErrorCampo(error.getField(), error.getDefaultMessage());
    }

    private String nombreCampo(JsonMappingException ex) {
        return ex.getPath().stream()
                .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                .collect(Collectors.joining("."));
    }

    private String mensajeDeFormato(Class<?> tipo) {
        if (LocalDate.class.equals(tipo)) {
            return "Debe tener formato yyyy/MM/dd (ej. 1999/01/31) y ser una fecha real";
        }
        if (BigDecimal.class.equals(tipo)) {
            return "Debe ser una cantidad con maximo 2 decimales (ej. 15000.00)";
        }
        if (Long.class.equals(tipo) || Integer.class.equals(tipo)) {
            return "Debe ser un numero entero, sin letras, espacios ni guiones";
        }
        if (Boolean.class.equals(tipo)) {
            return "Debe ser true o false";
        }
        return "El valor no corresponde al tipo de dato esperado";
    }

    /** Busca una excepcion de cierto tipo en la cadena de causas. */
    private <T extends Throwable> T buscarCausa(Throwable ex, Class<T> tipo) {
        Throwable actual = ex;
        while (actual != null) {
            if (tipo.isInstance(actual)) {
                return tipo.cast(actual);
            }
            actual = actual.getCause() == actual ? null : actual.getCause();
        }
        return null;
    }
}
