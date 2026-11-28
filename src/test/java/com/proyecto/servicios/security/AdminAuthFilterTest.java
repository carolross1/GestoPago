package com.proyecto.servicios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private AdminAuthFilter filter;

    @BeforeEach
    void setUp() throws IOException {
        filter = new AdminAuthFilter(jwtUtil, new ObjectMapper());
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    void shouldNotFilter_permiteLoginYOptions() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/admin/login");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/api/admin/cuentas");
        assertFalse(filter.shouldNotFilter(request));

        when(request.getMethod()).thenReturn("OPTIONS");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_sinToken_retorna401() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_tokenClienteNoAdmin_retorna403() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer tokenCliente");
        when(jwtUtil.esTokenAdmin("tokenCliente")).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_tokenAdminValido_permitePaso() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer tokenAdmin");
        when(jwtUtil.esTokenAdmin("tokenAdmin")).thenReturn(true);
        when(jwtUtil.obtenerCorreoAdmin("tokenAdmin")).thenReturn("admin@gestopago.com");

        filter.doFilterInternal(request, response, chain);

        verify(request).setAttribute("adminCorreo", "admin@gestopago.com");
        verify(chain).doFilter(request, response);
    }
}
