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

    @Schema(description = "ID único de transacción retornado por GestoPago", example = "291437188")
    private String idTx;

    @Schema(description = "Número de autorización retornado por GestoPago", example = "100024988")
    private String numAutorizacion;

    @Schema(description = "Código de respuesta del proveedor (01 = éxito)", example = "01")
    private String codigoProveedor;

    @Schema(description = "Mensaje descriptivo devuelto por GestoPago", example = "Operacion realizada con exito")
    private String mensajeProveedor;

    @Schema(description = "Número de cuenta origen del cliente", example = "1452864812")
    private String numeroCuenta;

    @Schema(description = "Tipo de destino de la transacción", example = "TELEFONO")
    private TipoDestinoTransaccion tipoDestino;

    @Schema(description = "Destino al que se aplicó el pago (teléfono o referencia)", example = "5512345678")
    private String destino;

    @Schema(description = "Monto pagado", example = "50.00")
    private BigDecimal monto;

    @Schema(description = "Saldo de la cuenta antes de la transacción", example = "200.00")
    private BigDecimal saldoAnterior;

    @Schema(description = "Saldo restante en la cuenta tras aplicar el cargo", example = "150.00")
    private BigDecimal saldoRestante;

    @JsonFormat(pattern = "yyyy/MM/dd")
    @Schema(description = "Fecha de la transacción, yyyy/MM/dd", example = "2026/10/23")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(description = "Hora de la transacción, HH:mm:ss", example = "18:25:30")
    private LocalTime hora;
}
