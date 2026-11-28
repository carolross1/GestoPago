package com.proyecto.servicios.model.transaccion;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@Schema(description = "Resultado de la transacción de pago / transferencia procesada por GestoPago")
public class PagoResponse {

    @Schema(description = "Indica si la transacción fue aprobada (true) o rechazada (false)", example = "true")
    @Builder.Default
    private boolean aprobada = true;

    @Schema(description = "Estado del ticket: EXITOSA o RECHAZADA", example = "EXITOSA")
    @Builder.Default
    private String estado = "EXITOSA";

    @Schema(description = "ID único de transacción retornado por GestoPago", example = "291437188")
    private String idTx;

    @Schema(description = "Número de autorización retornado por GestoPago", example = "100024988")
    private String numAutorizacion;

    @Schema(description = "Código de respuesta del proveedor (01 = éxito, -1 = rechazada)", example = "01")
    private String codigoProveedor;

    @Schema(description = "Mensaje descriptivo devuelto por GestoPago", example = "Operacion realizada con exito")
    private String mensajeProveedor;

    @Schema(description = "Folio UPC numérico de 10 dígitos generado para la operación", example = "4829103746")
    private String upc;

    @Schema(description = "Hora local en formato yyyyMMddHHmmss enviada a GestoPago", example = "20261127181530")
    private String horaLocal;

    @Schema(description = "ID del servicio en GestoPago", example = "133")
    private Integer idServicio;

    @Schema(description = "ID del producto en GestoPago", example = "1333")
    private Integer idProducto;

    @Schema(description = "ID / código de dispositivo TPV registrado en GestoPago", example = "GPS83-TPV-17")
    private String idDispositivo;

    @Schema(description = "Leyenda del producto para aclaraciones o reclamaciones")
    private String leyenda;

    @Schema(description = "Número de cuenta origen del cliente", example = "1452864812")
    private String numeroCuenta;

    @Schema(description = "Tipo de destino de la transacción", example = "TELEFONO")
    private TipoDestinoTransaccion tipoDestino;

    @Schema(description = "Destino al que se aplicó el pago (teléfono o referencia)", example = "5512345678")
    private String destino;

    @Schema(description = "Monto pagado o intentado", example = "50.00")
    private BigDecimal monto;

    @Schema(description = "Saldo de la cuenta antes de la transacción", example = "200.00")
    private BigDecimal saldoAnterior;

    @Schema(description = "Saldo restante en la cuenta tras la operación", example = "150.00")
    private BigDecimal saldoRestante;

    @JsonFormat(pattern = "yyyy/MM/dd")
    @Schema(description = "Fecha de la transacción, yyyy/MM/dd", example = "2026/10/23")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(description = "Hora de la transacción, HH:mm:ss", example = "18:25:30")
    private LocalTime hora;
}
