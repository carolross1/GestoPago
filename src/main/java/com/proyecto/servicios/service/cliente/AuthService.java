package com.proyecto.servicios.service.cliente;

import com.proyecto.servicios.model.cliente.LoginRequest;
import com.proyecto.servicios.model.cliente.LoginResponse;
import com.proyecto.servicios.model.cliente.RegistroLoginRequest;

public interface AuthService {

    void registrar(RegistroLoginRequest request);

    LoginResponse login(LoginRequest request);
}
