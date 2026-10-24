package com.proyecto.servicios.service.transaccion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.GestoPagoTxClient;
import com.proyecto.servicios.entity.cliente.Cliente;
import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.entity.cliente.Saldo;
import com.proyecto.servicios.exception.cliente.ClienteInactivoException;
import com.proyecto.servicios.exception.cliente.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.transaccion.Codigo2faInvalidoException;
import com.proyecto.servicios.exception.transaccion.SaldoInsuficienteException;
import com.proyecto.servicios.exception.transaccion.TransaccionRechazadaException;
import com.proyecto.servicios.model.gestopago.producto.MensajeDTO;
import com.proyecto.servicios.model.gestopago.tx.GestoPagoTxResponse;
import com.proyecto.servicios.model.transaccion.PagoRequest;
import com.proyecto.servicios.model.transaccion.PagoResponse;
import com.proyecto.servicios.model.transaccion.TipoDestinoTransaccion;
import com.proyecto.servicios.repositorys.cliente.ClienteRepository;
import com.proyecto.servicios.repositorys.cliente.CuentaRepository;
import com.proyecto.servicios.repositorys.cliente.SaldoRepository;
import com.proyecto.servicios.service.transaccion.Impl.TransaccionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SaldoRepository saldoRepository;

    @Mock
    private GestoPagoTxClient gestoPagoTxClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private TransaccionServiceImpl transaccionService;

    private Cuenta cuentaMock;
    private Cliente clienteMock;
    private Saldo saldoMock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transaccionService, "secretKey", "HPo7OLqB4Fkk4E2yGOtwqw8H5fHR9kNx67OR4g4UdlA=");
        ReflectionTestUtils.setField(transaccionService, "iv", "p5ldmBPdd/9pjC0bDC/nSg==");
        ReflectionTestUtils.setField(transaccionService, "unidad", "083");

        cuentaMock = new Cuenta();
        cuentaMock.setId(10L);
        cuentaMock.setClienteId(1L);
        cuentaMock.setNumeroCuenta("1452864812");
        cuentaMock.setEstatus("ACTIVA");

        clienteMock = new Cliente();
        clienteMock.setId(1L);
        clienteMock.setNombre("Carlos");
        clienteMock.setActivo(true);

        saldoMock = new Saldo();
        saldoMock.setCuentaId(10L);
        saldoMock.setSaldoResultante(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Pago exitoso con bandera TELEFONO descuenta saldo y retorna autorización")
    void pagoExitosoPorTelefono() {
        PagoRequest request = new PagoRequest();
        request.setNumeroCuenta("1452864812");
        request.setIdServicio(133);
        request.setIdProducto(582);
        request.setMonto(new BigDecimal("100.00"));
        request.setTipoDestino(TipoDestinoTransaccion.TELEFONO);
        request.setTelefono("5512345678");
        request.setCodigo2fa("123456");

        GestoPagoTxResponse txResponse = new GestoPagoTxResponse();
        txResponse.setIdTx("291437188");
        txResponse.setNumAutorizacion("100024988");
        MensajeDTO mensaje = new MensajeDTO();
        mensaje.setCodigo("01");
        mensaje.setTexto("Operacion realizada con exito");
        txResponse.setMensaje(mensaje);

        when(cuentaRepository.findByNumeroCuenta("1452864812")).thenReturn(Optional.of(cuentaMock));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteMock));
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L)).thenReturn(Optional.of(saldoMock));
        when(gestoPagoTxClient.enviarTransaccion(anyString(), anyString())).thenReturn(txResponse);

        PagoResponse resultado = transaccionService.procesarPago(request);

        assertNotNull(resultado);
        assertEquals("291437188", resultado.getIdTx());
        assertEquals("100024988", resultado.getNumAutorizacion());
        assertEquals(new BigDecimal("400.00"), resultado.getSaldoRestante());
        assertEquals(TipoDestinoTransaccion.TELEFONO, resultado.getTipoDestino());
        assertEquals("5512345678", resultado.getDestino());

        ArgumentCaptor<Saldo> captor = ArgumentCaptor.forClass(Saldo.class);
        verify(saldoRepository).save(captor.capture());
        assertEquals(new BigDecimal("400.00"), captor.getValue().getSaldoResultante());
        assertEquals("PAGO_TELEFONO", captor.getValue().getTipoMovimiento());
    }

    @Test
    @DisplayName("Pago exitoso con bandera TRANSFERENCIA descuenta saldo y envía referencia")
    void pagoExitosoPorTransferencia() {
        PagoRequest request = new PagoRequest();
        request.setNumeroCuenta("1452864812");
        request.setIdServicio(133);
        request.setIdProducto(582);
        request.setMonto(new BigDecimal("250.00"));
        request.setTipoDestino(TipoDestinoTransaccion.TRANSFERENCIA);
        request.setReferencia("RECIBO-CFE-001");
        request.setCodigo2fa("654321");

        GestoPagoTxResponse txResponse = new GestoPagoTxResponse();
        txResponse.setIdTx("998877");
        txResponse.setNumAutorizacion("AUT-8877");
        MensajeDTO mensaje = new MensajeDTO();
        mensaje.setCodigo("01");
        mensaje.setTexto("Operacion realizada con exito");
        txResponse.setMensaje(mensaje);

        when(cuentaRepository.findByNumeroCuenta("1452864812")).thenReturn(Optional.of(cuentaMock));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteMock));
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L)).thenReturn(Optional.of(saldoMock));
        when(gestoPagoTxClient.enviarTransaccion(anyString(), anyString())).thenReturn(txResponse);

        PagoResponse resultado = transaccionService.procesarPago(request);

        assertNotNull(resultado);
        assertEquals("998877", resultado.getIdTx());
        assertEquals(new BigDecimal("250.00"), resultado.getSaldoRestante());
        assertEquals(TipoDestinoTransaccion.TRANSFERENCIA, resultado.getTipoDestino());
        assertEquals("RECIBO-CFE-001", resultado.getDestino());
    }

    @Test
    @DisplayName("Falla cuando la cuenta no existe")
    void fallaCuentaInexistente() {
        PagoRequest request = new PagoRequest();
        request.setNumeroCuenta("9999999999");
        when(cuentaRepository.findByNumeroCuenta("9999999999")).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class, () -> transaccionService.procesarPago(request));
        verify(saldoRepository, never()).save(any());
        verify(gestoPagoTxClient, never()).enviarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Falla cuando el cliente está dado de baja")
    void fallaClienteInactivo() {
        clienteMock.setActivo(false);
        PagoRequest request = new PagoRequest();
        request.setNumeroCuenta("1452864812");

        when(cuentaRepository.findByNumeroCuenta("1452864812")).thenReturn(Optional.of(cuentaMock));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteMock));

        assertThrows(ClienteInactivoException.class, () -> transaccionService.procesarPago(request));
        verify(saldoRepository, never()).save(any());
        verify(gestoPagoTxClient, never()).enviarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Falla cuando el código 2FA es incorrecto")
    void fallaCodigo2faIncorrecto() {
        PagoRequest request = new PagoRequest();
        request.setNumeroCuenta("1452864812");
        request.setCodigo2fa("000000");

        when(cuentaRepository.findByNumeroCuenta("1452864812")).thenReturn(Optional.of(cuentaMock));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteMock));

        assertThrows(Codigo2faInvalidoException.class, () -> transaccionService.procesarPago(request));
        verify(saldoRepository, never()).save(any());
        verify(gestoPagoTxClient, never()).enviarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Falla cuando no hay saldo suficiente en la cuenta")
    void fallaSaldoInsuficiente() {
        PagoRequest request = new PagoRequest();
        request.setNumeroCuenta("1452864812");
        request.setMonto(new BigDecimal("1000.00")); // saldo actual es 500
        request.setCodigo2fa("123456");

        when(cuentaRepository.findByNumeroCuenta("1452864812")).thenReturn(Optional.of(cuentaMock));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteMock));
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L)).thenReturn(Optional.of(saldoMock));

        assertThrows(SaldoInsuficienteException.class, () -> transaccionService.procesarPago(request));
        verify(saldoRepository, never()).save(any());
        verify(gestoPagoTxClient, never()).enviarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Si GestoPago rechaza la transacción, se lanza TransaccionRechazadaException y NO se descuenta saldo")
    void fallaGestoPagoRechazado() {
        PagoRequest request = new PagoRequest();
        request.setNumeroCuenta("1452864812");
        request.setIdServicio(133);
        request.setIdProducto(582);
        request.setMonto(new BigDecimal("100.00"));
        request.setTipoDestino(TipoDestinoTransaccion.TELEFONO);
        request.setTelefono("5512345678");
        request.setCodigo2fa("123456");

        GestoPagoTxResponse txResponse = new GestoPagoTxResponse();
        MensajeDTO mensaje = new MensajeDTO();
        mensaje.setCodigo("02");
        mensaje.setTexto("Saldo insuficiente del distribuidor en GestoPago");
        txResponse.setMensaje(mensaje);

        when(cuentaRepository.findByNumeroCuenta("1452864812")).thenReturn(Optional.of(cuentaMock));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteMock));
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L)).thenReturn(Optional.of(saldoMock));
        when(gestoPagoTxClient.enviarTransaccion(anyString(), anyString())).thenReturn(txResponse);

        assertThrows(TransaccionRechazadaException.class, () -> transaccionService.procesarPago(request));
        verify(saldoRepository, never()).save(any());
    }
}
