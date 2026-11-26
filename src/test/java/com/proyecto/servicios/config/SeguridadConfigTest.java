package com.proyecto.servicios.config;

import com.proyecto.servicios.security.SesionActivaFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SeguridadConfigTest {

    private final SeguridadConfig config = new SeguridadConfig();

    @Test
    @DisplayName("passwordEncoder retorna BCryptPasswordEncoder funcional")
    void passwordEncoder() {
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
        String pass = "Secreto123";
        String encoded = encoder.encode(pass);
        assertTrue(encoder.matches(pass, encoded));
    }

    @Test
    @DisplayName("sesionActivaFilterRegistration registra filtro con rutas protegidas")
    void sesionActivaFilterRegistration() {
        SesionActivaFilter filtro = mock(SesionActivaFilter.class);
        FilterRegistrationBean<SesionActivaFilter> reg = config.sesionActivaFilterRegistration(filtro);

        assertNotNull(reg);
        assertEquals(1, reg.getOrder());
        assertTrue(reg.getUrlPatterns().contains("/clientes/*"));
        assertTrue(reg.getUrlPatterns().contains("/cuentas/*"));
    }
}
