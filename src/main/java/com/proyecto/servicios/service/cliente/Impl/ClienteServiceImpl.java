package com.proyecto.servicios.service.cliente.Impl;

import com.proyecto.servicios.entity.cliente.Cliente;
import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.entity.cliente.Domicilio;
import com.proyecto.servicios.exception.cliente.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.cliente.CorreoDuplicadoException;
import com.proyecto.servicios.exception.cliente.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.cliente.CurpDuplicadaException;
import com.proyecto.servicios.exception.cliente.RfcDuplicadoException;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.model.cliente.ClienteActualizaRequest;
import com.proyecto.servicios.model.cliente.ClienteRequest;
import com.proyecto.servicios.model.cliente.ClienteResponse;
import com.proyecto.servicios.model.cliente.DomicilioRequest;
import com.proyecto.servicios.repositorys.cliente.ClienteRepository;
import com.proyecto.servicios.repositorys.cliente.CuentaRepository;
import com.proyecto.servicios.repositorys.cliente.DomicilioRepository;
import com.proyecto.servicios.service.cliente.ClienteService;
import com.proyecto.servicios.service.cliente.CuentaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClienteServiceImpl implements ClienteService {

    private static final int EDAD_MINIMA = 18;

    private final ClienteRepository clienteRepository;
    private final DomicilioRepository domicilioRepository;
    private final CuentaRepository cuentaRepository;
    private final CuentaService cuentaService;

    public ClienteServiceImpl(ClienteRepository clienteRepository,
                               DomicilioRepository domicilioRepository,
                               CuentaRepository cuentaRepository,
                               CuentaService cuentaService) {
        this.clienteRepository = clienteRepository;
        this.domicilioRepository = domicilioRepository;
        this.cuentaRepository = cuentaRepository;
        this.cuentaService = cuentaService;
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        validarMayoriaDeEdad(request.getFechaNacimiento());

        if (clienteRepository.existsByCurp(request.getCurp())) {
            throw new CurpDuplicadaException(request.getCurp());
        }
        if (clienteRepository.existsByRfc(request.getRfc())) {
            throw new RfcDuplicadoException(request.getRfc());
        }
        if (clienteRepository.existsByCorreo(request.getCorreo())) {
            throw new CorreoDuplicadoException(request.getCorreo());
        }

        Cliente cliente = new Cliente();
        copiarCamposBasicos(request, cliente);
        cliente = clienteRepository.save(cliente);

        Domicilio domicilio = mapearDomicilio(request.getDomicilio(), cliente.getId());
        domicilioRepository.save(domicilio);

        Cuenta cuenta = cuentaService.crearCuentaParaCliente(cliente.getId());

        log.info("Cliente registrado id={} con cuenta {}", cliente.getId(), cuenta.getNumeroCuenta());
        return construirResponse(cliente, domicilio, cuenta.getNumeroCuenta());
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteActualizaRequest request) {
        Cliente cliente = buscarPorId(id);

        if (request.getCorreo() != null && !request.getCorreo().equals(cliente.getCorreo())) {
            if (clienteRepository.existsByCorreo(request.getCorreo())) {
                throw new CorreoDuplicadoException(request.getCorreo());
            }
            cliente.setCorreo(request.getCorreo());
        }

        if (request.getNombre() != null) cliente.setNombre(request.getNombre());
        if (request.getSegundoNombre() != null) cliente.setSegundoNombre(request.getSegundoNombre());
        if (request.getApellidoPaterno() != null) cliente.setApellidoPaterno(request.getApellidoPaterno());
        if (request.getApellidoMaterno() != null) cliente.setApellidoMaterno(request.getApellidoMaterno());
        if (request.getSexo() != null) cliente.setSexo(request.getSexo());
        if (request.getNacionalidad() != null) cliente.setNacionalidad(request.getNacionalidad());
        if (request.getEstadoCivil() != null) cliente.setEstadoCivil(request.getEstadoCivil());
        if (request.getTelefonoMovil() != null) cliente.setTelefonoMovil(request.getTelefonoMovil());
        if (request.getTelefonoAlternativo() != null) cliente.setTelefonoAlternativo(request.getTelefonoAlternativo());
        if (request.getOcupacion() != null) cliente.setOcupacion(request.getOcupacion());
        if (request.getEmpresa() != null) cliente.setEmpresa(request.getEmpresa());
        if (request.getIngresoMensual() != null) cliente.setIngresoMensual(request.getIngresoMensual());

        cliente = clienteRepository.save(cliente);

        Domicilio domicilio = domicilioRepository.findByClienteId(id).orElse(null);
        if (request.getDomicilio() != null) {
            if (domicilio == null) {
                domicilio = mapearDomicilio(request.getDomicilio(), id);
            } else {
                actualizarDomicilio(domicilio, request.getDomicilio());
            }
            domicilio = domicilioRepository.save(domicilio);
        }

        String numeroCuenta = cuentaRepository.findByClienteId(id)
                .map(Cuenta::getNumeroCuenta)
                .orElse(null);

        return construirResponse(cliente, domicilio, numeroCuenta);
    }

    @Override
    public ClienteResponse obtenerPorId(Long id) {
        Cliente cliente = buscarPorId(id);
        return construirResponseCompleto(cliente);
    }

    @Override
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(this::construirResponseCompleto)
                .collect(Collectors.toList());
    }

    @Override
    public ClienteResponse obtenerPorCurp(String curp) {
        Cliente cliente = clienteRepository.findByCurp(curp)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontro un cliente con esa CURP"));
        return construirResponseCompleto(cliente);
    }

    @Override
    public ClienteResponse obtenerPorRfc(String rfc) {
        Cliente cliente = clienteRepository.findByRfc(rfc)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontro un cliente con ese RFC"));
        return construirResponseCompleto(cliente);
    }

    @Override
    public ClienteResponse obtenerPorCorreo(String correo) {
        Cliente cliente = clienteRepository.findByCorreo(correo)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontro un cliente con ese correo"));
        return construirResponseCompleto(cliente);
    }

    @Override
    public List<ClienteResponse> listarActivos() {
        return clienteRepository.findByActivoTrue().stream()
                .map(this::construirResponseCompleto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteResponse> listarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        return clienteRepository.findByFechaRegistroBetween(desde, hasta).stream()
                .map(this::construirResponseCompleto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void darDeBaja(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
        // Regla de negocio: solo clientes activos pueden tener cuentas activas
        cuentaService.desactivarCuentaDeCliente(id);
        log.info("Cliente id={} dado de baja (baja logica)", id);
    }

    @Override
    public ClienteResponse obtenerPorNumeroCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException("No se encontro una cuenta con ese numero"));
        Cliente cliente = buscarPorId(cuenta.getClienteId());
        return construirResponse(cliente, domicilioRepository.findByClienteId(cliente.getId()).orElse(null),
                numeroCuenta);
    }

    private Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se encontro el cliente con id " + id));
    }

    private void validarMayoriaDeEdad(LocalDate fechaNacimiento) {
        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < EDAD_MINIMA) {
            throw new ValidacionNegocioException("El cliente debe ser mayor de edad (18 anios o mas)");
        }
    }

    private void copiarCamposBasicos(ClienteRequest request, Cliente cliente) {
        cliente.setNombre(request.getNombre());
        cliente.setSegundoNombre(request.getSegundoNombre());
        cliente.setApellidoPaterno(request.getApellidoPaterno());
        cliente.setApellidoMaterno(request.getApellidoMaterno());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setCurp(request.getCurp());
        cliente.setRfc(request.getRfc());
        cliente.setSexo(request.getSexo());
        cliente.setNacionalidad(request.getNacionalidad());
        cliente.setEstadoCivil(request.getEstadoCivil());
        cliente.setCorreo(request.getCorreo());
        cliente.setTelefonoMovil(request.getTelefonoMovil());
        cliente.setTelefonoAlternativo(request.getTelefonoAlternativo());
        cliente.setOcupacion(request.getOcupacion());
        cliente.setEmpresa(request.getEmpresa());
        cliente.setIngresoMensual(request.getIngresoMensual());
        cliente.setActivo(true);
    }

    private Domicilio mapearDomicilio(DomicilioRequest request, Long clienteId) {
        Domicilio domicilio = new Domicilio();
        domicilio.setClienteId(clienteId);
        actualizarDomicilio(domicilio, request);
        return domicilio;
    }

    private void actualizarDomicilio(Domicilio domicilio, DomicilioRequest request) {
        domicilio.setCalle(request.getCalle());
        domicilio.setNumeroExterior(request.getNumeroExterior());
        domicilio.setNumeroInterior(request.getNumeroInterior());
        domicilio.setColonia(request.getColonia());
        domicilio.setMunicipio(request.getMunicipio());
        domicilio.setEstado(request.getEstado());
        domicilio.setCodigoPostal(request.getCodigoPostal());
        domicilio.setPais(request.getPais());
    }

    private ClienteResponse construirResponseCompleto(Cliente cliente) {
        Domicilio domicilio = domicilioRepository.findByClienteId(cliente.getId()).orElse(null);
        String numeroCuenta = cuentaRepository.findByClienteId(cliente.getId())
                .map(Cuenta::getNumeroCuenta)
                .orElse(null);
        return construirResponse(cliente, domicilio, numeroCuenta);
    }

    private ClienteResponse construirResponse(Cliente cliente, Domicilio domicilio, String numeroCuenta) {
        ClienteResponse response = new ClienteResponse();
        response.setId(cliente.getId());
        response.setNombre(cliente.getNombre());
        response.setSegundoNombre(cliente.getSegundoNombre());
        response.setApellidoPaterno(cliente.getApellidoPaterno());
        response.setApellidoMaterno(cliente.getApellidoMaterno());
        response.setFechaNacimiento(cliente.getFechaNacimiento());
        response.setCurp(cliente.getCurp());
        response.setRfc(cliente.getRfc());
        response.setSexo(cliente.getSexo());
        response.setNacionalidad(cliente.getNacionalidad());
        response.setEstadoCivil(cliente.getEstadoCivil());
        response.setCorreo(cliente.getCorreo());
        response.setTelefonoMovil(cliente.getTelefonoMovil());
        response.setTelefonoAlternativo(cliente.getTelefonoAlternativo());
        response.setOcupacion(cliente.getOcupacion());
        response.setEmpresa(cliente.getEmpresa());
        response.setIngresoMensual(cliente.getIngresoMensual());
        response.setActivo(cliente.getActivo());
        response.setFechaRegistro(cliente.getFechaRegistro());
        response.setNumeroCuenta(numeroCuenta);

        if (domicilio != null) {
            DomicilioRequest domicilioDto = new DomicilioRequest();
            domicilioDto.setCalle(domicilio.getCalle());
            domicilioDto.setNumeroExterior(domicilio.getNumeroExterior());
            domicilioDto.setNumeroInterior(domicilio.getNumeroInterior());
            domicilioDto.setColonia(domicilio.getColonia());
            domicilioDto.setMunicipio(domicilio.getMunicipio());
            domicilioDto.setEstado(domicilio.getEstado());
            domicilioDto.setCodigoPostal(domicilio.getCodigoPostal());
            domicilioDto.setPais(domicilio.getPais());
            response.setDomicilio(domicilioDto);
        }

        return response;
    }
}
