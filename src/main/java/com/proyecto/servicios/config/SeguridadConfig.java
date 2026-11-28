package com.proyecto.servicios.config;

import com.proyecto.servicios.security.AdminAuthFilter;
import com.proyecto.servicios.security.SesionActivaFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Registra SesionActivaFilter solo sobre las rutas del portal de
 * clientes (/clientes/* y /cuentas/*), dejando /auth/* (login/registro)
 * y el resto de la app sin verificacion de sesion.
 * Habilita soporte completo de CORS para conexiones desde frontend web y movil.
 */
@Configuration
public class SeguridadConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<SesionActivaFilter> sesionActivaFilterRegistration(SesionActivaFilter filtro) {
        FilterRegistrationBean<SesionActivaFilter> registration = new FilterRegistrationBean<>(filtro);
        registration.addUrlPatterns(
                "/clientes/*",
                "/cuentas/*",
                "/transacciones/*",
                "/auth/sesion",
                "/catalogos/nacionalidades/sincronizar"
        );
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AdminAuthFilter> adminAuthFilterRegistration(AdminAuthFilter filtro) {
        FilterRegistrationBean<AdminAuthFilter> registration = new FilterRegistrationBean<>(filtro);
        registration.addUrlPatterns("/api/admin/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addViewController("/admin").setViewName("forward:/admin.html");
                registry.addViewController("/admin/").setViewName("forward:/admin.html");
            }
        };
    }
}