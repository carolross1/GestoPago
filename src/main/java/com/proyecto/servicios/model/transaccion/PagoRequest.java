package com.proyecto.servicios.model.transaccion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.proyecto.servicios.model.comun.SolicitudEstricta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Solicitud de pago o transferencia vía GestoPago con 2FA y validación de saldo")
public class PagoRequest extends SolicitudEstricta {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "El número de cuenta debe tener exactamente 10 dígitos")
    @Schema(description = "Número de cuenta de 10 dígitos del cliente emisor", example = "1452864812")
    private String numeroCuenta;

    @NotNull(message = "El idServicio es obligatorio")
    @Positive(message = "El idServicio debe ser mayor a cero")
    @Schema(description = "ID del servicio en el catálogo de GestoPago", example = "133")
    private Integer idServicio;

    @NotNull(message = "El idProducto es obligatorio")
    @Positive(message = "El idProducto debe ser mayor a cero")
    @Schema(description = "ID del producto en el catálogo de GestoPago", example = "582")
    private Integer idProducto;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Digits(integer = 12, fraction = 2, message = "El monto debe tener máximo 2 decimales (ej. 50.00)")
    @Schema(description = "Monto de la transacción con máximo 2 decimales", example = "50.00")
    private BigDecimal monto;

    @NotNull(message = "La bandera de tipo de transacción (tipoDestino: TELEFONO o TRANSFERENCIA) es obligatoria")
    @Schema(description = "Bandera para diferenciar si la transacción es por teléfono o por transferencia", example = "TELEFONO")
    private TipoDestinoTransaccion tipoDestino;

    @Pattern(regexp = "^\\d{10}$", message = "El teléfono debe tener exactamente 10 dígitos")
    @Schema(description = "Teléfono de 10 dígitos (obligatorio si tipoDestino es TELEFONO)", example = "5512345678")
    private String telefono;

    @Size(max = 100, message = "La referencia debe tener máximo 100 caracteres")
    @Schema(description = "Referencia del recibo o servicio (obligatoria si tipoDestino es TRANSFERENCIA)", example = "REF-2026-987654")
    private String referencia;

    @NotBlank(message = "El código 2FA es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El código 2FA debe ser de exactamente 6 dígitos numéricos")
    @Schema(description = "Código de doble factor de autenticación (6 dígitos)", example = "123456")
    private String codigo2fa;

    @JsonIgnore
    @AssertTrue(message = "Para transacciones por TELEFONO, el campo 'telefono' es obligatorio y debe tener 10 dígitos")
    public boolean isTelefonoValidoSegunTipo() {
        if (tipoDestino == TipoDestinoTransaccion.TELEFONO) {
            return telefono != null && telefono.matches("^\\d{10}$");
        }
        return true;
    }

    @JsonIgnore
    @AssertTrue(message = "Para transacciones por TRANSFERENCIA, el campo 'referencia' es obligatorio")
    public boolean isReferenciaValidaSegunTipo() {
        if (tipoDestino == TipoDestinoTransaccion.TRANSFERENCIA) {
            return referencia != null && !referencia.trim().isEmpty();
        }
        return true;
    }
}
