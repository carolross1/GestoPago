package com.proyecto.servicios.service.cliente.Impl;

import com.proyecto.servicios.entity.cliente.Cliente;
import com.proyecto.servicios.entity.cliente.Login;
import com.proyecto.servicios.entity.cliente.Sesion;
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
import com.proyecto.servicios.service.cliente.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Login/registro para el portal del cliente. La contrasena se guarda
 * con BCrypt (hash de una via) via PasswordEncoder, nunca con el
 * cifrado reversible que usan CURP/RFC/domicilio.
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final ClienteRepository clienteRepository;
    private final LoginRepository loginRepository;
    private final SesionRepository sesionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(ClienteRepository clienteRepository,
                            LoginRepository loginRepository,
                            SesionRepository sesionRepository,
                            PasswordEncoder passwordEncoder,
                            JwtUtil jwtUtil) {
        this.clienteRepository = clienteRepository;
        this.loginRepository = loginRepository;
        this.sesionRepository = sesionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public void registrar(RegistroLoginRequest request) {
        Cliente cliente = clienteRepository.findByRfc(request.getRfc())
                .orElseThrow(() -> new ClienteNoEncontradoException(
                        "No existe un cliente registrado con ese RFC. Debe darse de alta primero."));

        if (loginRepository.existsByClienteId(cliente.getId())) {
            throw new LoginYaRegistradoException("Este cliente ya tiene credenciales de acceso registradas");
        }

        Login login = new Login();
        login.setClienteId(cliente.getId());
        login.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        loginRepository.save(login);

        log.info("Credenciales de portal registradas para cliente id={}", cliente.getId());
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Cliente cliente = clienteRepository.findByRfc(request.getRfc())
                .orElseThrow(() -> new CredencialesInvalidasException("RFC o contrasena incorrectos"));

        Login login = loginRepository.findByClienteId(cliente.getId())
                .orElseThrow(() -> new CredencialesInvalidasException("RFC o contrasena incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), login.getPasswordHash())) {
            throw new CredencialesInvalidasException("RFC o contrasena incorrectos");
        }

        String token = jwtUtil.generarToken(cliente.getId());

        Sesion sesion = new Sesion();
        sesion.setLoginId(login.getId());
        sesion.setToken(token);
        sesion.setActiva(true);
        sesion.setUltimaActividad(LocalDateTime.now());
        sesionRepository.save(sesion);

        log.info("Login exitoso para cliente id={}", cliente.getId());
        return new LoginResponse(token, jwtUtil.getExpiracionMs());
    }
}
