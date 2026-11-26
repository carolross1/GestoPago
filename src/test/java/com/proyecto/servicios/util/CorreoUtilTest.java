package com.proyecto.servicios.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CorreoUtilTest {

    @Test
    @DisplayName("normalizar recorta espacios y convierte a minusculas")
    void normalizar() {
        assertNull(CorreoUtil.normalizar(null));
        assertEquals("juan@correo.com", CorreoUtil.normalizar("  Juan@Correo.COM  "));
        assertEquals("test.user+tag@domain.com", CorreoUtil.normalizar("Test.User+Tag@Domain.COM"));
    }
}
