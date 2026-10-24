package com.proyecto.servicios.service.cliente.Impl;

import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.entity.cliente.Saldo;
import com.proyecto.servicios.exception.cliente.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.model.cliente.AbonoRequest;
import com.proyecto.servicios.model.cliente.AbonoResponse;
import com.proyecto.servicios.model.cliente.CuentaFiltroRequest;
import com.proyecto.servicios.model.cliente.CuentaResponse;
import com.proyecto.servicios.model.comun.PaginaResponse;
import com.proyecto.servicios.repositorys.cliente.CuentaSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.time.LocalDateTime;

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

    /**
     * Sin @Transactional propio: siempre se invoca desde ClienteServiceImpl.crear,
     * cuya transaccion cubre tambien la cuenta y el saldo de apertura.
     */
    @Override
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
    public PaginaResponse<CuentaResponse> buscar(CuentaFiltroRequest filtro) {
        PageRequest pagina = PageRequest.of(filtro.getPagina(), filtro.getTamano(), Sort.by("id"));
        Page<CuentaResponse> resultado = cuentaRepository
                .findAll(CuentaSpecifications.conFiltros(filtro), pagina)
                .map(this::construirResponse);
        return new PaginaResponse<>(resultado);
    }

    /** Optional: si el cliente tiene cuenta se desactiva; si no tiene, no pasa nada. */
    @Override
    public void desactivarCuentaDeCliente(Long clienteId) {
        cuentaRepository.findByClienteId(clienteId).ifPresent(cuenta -> {
            cuenta.setEstatus(ESTATUS_INACTIVA);
            cuentaRepository.save(cuenta);
        });
    }

    @Override
    @Transactional
    public AbonoResponse abonar(AbonoRequest request) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(request.getNumeroCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException(
                        "No se encontró la cuenta con número " + request.getNumeroCuenta()));

        if (!ESTATUS_ACTIVA.equalsIgnoreCase(cuenta.getEstatus())) {
            throw new ValidacionNegocioException(
                    "La cuenta " + request.getNumeroCuenta() + " está inactiva y no puede recibir abonos");
        }

        BigDecimal saldoActual = saldoRepository.findFirstByCuentaIdOrderByFechaDesc(cuenta.getId())
                .map(Saldo::getSaldoResultante)
                .orElse(BigDecimal.ZERO);

        BigDecimal montoAbono = request.getMonto().setScale(2, RoundingMode.HALF_UP);
        BigDecimal nuevoSaldo = saldoActual.add(montoAbono).setScale(2, RoundingMode.HALF_UP);

        String concepto = (request.getConcepto() != null && !request.getConcepto().trim().isEmpty())
                ? request.getConcepto().trim() : "ABONO";

        Saldo movimiento = new Saldo();
        movimiento.setCuentaId(cuenta.getId());
        movimiento.setTipoMovimiento(concepto);
        movimiento.setMonto(montoAbono);
        movimiento.setSaldoResultante(nuevoSaldo);
        movimiento.setFecha(LocalDateTime.now());
        saldoRepository.save(movimiento);

        log.info("Abono aplicado exitosamente. Cuenta={} Monto={} SaldoAnterior={} NuevoSaldo={}",
                cuenta.getNumeroCuenta(), montoAbono, saldoActual, nuevoSaldo);

        LocalDateTime ahora = LocalDateTime.now();
        return AbonoResponse.builder()
                .numeroCuenta(cuenta.getNumeroCuenta())
                .montoAbonado(montoAbono)
                .saldoAnterior(dosDecimales(saldoActual))
                .nuevoSaldo(nuevoSaldo)
                .concepto(concepto)
                .fecha(ahora.toLocalDate())
                .hora(ahora.toLocalTime().withNano(0))
                .build();
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
