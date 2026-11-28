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
import com.proyecto.servicios.model.transaccion.ComprobantePagoCorreo;
import com.proyecto.servicios.model.transaccion.HistorialTransaccionesRequest;
import com.proyecto.servicios.model.transaccion.MovimientoHistorialResponse;
import com.proyecto.servicios.model.transaccion.PagoRequest;
import com.proyecto.servicios.model.transaccion.PagoResponse;
import com.proyecto.servicios.model.transaccion.Solicitar2faRequest;
import com.proyecto.servicios.model.transaccion.Solicitar2faResponse;
import com.proyecto.servicios.model.transaccion.TipoDestinoTransaccion;
import com.proyecto.servicios.model.transaccion.ValidarReferenciaRequest;
import com.proyecto.servicios.model.transaccion.ValidarReferenciaResponse;
import com.proyecto.servicios.repositorys.cliente.ClienteRepository;
import com.proyecto.servicios.repositorys.cliente.CuentaRepository;
import com.proyecto.servicios.repositorys.cliente.SaldoRepository;
import com.proyecto.servicios.repositorys.producto.CatalogoProductosRepository;
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
import java.util.List;
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

    @Mock
    private ServicioNotificacion2FA servicio2fa;

    @Mock
    private CatalogoProductosRepository catalogoProductosRepository;

    @Mock
    private ServicioNotificacionPago servicioNotificacionPago;

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
        ReflectionTestUtils.setField(transaccionService, "idDispositivo", "GPS83-TPV-17");
        ReflectionTestUtils.setField(transaccionService, "expiracionMinutos", 5);
        ReflectionTestUtils.setField(transaccionService, "mostrarCodigoEnRespuesta", true);

        cuentaMock = new Cuenta();
        cuentaMock.setId(10L);
        cuentaMock.setClienteId(1L);
        cuentaMock.setNumeroCuenta("1452864812");
        cuentaMock.setEstatus("ACTIVA");

        clienteMock = new Cliente();
        clienteMock.setId(1L);
        clienteMock.setNombre("Carlos");
        clienteMock.setCorreo("carlos@correo.com");
        clienteMock.setActivo(true);

        saldoMock = new Saldo();
        saldoMock.setCuentaId(10L);
        saldoMock.setSaldoResultante(new BigDecimal("500.00"));
    }

    private PagoRequest pagoTelefono(String monto, String codigo) {
        PagoRequest request = new PagoRequest();
        request.setNumeroCuenta("1452864812");
        request.setIdServicio(133);
        request.setIdProducto(582);
        request.setMonto(new BigDecimal(monto));
        request.setTipoDestino(TipoDestinoTransaccion.TELEFONO);
        request.setTelefono("5512345678");
        request.setCodigo2fa(codigo);
        return request;
    }

    private GestoPagoTxResponse respuestaGestoPago(String codigo, String texto) {
        GestoPagoTxResponse txResponse = new GestoPagoTxResponse();
        txResponse.setIdTx("291437188");
        txResponse.setNumAutorizacion("100024988");
        MensajeDTO mensaje = new MensajeDTO();
        mensaje.setCodigo(codigo);
        mensaje.setTexto(texto);
        txResponse.setMensaje(mensaje);
        return txResponse;
    }

    private void cuentaYClienteValidos() {
        when(cuentaRepository.findByNumeroCuenta("1452864812")).thenReturn(Optional.of(cuentaMock));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteMock));
    }

    @Test
    @DisplayName("Solicitar 2FA genera el código, lo envía al correo del cliente y enmascara el correo")
    void solicitar2faEnviaCorreo() {
        cuentaYClienteValidos();
        when(servicio2fa.generarYEnviarCodigo("1452864812", "carlos@correo.com")).thenReturn("482915");
        when(servicio2fa.enmascararCorreo("carlos@correo.com")).thenReturn("c***s@correo.com");

        Solicitar2faRequest request = new Solicitar2faRequest();
        request.setNumeroCuenta("1452864812");

        Solicitar2faResponse resultado = transaccionService.solicitarCodigo2fa(request);

        assertEquals("c***s@correo.com", resultado.getCorreoEnmascarado());
        assertEquals("482915", resultado.getCodigoParaPruebas());
        assertEquals(5, resultado.getExpiracionMinutos());
        verify(servicio2fa).generarYEnviarCodigo("1452864812", "carlos@correo.com");
    }

    @Test
    @DisplayName("Pago exitoso con bandera TELEFONO descuenta saldo y retorna autorización")
    void pagoExitosoPorTelefono() {
        cuentaYClienteValidos();
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L)).thenReturn(Optional.of(saldoMock));
        when(servicio2fa.validarYConsumirCodigo("1452864812", "482915")).thenReturn(true);
        when(gestoPagoTxClient.enviarTransaccion(anyString(), anyString()))
                .thenReturn(respuestaGestoPago("01", "Operacion realizada con exito"));

        PagoResponse resultado = transaccionService.procesarPago(pagoTelefono("100.00", "482915"));

        assertEquals("291437188", resultado.getIdTx());
        assertEquals("100024988", resultado.getNumAutorizacion());
        assertEquals(new BigDecimal("400.00"), resultado.getSaldoRestante());
        assertEquals(TipoDestinoTransaccion.TELEFONO, resultado.getTipoDestino());
        assertEquals("5512345678", resultado.getDestino());

        ArgumentCaptor<Saldo> captor = ArgumentCaptor.forClass(Saldo.class);
        verify(saldoRepository).save(captor.capture());
        assertEquals(new BigDecimal("400.00"), captor.getValue().getSaldoResultante());
        assertEquals("PAGO_TELEFONO", captor.getValue().getTipoMovimiento());

        ArgumentCaptor<ComprobantePagoCorreo> correo = ArgumentCaptor.forClass(ComprobantePagoCorreo.class);
        verify(servicioNotificacionPago).enviarComprobante(correo.capture());
        assertEquals("carlos@correo.com", correo.getValue().getCorreoDestino());
        assertEquals(new BigDecimal("100.00"), correo.getValue().getMonto());
        assertEquals("291437188", correo.getValue().getIdTx());
        assertTrue(correo.getValue().getUpc().matches("^\\d{10}$"));
        assertEquals("GPS83-TPV-17", resultado.getIdDispositivo());
        assertEquals(133, resultado.getIdServicio());
        assertEquals(582, resultado.getIdProducto());
        assertNotNull(resultado.getHoraLocal());
        assertNotNull(resultado.getLeyenda());
    }

    @Test
    @DisplayName("Si GestoPago responde -1 genera ticket OPERACION RECHAZADA sin descontar saldo y envia correo")
    void gestoPagoRespondeMenosUnoGeneraTicketRechazado() {
        cuentaYClienteValidos();
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L)).thenReturn(Optional.of(saldoMock));
        when(servicio2fa.validarYConsumirCodigo("1452864812", "482915")).thenReturn(true);
        when(gestoPagoTxClient.enviarTransaccion(anyString(), anyString()))
                .thenReturn(respuestaGestoPago("-1", "Transaccion rechazada"));

        PagoResponse resultado = transaccionService.procesarPago(pagoTelefono("100.00", "482915"));

        assertFalse(resultado.isAprobada());
        assertEquals("RECHAZADA", resultado.getEstado());
        assertEquals("-1", resultado.getCodigoProveedor());
        assertEquals("Transacción no válida", resultado.getMensajeProveedor());
        assertTrue(resultado.getUpc().matches("^\\d{10}$"));
        assertEquals("GPS83-TPV-17", resultado.getIdDispositivo());
        assertEquals(new BigDecimal("500.00"), resultado.getSaldoRestante());
        assertNotNull(resultado.getLeyenda());

        verify(saldoRepository, never()).save(any());
        ArgumentCaptor<ComprobantePagoCorreo> correo = ArgumentCaptor.forClass(ComprobantePagoCorreo.class);
        verify(servicioNotificacionPago).enviarComprobante(correo.capture());
        assertFalse(correo.getValue().isAprobada());
        assertEquals("-1", correo.getValue().getCodigoRespuesta());
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

        cuentaYClienteValidos();
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L)).thenReturn(Optional.of(saldoMock));
        when(servicio2fa.validarYConsumirCodigo("1452864812", "654321")).thenReturn(true);
        when(gestoPagoTxClient.enviarTransaccion(anyString(), anyString()))
                .thenReturn(respuestaGestoPago("01", "Operacion realizada con exito"));

        PagoResponse resultado = transaccionService.procesarPago(request);

        assertEquals(new BigDecimal("250.00"), resultado.getSaldoRestante());
        assertEquals(TipoDestinoTransaccion.TRANSFERENCIA, resultado.getTipoDestino());
        assertEquals("RECIBO-CFE-001", resultado.getDestino());
    }

    @Test
    @DisplayName("Falla cuando la cuenta no existe")
    void fallaCuentaInexistente() {
        when(cuentaRepository.findByNumeroCuenta("1452864812")).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class,
                () -> transaccionService.procesarPago(pagoTelefono("10.00", "123456")));
        verify(saldoRepository, never()).save(any());
        verify(gestoPagoTxClient, never()).enviarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Falla cuando el cliente está dado de baja")
    void fallaClienteInactivo() {
        clienteMock.setActivo(false);
        cuentaYClienteValidos();

        assertThrows(ClienteInactivoException.class,
                () -> transaccionService.procesarPago(pagoTelefono("10.00", "123456")));
        verify(saldoRepository, never()).save(any());
        verify(gestoPagoTxClient, never()).enviarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Falla cuando el código 2FA no coincide con el enviado por correo")
    void fallaCodigo2faIncorrecto() {
        cuentaYClienteValidos();
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L)).thenReturn(Optional.of(saldoMock));
        when(servicio2fa.validarYConsumirCodigo("1452864812", "111111")).thenReturn(false);

        assertThrows(Codigo2faInvalidoException.class,
                () -> transaccionService.procesarPago(pagoTelefono("10.00", "111111")));
        verify(saldoRepository, never()).save(any());
        verify(gestoPagoTxClient, never()).enviarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Falla por saldo insuficiente sin consumir el código 2FA")
    void fallaSaldoInsuficiente() {
        cuentaYClienteValidos();
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L)).thenReturn(Optional.of(saldoMock));

        assertThrows(SaldoInsuficienteException.class,
                () -> transaccionService.procesarPago(pagoTelefono("1000.00", "123456")));
        verify(servicio2fa, never()).validarYConsumirCodigo(any(), any());
        verify(saldoRepository, never()).save(any());
        verify(gestoPagoTxClient, never()).enviarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Falla si el monto no coincide con el precio fijo del paquete")
    void fallaMontoNoCoincideConPrecioFijoPaquete() {
        cuentaYClienteValidos();
        PagoRequest req = pagoTelefono("150.00", "123456");
        req.setIdServicio(133);
        req.setIdProducto(1333); // Telcel Paquete Amigo Sin Limite $200

        assertThrows(com.proyecto.servicios.exception.cliente.ValidacionNegocioException.class,
                () -> transaccionService.procesarPago(req));
        verify(servicio2fa, never()).validarYConsumirCodigo(any(), any());
        verify(saldoRepository, never()).save(any());
        verify(gestoPagoTxClient, never()).enviarTransaccion(any(), any());
    }

    @Test
    @DisplayName("Si GestoPago rechaza la transacción NO se descuenta saldo")
    void fallaGestoPagoRechazado() {
        cuentaYClienteValidos();
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L)).thenReturn(Optional.of(saldoMock));
        when(servicio2fa.validarYConsumirCodigo("1452864812", "123456")).thenReturn(true);
        when(gestoPagoTxClient.enviarTransaccion(anyString(), anyString()))
                .thenReturn(respuestaGestoPago("02", "Saldo insuficiente del distribuidor en GestoPago"));

        assertThrows(TransaccionRechazadaException.class,
                () -> transaccionService.procesarPago(pagoTelefono("100.00", "123456")));
        verify(saldoRepository, never()).save(any());
    }

    @Test
    @DisplayName("En modo simulación, si GestoPago falla por red/IP se aprueba simulado y descuenta saldo")
    void modoSimulacionApruebaCuandoGestoPagoFalla() {
        ReflectionTestUtils.setField(transaccionService, "modoPruebasAprobar", true);
        cuentaYClienteValidos();
        when(saldoRepository.findFirstByCuentaIdOrderByFechaDesc(10L)).thenReturn(Optional.of(saldoMock));
        when(servicio2fa.validarYConsumirCodigo("1452864812", "482915")).thenReturn(true);
        when(gestoPagoTxClient.enviarTransaccion(anyString(), anyString()))
                .thenThrow(new RuntimeException("IP no autorizada en GestoPago"));

        PagoResponse resultado = transaccionService.procesarPago(pagoTelefono("100.00", "482915"));

        assertNotNull(resultado.getIdTx());
        assertTrue(resultado.getIdTx().startsWith("SIM-"));
        assertTrue(resultado.getNumAutorizacion().startsWith("AUTH-"));
        assertEquals(new BigDecimal("400.00"), resultado.getSaldoRestante());
        verify(saldoRepository).save(any(Saldo.class));
    }

    @Test
    @DisplayName("Validar referencia de telefono valida exitosamente")
    void validarReferenciaTelefonoExito() {
        ValidarReferenciaRequest req = new ValidarReferenciaRequest();
        req.setIdServicio(133);
        req.setIdProducto(582);
        req.setTipoDestino(TipoDestinoTransaccion.TELEFONO);
        req.setReferencia("5512345678");

        ValidarReferenciaResponse resp = transaccionService.validarReferencia(req);
        assertTrue(resp.isValida());
        assertEquals("5512345678", resp.getReferencia());
    }

    @Test
    @DisplayName("Validar referencia de telefono menor a 10 digitos es rechazada")
    void validarReferenciaTelefonoInvalida() {
        ValidarReferenciaRequest req = new ValidarReferenciaRequest();
        req.setIdServicio(133);
        req.setIdProducto(582);
        req.setTipoDestino(TipoDestinoTransaccion.TELEFONO);
        req.setReferencia("123");

        ValidarReferenciaResponse resp = transaccionService.validarReferencia(req);
        assertFalse(resp.isValida());
        assertTrue(resp.getMensaje().contains("10"));
    }

    @Test
    @DisplayName("Validar referencia de telefono con 10 digitos repetidos (1111111111) es rechazada")
    void validarReferenciaTelefonoRepetidoInvalida() {
        ValidarReferenciaRequest req = new ValidarReferenciaRequest();
        req.setIdServicio(133);
        req.setIdProducto(1331);
        req.setTipoDestino(TipoDestinoTransaccion.TELEFONO);
        req.setReferencia("1111111111");

        ValidarReferenciaResponse resp = transaccionService.validarReferencia(req);
        assertFalse(resp.isValida());
    }

    @Test
    @DisplayName("Validar referencia de servicio valida exitosamente")
    void validarReferenciaServicioExito() {
        ValidarReferenciaRequest req = new ValidarReferenciaRequest();
        req.setIdServicio(133);
        req.setIdProducto(582);
        req.setTipoDestino(TipoDestinoTransaccion.TRANSFERENCIA);
        req.setReferencia("018001234567");

        ValidarReferenciaResponse resp = transaccionService.validarReferencia(req);
        assertTrue(resp.isValida());
        assertNotNull(resp.getAdeudoSugerido());
    }

    @Test
    @DisplayName("Obtener historial de transacciones regresa movimientos mapeados")
    void obtenerHistorialExito() {
        when(cuentaRepository.findByNumeroCuenta("1452864812")).thenReturn(Optional.of(cuentaMock));
        Saldo saldoMovimiento = new Saldo();
        saldoMovimiento.setCuentaId(10L);
        saldoMovimiento.setTipoMovimiento("CARGO");
        saldoMovimiento.setMonto(new BigDecimal("250.00"));
        saldoMovimiento.setSaldoResultante(new BigDecimal("750.00"));
        saldoMovimiento.setConcepto("Pago CFE");
        saldoMovimiento.setReferencia("0123456789");
        saldoMovimiento.setFolio("UPC-987654");
        saldoMovimiento.setEstado("EXITOSA");
        saldoMovimiento.setIdServicio(582);
        saldoMovimiento.setIdProducto(133);
        saldoMovimiento.setFecha(java.time.LocalDateTime.now());

        when(saldoRepository.findByCuentaIdOrderByFechaDesc(10L))
                .thenReturn(List.of(saldoMovimiento));
        when(servicioNotificacionPago.construirUrlLogo(any(), any(), any(), any()))
                .thenReturn("https://cdn.logo.png");

        HistorialTransaccionesRequest req = new HistorialTransaccionesRequest();
        req.setNumeroCuenta("1452864812");

        List<MovimientoHistorialResponse> lista = transaccionService.obtenerHistorial(req);
        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("Pago CFE", lista.get(0).getConcepto());
        assertEquals("UPC-987654", lista.get(0).getFolio());
    }
}
