package com.proyecto.servicios.service.transaccion.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.GestoPagoTxClient;
import com.proyecto.servicios.entity.cliente.Cliente;
import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.entity.cliente.Saldo;
import com.proyecto.servicios.entity.producto.CatalogoProductosEntity;
import com.proyecto.servicios.exception.cliente.ClienteInactivoException;
import com.proyecto.servicios.exception.cliente.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.exception.transaccion.Codigo2faInvalidoException;
import com.proyecto.servicios.exception.transaccion.SaldoInsuficienteException;
import com.proyecto.servicios.exception.transaccion.TransaccionRechazadaException;
import com.proyecto.servicios.model.gestopago.tx.GestoPagoTxPayload;
import com.proyecto.servicios.model.gestopago.tx.GestoPagoTxResponse;
import com.proyecto.servicios.model.transaccion.ComprobantePagoCorreo;
import com.proyecto.servicios.model.transaccion.PagoRequest;
import com.proyecto.servicios.model.transaccion.PagoResponse;
import com.proyecto.servicios.model.transaccion.Solicitar2faRequest;
import com.proyecto.servicios.model.transaccion.Solicitar2faResponse;
import com.proyecto.servicios.model.transaccion.TipoDestinoTransaccion;
import com.proyecto.servicios.repositorys.cliente.ClienteRepository;
import com.proyecto.servicios.repositorys.cliente.CuentaRepository;
import com.proyecto.servicios.repositorys.cliente.SaldoRepository;
import com.proyecto.servicios.repositorys.producto.CatalogoProductosRepository;
import com.proyecto.servicios.security.GestoPagoCifradoUtil;
import com.proyecto.servicios.service.transaccion.ServicioNotificacion2FA;
import com.proyecto.servicios.service.transaccion.ServicioNotificacionPago;
import com.proyecto.servicios.service.transaccion.TransaccionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
public class TransaccionServiceImpl implements TransaccionService {

    private static final String ESTATUS_ACTIVA = "ACTIVA";
    private static final String CODIGO_EXITO_PROVEEDOR = "01";
    private static final DateTimeFormatter FORMATO_HORA_LOCAL = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String CARACTERES_UPC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LONGITUD_UPC = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final SaldoRepository saldoRepository;
    private final GestoPagoTxClient gestoPagoTxClient;
    private final ObjectMapper objectMapper;
    private final ServicioNotificacion2FA servicio2fa;
    private final CatalogoProductosRepository catalogoProductosRepository;
    private final ServicioNotificacionPago servicioNotificacionPago;

    @Value("${gestopago.tx.secret-key}")
    private String secretKey;

    @Value("${gestopago.tx.iv}")
    private String iv;

    @Value("${gestopago.tx.unidad:083}")
    private String unidad;

    @Value("${seguridad.2fa.expiracion-minutos:5}")
    private int expiracionMinutos;

    /** Solo para desarrollo/pruebas: regresa el codigo en la respuesta. En produccion debe ir en false. */
    @Value("${seguridad.2fa.mostrar-codigo-en-respuesta:true}")
    private boolean mostrarCodigoEnRespuesta;

    public TransaccionServiceImpl(CuentaRepository cuentaRepository,
                                  ClienteRepository clienteRepository,
                                  SaldoRepository saldoRepository,
                                  GestoPagoTxClient gestoPagoTxClient,
                                  ObjectMapper objectMapper,
                                  ServicioNotificacion2FA servicio2fa,
                                  CatalogoProductosRepository catalogoProductosRepository,
                                  ServicioNotificacionPago servicioNotificacionPago) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.saldoRepository = saldoRepository;
        this.gestoPagoTxClient = gestoPagoTxClient;
        this.objectMapper = objectMapper;
        this.servicio2fa = servicio2fa;
        this.catalogoProductosRepository = catalogoProductosRepository;
        this.servicioNotificacionPago = servicioNotificacionPago;
    }

    @Override
    public Solicitar2faResponse solicitarCodigo2fa(Solicitar2faRequest request) {
        Cuenta cuenta = obtenerCuentaActiva(request.getNumeroCuenta());
        Cliente cliente = obtenerClienteActivo(cuenta);

        if (cliente.getCorreo() == null || cliente.getCorreo().isBlank()) {
            throw new ValidacionNegocioException(
                    "El cliente de la cuenta " + request.getNumeroCuenta() + " no tiene correo registrado para enviar el 2FA");
        }

        String codigo = servicio2fa.generarYEnviarCodigo(cuenta.getNumeroCuenta(), cliente.getCorreo());

        return Solicitar2faResponse.builder()
                .numeroCuenta(cuenta.getNumeroCuenta())
                .correoEnmascarado(servicio2fa.enmascararCorreo(cliente.getCorreo()))
                .mensaje("Código 2FA enviado al correo registrado. Válido por " + expiracionMinutos + " minutos.")
                .expiracionMinutos(expiracionMinutos)
                .codigoParaPruebas(mostrarCodigoEnRespuesta ? codigo : null)
                .build();
    }

    @Override
    @Transactional
    public PagoResponse procesarPago(PagoRequest request) {
        log.info("Iniciando procesamiento de pago para cuenta={} monto={} tipo={}",
                request.getNumeroCuenta(), request.getMonto(), request.getTipoDestino());

        // 1 y 2. Cuenta existente/activa y cliente activo
        Cuenta cuenta = obtenerCuentaActiva(request.getNumeroCuenta());
        Cliente cliente = obtenerClienteActivo(cuenta);

        // 3. Validar saldo suficiente (antes del 2FA para no consumir el codigo inutilmente)
        BigDecimal saldoActual = saldoRepository.findFirstByCuentaIdOrderByFechaDesc(cuenta.getId())
                .map(Saldo::getSaldoResultante)
                .orElse(BigDecimal.ZERO);

        BigDecimal montoPago = request.getMonto().setScale(2, RoundingMode.HALF_UP);
        if (saldoActual.compareTo(montoPago) < 0) {
            throw new SaldoInsuficienteException(saldoActual, montoPago);
        }

        // 4. Validar el codigo 2FA enviado por correo (un solo uso)
        if (!servicio2fa.validarYConsumirCodigo(cuenta.getNumeroCuenta(), request.getCodigo2fa())) {
            throw new Codigo2faInvalidoException(
                    "El código 2FA es incorrecto, ya fue utilizado o expiró. Solicita uno nuevo en POST /transacciones/2fa");
        }

        // 5. Construir y cifrar payload para GestoPago (sendTx)
        String upc = generarUpc();
        String payloadCifrado = prepararYCifrarPayloadGestoPago(request, montoPago, upc);

        // 6. Invocar endpoint sendTx de GestoPago (el token se agrega en GestoPagoServiceFeignConfig)
        String randomParam = System.currentTimeMillis() + "_rand";
        log.info("Enviando transacción a GestoPago para producto={} servicio={}",
                request.getIdProducto(), request.getIdServicio());

        GestoPagoTxResponse response = gestoPagoTxClient.enviarTransaccion(randomParam, payloadCifrado);

        // 7. Evaluar respuesta del proveedor
        if (response == null || response.getMensaje() == null) {
            throw new TransaccionRechazadaException("SIN_RESPUESTA", "No se recibió respuesta válida de GestoPago");
        }

        String codigoRespuesta = response.getMensaje().getCodigo();
        String textoRespuesta = response.getMensaje().getTexto();

        if (!CODIGO_EXITO_PROVEEDOR.equals(codigoRespuesta)) {
            log.warn("Transacción rechazada por GestoPago: codigo={} texto={}", codigoRespuesta, textoRespuesta);
            throw new TransaccionRechazadaException(codigoRespuesta, textoRespuesta);
        }

        // 8. Pago exitoso: descontar saldo y registrar movimiento
        BigDecimal nuevoSaldo = saldoActual.subtract(montoPago).setScale(2, RoundingMode.HALF_UP);
        String tipoMovimiento = request.getTipoDestino() == TipoDestinoTransaccion.TELEFONO
                ? "PAGO_TELEFONO" : "TRANSFERENCIA";

        Saldo movimiento = new Saldo();
        movimiento.setCuentaId(cuenta.getId());
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setMonto(montoPago);
        movimiento.setSaldoResultante(nuevoSaldo);
        movimiento.setFecha(LocalDateTime.now());
        saldoRepository.save(movimiento);

        log.info("Pago aplicado con éxito. Cuenta={} SaldoAnterior={} SaldoNuevo={} IdTx={}",
                cuenta.getNumeroCuenta(), saldoActual, nuevoSaldo, response.getIdTx());

        LocalDateTime ahora = LocalDateTime.now();
        String destino = request.getTipoDestino() == TipoDestinoTransaccion.TELEFONO
                ? request.getTelefono() : request.getReferencia();

        // 9. Enviar comprobante por correo (con logo del producto). Nunca revierte el pago.
        enviarComprobantePorCorreo(cliente, cuenta, request, destino, montoPago, nuevoSaldo,
                response, upc, ahora);

        return PagoResponse.builder()
                .idTx(response.getIdTx())
                .numAutorizacion(response.getNumAutorizacion())
                .codigoProveedor(codigoRespuesta)
                .mensajeProveedor(textoRespuesta)
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoDestino(request.getTipoDestino())
                .destino(destino)
                .monto(montoPago)
                .saldoAnterior(saldoActual.setScale(2, RoundingMode.HALF_UP))
                .saldoRestante(nuevoSaldo)
                .fecha(ahora.toLocalDate())
                .hora(ahora.toLocalTime().withNano(0))
                .build();
    }

    private Cuenta obtenerCuentaActiva(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(
                        "No se encontró la cuenta con número " + numeroCuenta));

        if (!ESTATUS_ACTIVA.equalsIgnoreCase(cuenta.getEstatus())) {
            throw new ValidacionNegocioException(
                    "La cuenta " + numeroCuenta + " no está activa y no puede realizar transacciones");
        }
        return cuenta;
    }

    private Cliente obtenerClienteActivo(Cuenta cuenta) {
        Cliente cliente = clienteRepository.findById(cuenta.getClienteId())
                .orElseThrow(() -> new ValidacionNegocioException(
                        "No se encontró el cliente asociado a la cuenta " + cuenta.getNumeroCuenta()));

        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ClienteInactivoException(cliente.getId(), "realizar transacciones");
        }
        return cliente;
    }

    private void enviarComprobantePorCorreo(Cliente cliente, Cuenta cuenta, PagoRequest request, String destino,
                                            BigDecimal monto, BigDecimal saldoRestante,
                                            GestoPagoTxResponse response, String upc, LocalDateTime fechaHora) {
        try {
            Optional<CatalogoProductosEntity> producto = catalogoProductosRepository
                    .findByIdProducto(request.getIdProducto());
            String nombreServicio = producto.map(CatalogoProductosEntity::getServicio)
                    .orElse("Servicio " + request.getIdServicio());
            String nombreProducto = producto.map(CatalogoProductosEntity::getProducto)
                    .orElse("Producto " + request.getIdProducto());

            ComprobantePagoCorreo comprobante = ComprobantePagoCorreo.builder()
                    .correoDestino(cliente.getCorreo())
                    .nombreCliente(cliente.getNombre())
                    .numeroCuenta(cuenta.getNumeroCuenta())
                    .servicio(nombreServicio)
                    .producto(nombreProducto)
                    .urlLogo(servicioNotificacionPago.construirUrlLogo(
                            request.getIdProducto(), request.getIdServicio(), nombreServicio, nombreProducto))
                    .destino(destino)
                    .monto(monto)
                    .saldoRestante(saldoRestante)
                    .idTx(response.getIdTx())
                    .numAutorizacion(response.getNumAutorizacion())
                    .upc(upc)
                    .fechaHora(fechaHora)
                    .build();

            servicioNotificacionPago.enviarComprobante(comprobante);
        } catch (Exception e) {
            log.warn("No se pudo preparar el comprobante de pago por correo: {}", e.getMessage());
        }
    }

    /** UPC de 10 caracteres alfanumericos aleatorios (A-Z, 0-9). */
    private String generarUpc() {
        StringBuilder sb = new StringBuilder(LONGITUD_UPC);
        for (int i = 0; i < LONGITUD_UPC; i++) {
            sb.append(CARACTERES_UPC.charAt(RANDOM.nextInt(CARACTERES_UPC.length())));
        }
        return sb.toString();
    }

    private String prepararYCifrarPayloadGestoPago(PagoRequest request, BigDecimal montoPago, String upc) {
        try {
            String horaLocal = LocalDateTime.now().format(FORMATO_HORA_LOCAL);

            String telefonoParaGestoPago;
            String referenciaParaGestoPago;

            if (request.getTipoDestino() == TipoDestinoTransaccion.TELEFONO) {
                telefonoParaGestoPago = request.getTelefono();
                referenciaParaGestoPago = null;
            } else {
                telefonoParaGestoPago = "1111111111";
                referenciaParaGestoPago = request.getReferencia();
            }

            GestoPagoTxPayload payload = GestoPagoTxPayload.builder()
                    .idProducto(request.getIdProducto())
                    .idServicio(request.getIdServicio())
                    .telefono(telefonoParaGestoPago)
                    .referencia(referenciaParaGestoPago)
                    .montoPago(montoPago)
                    .horaLocal(horaLocal)
                    .upc(upc)
                    .unidad(unidad)
                    .build();

            String jsonPayload = objectMapper.writeValueAsString(payload);
            log.debug("JSON plano generado para sendTx: {}", jsonPayload);

            return GestoPagoCifradoUtil.cifrar(jsonPayload, secretKey, iv);
        } catch (Exception e) {
            log.error("Fallo al construir y cifrar payload para GestoPago: {}", e.getMessage(), e);
            throw new IllegalStateException("Error al preparar la transacción cifrada", e);
        }
    }
}
