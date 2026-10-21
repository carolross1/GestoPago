package com.proyecto.servicios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.entity.cliente.Sesion;
import com.proyecto.servicios.model.comun.RespuestaApi;
import com.proyecto.servicios.repositorys.cliente.SesionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

/**
 * Protege los endpoints del portal de clientes: exige un JWT valido en
 * el header Authorization Y una sesion activa en base de datos. Si han
 * pasado mas de N minutos desde la ultima actividad, la sesion se marca
 * como inactiva (bandera) y la peticion se rechaza, obligando a
 * iniciar sesion de nuevo.
 *
 * Se registra solo sobre las rutas protegidas (ver SeguridadConfig),
 * nunca sobre /auth/**, para no bloquear el propio login/registro.
 */
@Slf4j
@Component
public class SesionActivaFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SesionRepository sesionRepository;
    private final ObjectMapper objectMapper;

    @Value("${seguridad.sesion.inactividad-minutos}")
    private long inactividadMinutos;

    public SesionActivaFilter(JwtUtil jwtUtil, SesionRepository sesionRepository, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.sesionRepository = sesionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * El alta de un cliente (POST /clientes) no exige token: el login
     * requiere que el cliente ya exista, asi que si el alta tambien
     * pidiera token nunca se podria crear el primer cliente.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "POST".equalsIgnoreCase(request.getMethod())
                && ("/clientes".equals(path) || "/clientes/".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            rechazar(request, response, "Falta el token de autenticacion");
            return;
        }

        String token = header.substring(7);

        if (!jwtUtil.esValido(token)) {
            rechazar(request, response, "Token invalido o expirado, inicia sesion nuevamente");
            return;
        }

        Optional<Sesion> sesionOpt = sesionRepository.findByToken(token);
        if (sesionOpt.isEmpty() || !Boolean.TRUE.equals(sesionOpt.get().getActiva())) {
            rechazar(request, response, "Sesion invalida, inicia sesion nuevamente");
            return;
        }

        Sesion sesion = sesionOpt.get();
        long minutosInactivo = ChronoUnit.MINUTES.between(sesion.getUltimaActividad(), LocalDateTime.now());

        if (minutosInactivo >= inactividadMinutos) {
            sesion.setActiva(false);
            sesionRepository.save(sesion);
            log.info("Sesion marcada como inactiva por {} minutos sin actividad", minutosInactivo);
            rechazar(request, response, "Sesion expirada por inactividad, inicia sesion nuevamente");
            return;
        }

        sesion.setUltimaActividad(LocalDateTime.now());
        sesionRepository.save(sesion);

        request.setAttribute("clienteId", jwtUtil.obtenerClienteId(token));
        chain.doFilter(request, response);
    }

    /**
     * El filtro corre ANTES de los controladores, asi que el
     * @RestControllerAdvice no lo alcanza: aqui se arma directamente la
     * misma estructura RespuestaApi que usa el resto de la API.
     */
    private void rechazar(HttpServletRequest request, HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        RespuestaApi<Void> body = RespuestaApi.error(HttpServletResponse.SC_UNAUTHORIZED, mensaje,
                request.getRequestURI(), Collections.emptyList());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
