package com.proyecto.servicios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.entity.cliente.Sesion;
import com.proyecto.servicios.repositorys.cliente.SesionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SesionActivaFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private ObjectMapper objectMapper = new ObjectMapper();
    private SesionActivaFilter filter;

    @BeforeEach
    void setUp() throws IOException {
        filter = new SesionActivaFilter(jwtUtil, sesionRepository, objectMapper);
        ReflectionTestUtils.setField(filter, "inactividadMinutos", 15L);
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    void shouldNotFilter_postClientes() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/clientes");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/clientes/");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/cuentas");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_sinHeader_rechaza() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_tokenInvalido_rechaza() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer tokenInvalido");
        when(jwtUtil.esValido("tokenInvalido")).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_sesionNoEncontradaOInactiva_rechaza() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer tokenValido");
        when(jwtUtil.esValido("tokenValido")).thenReturn(true);
        when(sesionRepository.findByToken("tokenValido")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_sesionExpiradaPorInactividad_rechaza() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer tokenValido");
        when(jwtUtil.esValido("tokenValido")).thenReturn(true);

        Sesion sesion = new Sesion();
        sesion.setActiva(true);
        sesion.setUltimaActividad(LocalDateTime.now().minusMinutes(20));
        when(sesionRepository.findByToken("tokenValido")).thenReturn(Optional.of(sesion));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(sesionRepository).save(sesion);
        assertFalse(sesion.getActiva());
    }

    @Test
    void doFilterInternal_sesionValida_permiteAcceso() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer tokenValido");
        when(jwtUtil.esValido("tokenValido")).thenReturn(true);
        when(jwtUtil.obtenerClienteId("tokenValido")).thenReturn(100L);

        Sesion sesion = new Sesion();
        sesion.setActiva(true);
        sesion.setUltimaActividad(LocalDateTime.now().minusMinutes(2));
        when(sesionRepository.findByToken("tokenValido")).thenReturn(Optional.of(sesion));

        filter.doFilterInternal(request, response, chain);

        verify(request).setAttribute("clienteId", 100L);
        verify(chain).doFilter(request, response);
        verify(sesionRepository).save(sesion);
    }
}
