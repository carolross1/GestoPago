package com.proyecto.servicios.config;

import com.proyecto.servicios.security.SesionActivaFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Registra SesionActivaFilter solo sobre las rutas del portal de
 * clientes (/clientes/* y /cuentas/*), dejando /auth/* (login/registro)
 * y el resto de la app sin verificacion de sesion.
 */
@Configuration
public class SeguridadConfig {

    @Bean
    public FilterRegistrationBean<SesionActivaFilter> sesionActivaFilterRegistration(SesionActivaFilter filtro) {
        FilterRegistrationBean<SesionActivaFilter> registration = new FilterRegistrationBean<>(filtro);
        // La sincronizacion del catalogo modifica la BD y consume cuota de la API: exige sesion.
        // GET /catalogos/nacionalidades queda publico para poder llenar el combo antes del login.
        registration.addUrlPatterns("/clientes/*", "/cuentas/*", "/catalogos/nacionalidades/sincronizar");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
