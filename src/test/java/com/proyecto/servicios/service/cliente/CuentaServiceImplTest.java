package com.proyecto.servicios.service.cliente;

import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.entity.cliente.Saldo;
import com.proyecto.servicios.exception.cliente.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.model.cliente.AbonoRequest;
import com.proyecto.servicios.model.cliente.AbonoResponse;
import com.proyecto.servicios.model.cliente.CuentaFiltroRequest;
import com.proyecto.servicios.model.cliente.CuentaResponse;
import com.proyecto.servicios.model.comun.PaginaResponse;
import com.proyecto.servicios.repositorys.cliente.CuentaRepository;
import com.proyecto.servicios.repositorys.cliente.SaldoRepository;
import com.proyecto.servicios.service.cliente.Impl.CuentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private SaldoRepository saldoRepository;

    @InjectMocks
    private CuentaServiceImpl cuentaService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cuentaService, "saldoInicial", new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("crearCuentaParaCliente genera cuenta activa con saldo de apertura")
    void crearCuentaParaClienteExitoso() {
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(invocation -> {
            Cuenta c = invocation.getArgument(0);
            c.setId(100L);
            return c;
        });

        Cuenta creada = cuentaService.crearCuentaParaCliente(1L);

        assertNotNull(creada);
        assertEquals(1L, creada.getClienteId());
        assertEquals("ACTIVA", creada.getEstatus());
        assertNotNull(creada.getNumeroCuenta());

        ArgumentCaptor<Saldo> saldoCaptor = ArgumentCaptor.forClass(Saldo.class);
        verify(saldoRepository).save(saldoCaptor.capture());
        assertEquals(100L, saldoCaptor.getValue().getCuentaId());
        assertEquals("APERTURA", saldoCaptor.getValue().getTipoMovimiento());
        assertEquals(new BigDecimal("0.00"), saldoCaptor.getValue().getMonto());
    }

    @Test
    @DisplayName("buscar cuentas con filtro retorna pagina con saldo actual")
    void buscarCuentasExitoso() {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(10L);
        cuenta.setClienteId(1L);
        cuenta.setNumeroCuenta("1234567890");
        cuenta.setEstatus("ACTIVA");
        cuenta.setFechaApertura(LocalDateTime.of(2026, 1, 1, 10, 0, 0));

        Saldo saldo = new Saldo();
        saldo.setSaldoResultante(new BigDecimal("150.50"));

        when(cuentaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cuenta)));
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L))
                .thenReturn(Optional.of(saldo));

        CuentaFiltroRequest filtro = new CuentaFiltroRequest();
        filtro.setPagina(0);
        filtro.setTamano(10);

        PaginaResponse<CuentaResponse> response = cuentaService.buscar(filtro);

        assertNotNull(response);
        assertEquals(1, response.getContenido().size());
        assertEquals("1234567890", response.getContenido().get(0).getNumeroCuenta());
        assertEquals(new BigDecimal("150.50"), response.getContenido().get(0).getSaldoActual());
    }

    @Test
    @DisplayName("desactivarCuentaDeCliente desactiva la cuenta si existe")
    void desactivarCuentaExistente() {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(10L);
        cuenta.setEstatus("ACTIVA");

        when(cuentaRepository.findByClienteId(10L)).thenReturn(Optional.of(cuenta));

        cuentaService.desactivarCuentaDeCliente(10L);

        assertEquals("INACTIVA", cuenta.getEstatus());
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    @DisplayName("desactivarCuentaDeCliente no falla si no existe cuenta")
    void desactivarCuentaNoExistente() {
        when(cuentaRepository.findByClienteId(999L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> cuentaService.desactivarCuentaDeCliente(999L));
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("abonar suma correctamente al saldo anterior y guarda registro")
    void abonarExitoso() {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(20L);
        cuenta.setNumeroCuenta("9876543210");
        cuenta.setEstatus("ACTIVA");

        Saldo saldoAnterior = new Saldo();
        saldoAnterior.setSaldoResultante(new BigDecimal("200.00"));

        when(cuentaRepository.findByNumeroCuenta("9876543210")).thenReturn(Optional.of(cuenta));
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(20L)).thenReturn(Optional.of(saldoAnterior));

        AbonoRequest request = new AbonoRequest();
        request.setNumeroCuenta("9876543210");
        request.setMonto(new BigDecimal("50.00"));
        request.setConcepto("Depósito quincena");

        AbonoResponse response = cuentaService.abonar(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("50.00"), response.getMontoAbonado());
        assertEquals(new BigDecimal("200.00"), response.getSaldoAnterior());
        assertEquals(new BigDecimal("250.00"), response.getNuevoSaldo());
        assertEquals("Depósito quincena", response.getConcepto());

        ArgumentCaptor<Saldo> captor = ArgumentCaptor.forClass(Saldo.class);
        verify(saldoRepository).save(captor.capture());
        assertEquals(new BigDecimal("250.00"), captor.getValue().getSaldoResultante());
    }

    @Test
    @DisplayName("abonar usa concepto por defecto si viene vacio")
    void abonarConceptoPorDefecto() {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(20L);
        cuenta.setNumeroCuenta("9876543210");
        cuenta.setEstatus("ACTIVA");

        when(cuentaRepository.findByNumeroCuenta("9876543210")).thenReturn(Optional.of(cuenta));
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(20L)).thenReturn(Optional.empty());

        AbonoRequest request = new AbonoRequest();
        request.setNumeroCuenta("9876543210");
        request.setMonto(new BigDecimal("100.00"));
        request.setConcepto(null);

        AbonoResponse response = cuentaService.abonar(request);

        assertEquals("ABONO", response.getConcepto());
        assertEquals(new BigDecimal("100.00"), response.getNuevoSaldo());
    }

    @Test
    @DisplayName("abonar lanza excepcion si la cuenta no existe")
    void abonarCuentaNoExiste() {
        when(cuentaRepository.findByNumeroCuenta("0000000000")).thenReturn(Optional.empty());

        AbonoRequest request = new AbonoRequest();
        request.setNumeroCuenta("0000000000");
        request.setMonto(new BigDecimal("10.00"));

        assertThrows(CuentaNoEncontradaException.class, () -> cuentaService.abonar(request));
        verify(saldoRepository, never()).save(any());
    }

    @Test
    @DisplayName("abonar lanza excepcion si la cuenta esta inactiva")
    void abonarCuentaInactiva() {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("1111222233");
        cuenta.setEstatus("INACTIVA");

        when(cuentaRepository.findByNumeroCuenta("1111222233")).thenReturn(Optional.of(cuenta));

        AbonoRequest request = new AbonoRequest();
        request.setNumeroCuenta("1111222233");
        request.setMonto(new BigDecimal("10.00"));

        assertThrows(ValidacionNegocioException.class, () -> cuentaService.abonar(request));
        verify(saldoRepository, never()).save(any());
    }
}
