package com.proyecto.servicios.service.cliente.Impl;

import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.entity.cliente.Saldo;
import com.proyecto.servicios.exception.cliente.CuentaNoEncontradaException;
import com.proyecto.servicios.model.cliente.CuentaResponse;
import com.proyecto.servicios.model.cliente.SaldoResponse;
import com.proyecto.servicios.repositorys.cliente.CuentaRepository;
import com.proyecto.servicios.repositorys.cliente.SaldoRepository;
import com.proyecto.servicios.service.cliente.CuentaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CuentaServiceImpl implements CuentaService {

    private static final String ESTATUS_ACTIVA = "ACTIVA";
    private static final String ESTATUS_INACTIVA = "INACTIVA";
    private static final String MOVIMIENTO_APERTURA = "APERTURA";

    private final CuentaRepository cuentaRepository;
    private final SaldoRepository saldoRepository;
    private final SecureRandom random = new SecureRandom();

    @Value("${cuentas.saldo-inicial}")
    private BigDecimal saldoInicial;

    public CuentaServiceImpl(CuentaRepository cuentaRepository, SaldoRepository saldoRepository) {
        this.cuentaRepository = cuentaRepository;
        this.saldoRepository = saldoRepository;
    }

    @Override
    @Transactional
    public Cuenta crearCuentaParaCliente(Long clienteId) {
        BigDecimal saldoApertura = dosDecimales(saldoInicial);

        Cuenta cuenta = new Cuenta();
        cuenta.setClienteId(clienteId);
        cuenta.setNumeroCuenta(generarNumeroCuentaUnico());
        cuenta.setEstatus(ESTATUS_ACTIVA);
        cuenta = cuentaRepository.save(cuenta);

        Saldo aperturaSaldo = new Saldo();
        aperturaSaldo.setCuentaId(cuenta.getId());
        aperturaSaldo.setTipoMovimiento(MOVIMIENTO_APERTURA);
        aperturaSaldo.setMonto(saldoApertura);
        aperturaSaldo.setSaldoResultante(saldoApertura);
        saldoRepository.save(aperturaSaldo);

        log.info("Cuenta creada para cliente id={} con saldo inicial {}", clienteId, saldoApertura);
        return cuenta;
    }

    @Override
    public CuentaResponse obtenerPorNumeroCuenta(String numeroCuenta) {
        Cuenta cuenta = buscarPorNumeroCuenta(numeroCuenta);
        return construirResponse(cuenta);
    }

    @Override
    public SaldoResponse obtenerSaldo(String numeroCuenta) {
        Cuenta cuenta = buscarPorNumeroCuenta(numeroCuenta);
        Saldo ultimo = saldoRepository.findFirstByCuentaIdOrderByFechaDesc(cuenta.getId())
                .orElseThrow(() -> new CuentaNoEncontradaException("La cuenta no tiene movimientos de saldo"));

        SaldoResponse response = new SaldoResponse();
        response.setNumeroCuenta(numeroCuenta);
        response.setSaldoActual(dosDecimales(ultimo.getSaldoResultante()));
        if (ultimo.getFecha() != null) {
            response.setFechaUltimoMovimiento(ultimo.getFecha().toLocalDate());
            response.setHoraUltimoMovimiento(ultimo.getFecha().toLocalTime().withNano(0));
        }
        return response;
    }

    @Override
    public List<CuentaResponse> listarActivas() {
        return cuentaRepository.findByEstatus(ESTATUS_ACTIVA).stream()
                .map(this::construirResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void desactivarCuentaDeCliente(Long clienteId) {
        cuentaRepository.findByClienteId(clienteId).ifPresent(cuenta -> {
            cuenta.setEstatus(ESTATUS_INACTIVA);
            cuentaRepository.save(cuenta);
        });
    }

    private Cuenta buscarPorNumeroCuenta(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(
                        "No se encontro una cuenta con el numero proporcionado"));
    }

    private CuentaResponse construirResponse(Cuenta cuenta) {
        CuentaResponse response = new CuentaResponse();
        response.setNumeroCuenta(cuenta.getNumeroCuenta());
        response.setClienteId(cuenta.getClienteId());
        response.setEstatus(cuenta.getEstatus());
        if (cuenta.getFechaApertura() != null) {
            response.setFechaApertura(cuenta.getFechaApertura().toLocalDate());
            response.setHoraApertura(cuenta.getFechaApertura().toLocalTime().withNano(0));
        }

        saldoRepository.findFirstByCuentaIdOrderByFechaDesc(cuenta.getId())
                .ifPresent(saldo -> response.setSaldoActual(dosDecimales(saldo.getSaldoResultante())));

        return response;
    }

    /** Todas las cantidades se manejan con exactamente 2 decimales. */
    private BigDecimal dosDecimales(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }

    private String generarNumeroCuentaUnico() {
        String candidato;
        do {
            candidato = generarDigitos(10);
        } while (cuentaRepository.existsByNumeroCuenta(candidato));
        return candidato;
    }

    private String generarDigitos(int longitud) {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
