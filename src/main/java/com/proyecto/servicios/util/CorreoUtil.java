package com.proyecto.servicios.util;

import java.util.Locale;

/**
 * Regla unica para manejar correos en todo el proyecto: sin espacios al
 * inicio/fin y siempre en minusculas.
 *
 * Es necesario porque el correo se guarda CIFRADO (AES): la base de datos
 * no puede hacer LOWER(correo) sobre el texto cifrado, y "Juan@x.com" y
 * "juan@x.com" producen cifrados distintos. Por eso el correo se
 * normaliza ANTES de cifrarse, tanto al guardar como al buscar.
 */
public final class CorreoUtil {

    private CorreoUtil() {
    }

    public static String normalizar(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }
}
