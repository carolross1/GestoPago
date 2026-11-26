package com.proyecto.servicios.model.transaccion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Solicitud para consultar y validar la referencia de un servicio antes del pago")
public class ValidarReferenciaRequest {

    @NotNull(message = "El idServicio es obligatorio")
    @Positive(message = "El idServicio debe ser mayor a cero")
    @Schema(description = "ID del servicio en el catálogo de GestoPago", example = "133")
    private Integer idServicio;

    @NotNull(message = "El idProducto es obligatorio")
    @Positive(message = "El idProducto debe ser mayor a cero")
    @Schema(description = "ID del producto en el catálogo de GestoPago", example = "582")
    private Integer idProducto;

    @NotBlank(message = "La referencia o teléfono es obligatorio")
    @Schema(description = "Número telefónico o referencia del recibo", example = "5512345678")
    private String referencia;

    @NotNull(message = "El tipo de destino (TELEFONO o TRANSFERENCIA) es obligatorio")
    @Schema(description = "Bandera para diferenciar validación de teléfono o referencia", example = "TELEFONO")
    private TipoDestinoTransaccion tipoDestino;
}
