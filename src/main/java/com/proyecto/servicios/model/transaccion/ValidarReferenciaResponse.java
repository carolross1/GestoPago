package com.proyecto.servicios.model.transaccion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Resultado de la consulta y validación de una referencia de servicio")
public class ValidarReferenciaResponse {

    @Schema(description = "Indica si la referencia cumple las reglas y es válida para cobro", example = "true")
    private boolean valida;

    @Schema(description = "Mensaje descriptivo del resultado de validación", example = "Referencia verificada correctamente")
    private String mensaje;

    @Schema(description = "Nombre del servicio asociado", example = "CFE")
    private String servicio;

    @Schema(description = "Nombre del producto", example = "CFE Suministrador de Servicios Básicos")
    private String producto;

    @Schema(description = "Referencia consultada", example = "5512345678")
    private String referencia;

    @Schema(description = "Tipo de referencia esperada según catálogo", example = "NUM")
    private String tipoReferencia;

    @Schema(description = "Monto de adeudo sugerido o fijo si aplica", example = "150.00")
    private BigDecimal adeudoSugerido;

    @Schema(description = "Titular o beneficiario identificado", example = "Servicio Residencial Activo")
    private String titularSugerido;

    @Schema(description = "URL del logotipo oficial del proveedor", example = "https://ui-avatars.com/api/?name=CFE")
    private String urlLogo;
}
