package com.proyecto.servicios.util;

import com.proyecto.servicios.model.comun.RespuestaApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class RespuestasTest {

    @Test
    @DisplayName("ok construye ResponseEntity 200 con datos")
    void ok() {
        ResponseEntity<RespuestaApi<String>> respuesta = Respuestas.ok("Operación exitosa", "resultado");
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().isExito());
        assertEquals("Operación exitosa", respuesta.getBody().getMensaje());
        assertEquals("resultado", respuesta.getBody().getDatos());
    }

    @Test
    @DisplayName("creado construye ResponseEntity 201 con datos")
    void creado() {
        ResponseEntity<RespuestaApi<Integer>> respuesta = Respuestas.creado("Registro creado", 123);
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().isExito());
        assertEquals("Registro creado", respuesta.getBody().getMensaje());
        assertEquals(123, respuesta.getBody().getDatos());
    }
}
