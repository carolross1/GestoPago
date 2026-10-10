package com.proyecto.servicios.controller.cliente;

/**
 * Ejemplos de cuerpos de error (GenericResponse) que se muestran en Swagger.
 * Son constantes para poder usarlas dentro de las anotaciones @ExampleObject.
 */
final class EjemplosError {

    private EjemplosError() {
    }

    static final String VALIDACION = """
            {"codigo": 400, "mensaje": "Error de validacion: nombre: Solo se permiten letras y espacios, sin numeros ni caracteres especiales"}""";
    static final String FECHA = """
            {"codigo": 400, "mensaje": "El campo fechaNacimiento debe tener formato yyyy/MM/dd (ej. 1999/01/31) y ser una fecha real"}""";
    static final String MENOR_EDAD = """
            {"codigo": 400, "mensaje": "El cliente debe ser mayor de edad (18 anios o mas)"}""";
    static final String RANGO = """
            {"codigo": 400, "mensaje": "La fecha 'desde' no puede ser posterior a la fecha 'hasta'"}""";
    static final String SIN_TOKEN = """
            {"codigo": 401, "mensaje": "Falta el token de autenticacion"}""";
    static final String SESION_EXPIRADA = """
            {"codigo": 401, "mensaje": "Sesion expirada por inactividad, inicia sesion nuevamente"}""";
    static final String CONTRASENA = """
            {"codigo": 401, "mensaje": "La contrasena es incorrecta"}""";
    static final String CLIENTE_NO_ENCONTRADO = """
            {"codigo": 404, "mensaje": "No se encontro el cliente con id 99"}""";
    static final String CORREO_NO_ENCONTRADO = """
            {"codigo": 404, "mensaje": "El correo no fue encontrado, verifica que este bien escrito o que el cliente este dado de alta"}""";
    static final String SIN_CONTRASENA = """
            {"codigo": 404, "mensaje": "El correo existe pero aun no tiene contrasena registrada, registrate primero en /auth/registro"}""";
    static final String CUENTA_NO_ENCONTRADA = """
            {"codigo": 404, "mensaje": "No se encontro una cuenta con el numero proporcionado"}""";
    static final String CURP_DUPLICADA = """
            {"codigo": 409, "mensaje": "Ya existe un cliente registrado con la CURP proporcionada"}""";
    static final String CORREO_DUPLICADO = """
            {"codigo": 409, "mensaje": "Ya existe un cliente registrado con el correo proporcionado"}""";
    static final String LOGIN_DUPLICADO = """
            {"codigo": 409, "mensaje": "Este correo ya tiene una contrasena registrada, inicia sesion"}""";
}
