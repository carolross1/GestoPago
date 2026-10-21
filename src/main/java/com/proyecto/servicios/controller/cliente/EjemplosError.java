package com.proyecto.servicios.controller.cliente;

/**
 * Ejemplos de respuestas de error (estructura RespuestaApi) que se muestran
 * en Swagger. Son constantes para poder usarlas en @ExampleObject.
 */
public final class EjemplosError {

    private EjemplosError() {
    }

    public static final String VALIDACION = "{\"exito\": false, \"codigo\": 400, "
            + "\"mensaje\": \"La solicitud tiene 2 campo(s) con datos invalidos\", \"fecha\": \"2026/10/16\", "
            + "\"hora\": \"18:30:05\", \"ruta\": \"/clientes\", \"datos\": null, \"errores\": ["
            + "{\"campo\": \"nombre\", \"mensaje\": \"Solo se permiten letras y espacios, sin numeros ni caracteres especiales\"}, "
            + "{\"campo\": \"correo\", \"mensaje\": \"El correo no tiene un formato valido\"}]}";

    public static final String CAMPO_PROHIBIDO = "{\"exito\": false, \"codigo\": 400, "
            + "\"mensaje\": \"La solicitud contiene campos que no se pueden enviar\", \"fecha\": \"2026/10/16\", "
            + "\"hora\": \"18:30:05\", \"ruta\": \"/clientes\", \"datos\": null, \"errores\": ["
            + "{\"campo\": \"activo\", \"mensaje\": \"El campo 'activo' no esta permitido en esta solicitud: "
            + "no existe o es un valor que asigna el servidor\"}]}";

    public static final String FECHA = "{\"exito\": false, \"codigo\": 400, "
            + "\"mensaje\": \"El campo fechaNacimiento tiene un formato invalido\", \"fecha\": \"2026/10/16\", "
            + "\"hora\": \"18:30:05\", \"ruta\": \"/clientes\", \"datos\": null, \"errores\": ["
            + "{\"campo\": \"fechaNacimiento\", \"mensaje\": \"Debe tener formato yyyy/MM/dd (ej. 1999/01/31) y ser una fecha real\"}]}";

    public static final String SIN_TOKEN = "{\"exito\": false, \"codigo\": 401, "
            + "\"mensaje\": \"Falta el token de autenticacion\", \"fecha\": \"2026/10/16\", \"hora\": \"18:30:05\", "
            + "\"ruta\": \"/clientes/buscar\", \"datos\": null, \"errores\": []}";

    public static final String CONTRASENA = "{\"exito\": false, \"codigo\": 401, "
            + "\"mensaje\": \"La contrasena es incorrecta\", \"fecha\": \"2026/10/16\", \"hora\": \"18:30:05\", "
            + "\"ruta\": \"/auth/login\", \"datos\": null, \"errores\": []}";

    public static final String CLIENTE_NO_ENCONTRADO = "{\"exito\": false, \"codigo\": 404, "
            + "\"mensaje\": \"No se encontro el cliente con id 99\", \"fecha\": \"2026/10/16\", \"hora\": \"18:30:05\", "
            + "\"ruta\": \"/clientes\", \"datos\": null, \"errores\": []}";

    public static final String CORREO_NO_ENCONTRADO = "{\"exito\": false, \"codigo\": 404, "
            + "\"mensaje\": \"El correo no fue encontrado, verifica que este bien escrito o que el cliente este dado de alta\", "
            + "\"fecha\": \"2026/10/16\", \"hora\": \"18:30:05\", \"ruta\": \"/auth/login\", \"datos\": null, \"errores\": []}";

    public static final String SIN_CONTRASENA = "{\"exito\": false, \"codigo\": 404, "
            + "\"mensaje\": \"El correo existe pero aun no tiene contrasena registrada, registrate primero en /auth/registro\", "
            + "\"fecha\": \"2026/10/16\", \"hora\": \"18:30:05\", \"ruta\": \"/auth/login\", \"datos\": null, \"errores\": []}";

    public static final String CURP_DUPLICADA = "{\"exito\": false, \"codigo\": 409, "
            + "\"mensaje\": \"Ya existe un cliente registrado con la CURP proporcionada\", \"fecha\": \"2026/10/16\", "
            + "\"hora\": \"18:30:05\", \"ruta\": \"/clientes\", \"datos\": null, \"errores\": []}";

    public static final String CORREO_DUPLICADO = "{\"exito\": false, \"codigo\": 409, "
            + "\"mensaje\": \"Ya existe un cliente registrado con el correo proporcionado\", \"fecha\": \"2026/10/16\", "
            + "\"hora\": \"18:30:05\", \"ruta\": \"/clientes\", \"datos\": null, \"errores\": []}";

    public static final String CLIENTE_INACTIVO = "{\"exito\": false, \"codigo\": 409, "
            + "\"mensaje\": \"El cliente con id 3 esta dado de baja y no se puede modificar\", \"fecha\": \"2026/10/16\", "
            + "\"hora\": \"18:30:05\", \"ruta\": \"/clientes\", \"datos\": null, \"errores\": []}";

    public static final String LOGIN_DUPLICADO = "{\"exito\": false, \"codigo\": 409, "
            + "\"mensaje\": \"Este correo ya tiene una contrasena registrada, inicia sesion\", \"fecha\": \"2026/10/16\", "
            + "\"hora\": \"18:30:05\", \"ruta\": \"/auth/registro\", \"datos\": null, \"errores\": []}";

    public static final String MENOR_EDAD = "{\"exito\": false, \"codigo\": 422, "
            + "\"mensaje\": \"El cliente debe ser mayor de edad (18 anios o mas)\", \"fecha\": \"2026/10/16\", "
            + "\"hora\": \"18:30:05\", \"ruta\": \"/clientes\", \"datos\": null, \"errores\": []}";

    public static final String NACIONALIDAD = "{\"exito\": false, \"codigo\": 422, "
            + "\"mensaje\": \"La nacionalidad 'XYZ' no existe en el catalogo. Consulta los codigos validos en "
            + "GET /catalogos/nacionalidades (ej. MEX)\", \"fecha\": \"2026/10/16\", \"hora\": \"18:30:05\", "
            + "\"ruta\": \"/clientes\", \"datos\": null, \"errores\": []}";

    public static final String RANGO = "{\"exito\": false, \"codigo\": 400, "
            + "\"mensaje\": \"La solicitud tiene 1 campo(s) con datos invalidos\", \"fecha\": \"2026/10/16\", "
            + "\"hora\": \"18:30:05\", \"ruta\": \"/clientes/buscar\", \"datos\": null, \"errores\": ["
            + "{\"campo\": \"rangoFechasValido\", \"mensaje\": \"fechaRegistroDesde no puede ser posterior a fechaRegistroHasta\"}]}";

    public static final String EXTERNO = "{\"exito\": false, \"codigo\": 502, "
            + "\"mensaje\": \"No fue posible comunicarse con el servicio externo RestCountries\", \"fecha\": \"2026/10/16\", "
            + "\"hora\": \"18:30:05\", \"ruta\": \"/catalogos/nacionalidades/sincronizar\", \"datos\": null, \"errores\": []}";
}
