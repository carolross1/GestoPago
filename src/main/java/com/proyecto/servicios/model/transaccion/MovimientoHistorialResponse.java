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
@Schema(description = "Detalle de una transacción en el historial de pagos")
public class MovimientoHistorialResponse {

    @Schema(description = "Identificador único interno del movimiento", example = "1")
    private Long id;

    @Schema(description = "Número de cuenta origen", example = "1452864812")
    private String numeroCuenta;

    @Schema(description = "Tipo de movimiento contable", example = "PAGO_TELEFONO")
    private String tipoMovimiento;

    @Schema(description = "Concepto o descripción de la transacción", example = "Pago Telcel Recarga")
    private String concepto;

    @Schema(description = "Nombre del servicio", example = "Telcel")
    private String servicio;

    @Schema(description = "Nombre del producto", example = "Paquete Amigo Sin Límite")
    private String producto;

    @Schema(description = "Referencia o teléfono utilizado", example = "5512345678")
    private String referencia;

    @Schema(description = "Folio de autorización o UPC de la transacción", example = "UPC-100024988")
    private String folio;

    @Schema(description = "Estado de la operación (EXITOSA, PENDIENTE, RECHAZADA)", example = "EXITOSA")
    private String estado;

    @Schema(description = "Importe pagado", example = "50.00")
    private BigDecimal monto;

    @Schema(description = "Saldo resultante en cuenta", example = "150.00")
    private BigDecimal saldoResultante;

    @Schema(description = "URL del logotipo del proveedor", example = "https://ui-avatars.com/api/?name=Telcel")
    private String urlLogo;

    @JsonFormat(pattern = "yyyy/MM/dd")
    @Schema(description = "Fecha de la operación (yyyy/MM/dd)", example = "2026/11/13")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(description = "Hora de la operación (HH:mm:ss)", example = "18:25:30")
    private LocalTime hora;
}
