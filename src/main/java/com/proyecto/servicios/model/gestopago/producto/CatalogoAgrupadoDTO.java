package com.proyecto.servicios.model.gestopago.producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Respuesta expuesta por GET /productos: catalogo agrupado por tipoFront,
 * leido desde Redis (o desde la base de datos si Redis no esta disponible).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoAgrupadoDTO {

    private LocalDateTime fechaActualizacion;

    private Map<Integer, List<ProductoDTO>> productosPorTipoFront;

    /**
     * Solo se llena cuando aun no existe un catalogo sincronizado
     * (por ejemplo, el cron diario no se ha ejecutado todavia).
     */
    private String mensaje;
}
