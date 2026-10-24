package com.proyecto.servicios.service.transaccion.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.client.GestoPagoTxClient;
import com.proyecto.servicios.entity.cliente.Cliente;
import com.proyecto.servicios.entity.cliente.Cuenta;
import com.proyecto.servicios.entity.cliente.Saldo;
import com.proyecto.servicios.exception.cliente.ClienteInactivoException;
import com.proyecto.servicios.exception.cliente.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.cliente.ValidacionNegocioException;
import com.proyecto.servicios.exception.transaccion.Codigo2faInvalidoException;
import com.proyecto.servicios.exception.transaccion.SaldoInsuficienteException;
import com.proyecto.servicios.exception.transaccion.TransaccionRechazadaException;
import com.proyecto.servicios.model.gestopago.tx.GestoPagoTxPayload;
import com.proyecto.servicios.model.gestopago.tx.GestoPagoTxResponse;
import com.proyecto.servicios.model.transaccion.PagoRequest;
import com.proyecto.servicios.model.transaccion.PagoResponse;
import com.proyecto.servicios.model.transaccion.TipoDestinoTransaccion;
import com.proyecto.servicios.repositorys.cliente.ClienteRepository;
import com.proyecto.servicios.repositorys.cliente.CuentaRepository;
import com.proyecto.servicios.repositorys.cliente.SaldoRepository;
import com.proyecto.servicios.security.GestoPagoCifradoUtil;
import com.proyecto.servicios.service.transaccion.TransaccionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class TransaccionServiceImpl implements TransaccionService {

    private static final String ESTATUS_ACTIVA = "ACTIVA";
    private static final String CODIGO_EXITO_PROVEEDOR = "01";
    private static final String CODIGO_2FA_FALLO_SIMULADO = "000000";
    private static final DateTimeFormatter FORMATO_HORA_LOCAL = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final SaldoRepository saldoRepository;
    private final GestoPagoTxClient gestoPagoTxClient;
    private final ObjectMapper objectMapper;

    @Value("${gestopago.tx.secret-key}")
    private String secretKey;

    @Value("${gestopago.tx.iv}")
    private String iv;

    @Value("${gestopago.tx.unidad:083}")
    private String unidad;

    public TransaccionServiceImpl(CuentaRepository cuentaRepository,
                                  ClienteRepository clienteRepository,
                                  SaldoRepository saldoRepository,
                                  GestoPagoTxClient gestoPagoTxClient,
                                  ObjectMapper objectMapper) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.saldoRepository = saldoRepository;
        this.gestoPagoTxClient = gestoPagoTxClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public PagoResponse procesarPago(PagoRequest request) {
        log.info("Iniciando procesamiento de pago para cuenta={} monto={} tipo={}",
                request.getNumeroCuenta(), request.getMonto(), request.getTipoDestino());

        // 1. Validar existencia y estatus de la cuenta
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(request.getNumeroCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException(
                        "No se encontró la cuenta con número " + request.getNumeroCuenta()));

        if (!ESTATUS_ACTIVA.equalsIgnoreCase(cuenta.getEstatus())) {
            throw new ValidacionNegocioException(
                    "La cuenta " + request.getNumeroCuenta() + " no está activa y no puede realizar transacciones");
        }

        // 2. Validar que el cliente dueño de la cuenta exista y esté activo
        Cliente cliente = clienteRepository.findById(cuenta.getClienteId())
                .orElseThrow(() -> new ValidacionNegocioException(
                        "No se encontró el cliente asociado a la cuenta " + request.getNumeroCuenta()));

        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ClienteInactivoException(cliente.getId(), "realizar transacciones");
        }

        // 3. Validar código 2FA
        validarCodigo2FA(request.getCodigo2fa());

        // 4. Validar saldo suficiente
        BigDecimal saldoActual = saldoRepository.findFirstByCuentaIdOrderByFechaDesc(cuenta.getId())
                .map(Saldo::getSaldoResultante)
                .orElse(BigDecimal.ZERO);

        BigDecimal montoPago = request.getMonto().setScale(2, RoundingMode.HALF_UP);
        if (saldoActual.compareTo(montoPago) < 0) {
            throw new SaldoInsuficienteException(saldoActual, montoPago);
        }

        // 5. Construir y cifrar payload para GestoPago (sendTx)
        String payloadCifrado = prepararYCifrarPayloadGestoPago(request, montoPago);

        // 6. Invocar endpoint sendTx de GestoPago
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

    private void validarCodigo2FA(String codigo2fa) {
        if (codigo2fa == null || !codigo2fa.matches("^\\d{6}$")) {
            throw new Codigo2faInvalidoException("El código 2FA debe contener exactamente 6 dígitos numéricos");
        }
        if (CODIGO_2FA_FALLO_SIMULADO.equals(codigo2fa)) {
            throw new Codigo2faInvalidoException("El código 2FA proporcionado es incorrecto o ha expirado");
        }
    }

    private String prepararYCifrarPayloadGestoPago(PagoRequest request, BigDecimal montoPago) {
        try {
            String horaLocal = LocalDateTime.now().format(FORMATO_HORA_LOCAL);
            String upc = "UPC_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

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
