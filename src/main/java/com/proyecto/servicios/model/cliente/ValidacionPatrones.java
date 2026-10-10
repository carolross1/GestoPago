package com.proyecto.servicios.model.cliente;

/**
 * Expresiones regulares y formatos compartidos por los DTO de cliente.
 * Se centralizan aqui para que ClienteRequest, ClienteActualizaRequest y
 * DomicilioRequest validen exactamente igual (las anotaciones aceptan
 * constantes static final como valor).
 */
public final class ValidacionPatrones {

    private ValidacionPatrones() {
    }

    private static final String LETRA = "[A-Za-zÁÉÍÓÚÜáéíóúüÑñ]";

    /**
     * Solo letras (con acentos y Ñ) separadas por un unico espacio.
     * No permite numeros, caracteres especiales, ni espacios al inicio,
     * al final o dobles.
     */
    public static final String SOLO_LETRAS = "^" + LETRA + "+( " + LETRA + "+)*$";

    /** Igual que SOLO_LETRAS pero acepta cadena vacia (campos opcionales). */
    public static final String SOLO_LETRAS_OPCIONAL = "^$|" + SOLO_LETRAS;

    public static final String MSG_SOLO_LETRAS =
            "Solo se permiten letras y espacios, sin numeros ni caracteres especiales";

    public static final String SEXO = "^(H|M|Otros)$";
    public static final String MSG_SEXO = "Sexo invalido, valores permitidos: H, M, Otros";

    /** Codigo ISO alfa-3 del catalogo de nacionalidades (ej. MEX). Que exista se valida en el servicio. */
    public static final String CODIGO_NACIONALIDAD = "^[A-Z]{3}$";
    public static final String MSG_NACIONALIDAD =
            "La nacionalidad debe ser el codigo de 3 letras del catalogo (ej. MEX). Consulta GET /catalogos/nacionalidades";

    public static final String ESTADO_CIVIL = "^(SOLTERO|CASADO|DIVORCIADO|VIUDO|UNION_LIBRE|SEPARADO)$";
    public static final String MSG_ESTADO_CIVIL =
            "Estado civil invalido, valores permitidos: SOLTERO, CASADO, DIVORCIADO, VIUDO, UNION_LIBRE, SEPARADO";

    /** Telefono numerico de 10 digitos: entre 1000000000 y 9999999999. */
    public static final long TELEFONO_MIN = 1_000_000_000L;
    public static final long TELEFONO_MAX = 9_999_999_999L;
    public static final String MSG_TELEFONO = "El telefono debe contener exactamente 10 digitos";

    /** Codigo postal numerico de Mexico (01000 a 99999; el 0 inicial se completa al guardar). */
    public static final long CP_MIN = 1_000L;
    public static final long CP_MAX = 99_999L;
    public static final String MSG_CP = "El codigo postal debe tener 5 digitos (01000 a 99999)";

    public static final long NUMERO_DOMICILIO_MAX = 999_999L;
    public static final String MSG_NUMERO_DOMICILIO = "Debe ser un numero entero entre 1 y 999999";

    /**
     * Cantidades de dinero: se reciben como numero o texto y se regresan
     * SIEMPRE como texto con 2 decimales ("15000.00"), para que ningun
     * cliente (Swagger, JavaScript) les quite los ceros.
     */
    public static final String PATRON_DINERO = "^\\d{1,12}(\\.\\d{1,2})?$";

    /** Formatos de fecha y hora que se reciben y regresan en la API. */
    public static final String FORMATO_FECHA = "yyyy/MM/dd";
    public static final String FORMATO_HORA = "HH:mm:ss";

    /**
     * Mismo formato año/mes/dia para LEER fechas en modo estricto. Se usa
     * "uuuu" (año) en vez de "yyyy" (año de era) porque el modo estricto
     * de java.time exige la era cuando se usa "yyyy".
     */
    public static final String FORMATO_FECHA_ESTRICTO = "uuuu/MM/dd";
}
