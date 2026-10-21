package com.proyecto.servicios.model.cliente;

import com.proyecto.servicios.model.comun.SolicitudEstricta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

/**
 * Criterios combinables para el unico endpoint de consulta de cuentas
 * (POST /cuentas/buscar). Todos opcionales; se unen con AND.
 */
@Getter
@Setter
@Schema(description = "Filtros combinables; envia solo los que necesites")
public class CuentaFiltroRequest extends SolicitudEstricta {

    @Pattern(regexp = "^\\d{10}$", message = "El numero de cuenta debe tener exactamente 10 digitos")
    @Schema(example = "1452864812")
    private String numeroCuenta;

    @Positive(message = "El id del cliente debe ser mayor a cero")
    @Schema(example = "3")
    private Long clienteId;

    @Pattern(regexp = "^(ACTIVA|INACTIVA)$", message = "Estatus invalido, valores permitidos: ACTIVA, INACTIVA")
    @Schema(example = "ACTIVA")
    private String estatus;

    @Min(value = 0, message = "La pagina inicia en 0")
    @Schema(example = "0", defaultValue = "0")
    private Integer pagina = 0;

    @Min(value = 1, message = "El tamano de pagina debe ser al menos 1")
    @Max(value = 100, message = "El tamano de pagina debe ser maximo 100")
    @Schema(example = "10", defaultValue = "10")
    private Integer tamano = 10;

    public void setEstatus(String estatus) {
        this.estatus = estatus == null ? null : estatus.trim().toUpperCase(Locale.ROOT);
    }

    public void setPagina(Integer pagina) {
        this.pagina = pagina == null ? 0 : pagina;
    }

    public void setTamano(Integer tamano) {
        this.tamano = tamano == null ? 10 : tamano;
    }
}
