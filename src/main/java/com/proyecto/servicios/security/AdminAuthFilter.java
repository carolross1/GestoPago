package com.proyecto.servicios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.model.comun.RespuestaApi;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro exclusivo para el panel administrativo (/api/admin/*).
 * Exige un JWT con rol=ADMIN emitido por POST /api/admin/login.
 * Un JWT de cliente normal nunca pasa este filtro.
 */
@Slf4j
@Component
public class AdminAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AdminAuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return "POST".equalsIgnoreCase(request.getMethod())
                && ("/api/admin/login".equals(path) || "/api/admin/login/".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            rechazar(request, response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Acceso restringido. Inicia sesión como administrador.");
            return;
        }

        String token = header.substring(7);
        if (!jwtUtil.esTokenAdmin(token)) {
            rechazar(request, response, HttpServletResponse.SC_FORBIDDEN,
                    "No tienes permisos de administrador o tu sesión expiró.");
            return;
        }

        request.setAttribute("adminCorreo", jwtUtil.obtenerCorreoAdmin(token));
        chain.doFilter(request, response);
    }

    private void rechazar(HttpServletRequest request, HttpServletResponse response, int status, String mensaje)
            throws IOException {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        RespuestaApi<Void> body = RespuestaApi.error(status, mensaje,
                request.getRequestURI(), Collections.emptyList());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
