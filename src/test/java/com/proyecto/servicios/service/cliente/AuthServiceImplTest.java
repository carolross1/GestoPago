package com.proyecto.servicios.service.cliente;

import com.proyecto.servicios.entity.cliente.Cliente;
import com.proyecto.servicios.entity.cliente.Login;
import com.proyecto.servicios.exception.cliente.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.cliente.CredencialesInvalidasException;
import com.proyecto.servicios.exception.cliente.LoginYaRegistradoException;
import com.proyecto.servicios.model.cliente.LoginRequest;
import com.proyecto.servicios.model.cliente.LoginResponse;
import com.proyecto.servicios.model.cliente.RegistroLoginRequest;
import com.proyecto.servicios.repositorys.cliente.ClienteRepository;
import com.proyecto.servicios.repositorys.cliente.LoginRepository;
import com.proyecto.servicios.repositorys.cliente.SesionRepository;
import com.proyecto.servicios.security.JwtUtil;
import com.proyecto.servicios.service.cliente.Impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private Cliente clienteConId(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setRfc("PELJ990101AB1");
        return cliente;
    }

    @Test
    void registrar_clienteExisteSinLogin_creaCredenciales() {
        Cliente cliente = clienteConId(1L);
        RegistroLoginRequest request = new RegistroLoginRequest();
        request.setRfc(cliente.getRfc());
        request.setPassword("password123");

        when(clienteRepository.findByRfc(cliente.getRfc())).thenReturn(Optional.of(cliente));
        when(loginRepository.existsByClienteId(1L)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash-generado");

        authService.registrar(request);
        // Si no lanzo excepcion, el flujo fue correcto
    }

    @Test
    void registrar_clienteNoExiste_lanzaExcepcion() {
        RegistroLoginRequest request = new RegistroLoginRequest();
        request.setRfc("NOEXISTE01");
        request.setPassword("password123");

        when(clienteRepository.findByRfc("NOEXISTE01")).thenReturn(Optional.empty());

        assertThrows(ClienteNoEncontradoException.class, () -> authService.registrar(request));
    }

    @Test
    void registrar_yaTieneLogin_lanzaExcepcion() {
        Cliente cliente = clienteConId(1L);
        RegistroLoginRequest request = new RegistroLoginRequest();
        request.setRfc(cliente.getRfc());
        request.setPassword("password123");

        when(clienteRepository.findByRfc(cliente.getRfc())).thenReturn(Optional.of(cliente));
        when(loginRepository.existsByClienteId(1L)).thenReturn(true);

        assertThrows(LoginYaRegistradoException.class, () -> authService.registrar(request));
    }

    @Test
    void login_credencialesCorrectas_regresaToken() {
        Cliente cliente = clienteConId(1L);
        Login login = new Login();
        login.setId(10L);
        login.setClienteId(1L);
        login.setPasswordHash("hash-guardado");

        LoginRequest request = new LoginRequest();
        request.setRfc(cliente.getRfc());
        request.setPassword("password123");

        when(clienteRepository.findByRfc(cliente.getRfc())).thenReturn(Optional.of(cliente));
        when(loginRepository.findByClienteId(1L)).thenReturn(Optional.of(login));
        when(passwordEncoder.matches("password123", "hash-guardado")).thenReturn(true);
        when(jwtUtil.generarToken(1L)).thenReturn("token-jwt-generado");
        when(jwtUtil.getExpiracionMs()).thenReturn(1800000L);

        LoginResponse response = authService.login(request);

        assertEquals("token-jwt-generado", response.getToken());
    }

    @Test
    void login_passwordIncorrecta_lanzaCredencialesInvalidas() {
        Cliente cliente = clienteConId(1L);
        Login login = new Login();
        login.setClienteId(1L);
        login.setPasswordHash("hash-guardado");

        LoginRequest request = new LoginRequest();
        request.setRfc(cliente.getRfc());
        request.setPassword("incorrecta");

        when(clienteRepository.findByRfc(cliente.getRfc())).thenReturn(Optional.of(cliente));
        when(loginRepository.findByClienteId(1L)).thenReturn(Optional.of(login));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
    }

    @Test
    void login_rfcNoExiste_lanzaCredencialesInvalidas() {
        LoginRequest request = new LoginRequest();
        request.setRfc("NOEXISTE01");
        request.setPassword("password123");

        when(clienteRepository.findByRfc("NOEXISTE01")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
    }
}
