package com.proyecto.servicios.client;

import com.proyecto.servicios.config.GestoPagoServiceFeignConfig;
import com.proyecto.servicios.model.gestopago.producto.RespuestaProductosDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Consume GET /sistema/service/getProductList.do
 * Advertencia del proveedor: maximo 3 llamadas por dia, no usarlo como
 * fuente directa para el frontend, solo para sincronizar catalogo interno.
 */
@FeignClient(
        name = "gestoPagoProductClient",
        url = "${gestopago.auth.url}",
        configuration = GestoPagoServiceFeignConfig.class
)
public interface GestoPagoProductClient {

    @GetMapping(value = "/sistema/service/getProductList.do", produces = MediaType.APPLICATION_XML_VALUE)
    RespuestaProductosDTO obtenerListaProductos();
}
