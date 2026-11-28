package com.proyecto.servicios.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "0123456789012345678901234567890123456789012345678901234567890123";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secreto", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiracionMs", 3600000L);
    }

    @Test
    void generarYValidarToken() {
        Long clienteId = 12345L;
        String token = jwtUtil.generarToken(clienteId);

        assertNotNull(token);
        assertTrue(jwtUtil.esValido(token));
        assertEquals(clienteId, jwtUtil.obtenerClienteId(token));
        assertEquals(3600000L, jwtUtil.getExpiracionMs());
    }

    @Test
    void tokenInvalidoRetornaFalse() {
        assertFalse(jwtUtil.esValido("token.invalido.falso"));
        assertFalse(jwtUtil.esTokenAdmin("token.invalido.falso"));
    }

    @Test
    void tokenAdmin_esDiferenteATokenCliente() {
        String tokenCliente = jwtUtil.generarToken(42L);
        String tokenAdmin = jwtUtil.generarTokenAdmin("admin@gestopago.com");

        assertFalse(jwtUtil.esTokenAdmin(tokenCliente));
        assertTrue(jwtUtil.esTokenAdmin(tokenAdmin));
        assertEquals("admin@gestopago.com", jwtUtil.obtenerCorreoAdmin(tokenAdmin));
    }
}
