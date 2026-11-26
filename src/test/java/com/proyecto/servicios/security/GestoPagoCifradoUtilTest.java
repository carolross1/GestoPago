package com.proyecto.servicios.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class GestoPagoCifradoUtilTest {

    private static final String KEY_B64 = Base64.getEncoder().encodeToString(new byte[32]); // 256 bits
    private static final String IV_B64 = Base64.getEncoder().encodeToString(new byte[16]);  // 128 bits

    @Test
    @DisplayName("cifrar y descifrar recuperan el texto original")
    void cifrarYDescifrarExitoso() {
        String original = "{\"monto\": 100.50, \"referencia\": \"TEST-123\"}";

        String cifrado = GestoPagoCifradoUtil.cifrar(original, KEY_B64, IV_B64);
        assertNotNull(cifrado);
        assertNotEquals(original, cifrado);

        String descifrado = GestoPagoCifradoUtil.descifrar(cifrado, KEY_B64, IV_B64);
        assertEquals(original, descifrado);
    }

    @Test
    @DisplayName("cifrar y descifrar retornan null si el texto es nulo")
    void manejoDeNulos() {
        assertNull(GestoPagoCifradoUtil.cifrar(null, KEY_B64, IV_B64));
        assertNull(GestoPagoCifradoUtil.descifrar(null, KEY_B64, IV_B64));
    }

    @Test
    @DisplayName("cifrar con claves invalidas lanza IllegalStateException")
    void errorConClaveInvalida() {
        assertThrows(IllegalStateException.class,
                () -> GestoPagoCifradoUtil.cifrar("data", "clave-invalida", IV_B64));
        assertThrows(IllegalStateException.class,
                () -> GestoPagoCifradoUtil.descifrar("data", "clave-invalida", IV_B64));
    }
}
