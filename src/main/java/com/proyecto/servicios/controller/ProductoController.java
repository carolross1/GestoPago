package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.gestopago.producto.CatalogoAgrupadoDTO;
import com.proyecto.servicios.service.CatalogoProductosService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /productos NUNCA llama directo a GestoPago: lee del catalogo ya
 * sincronizado (Redis, o base de datos si Redis no esta disponible).
 * La sincronizacion real corre una vez al dia via CatalogoProductosService.
 */
@RestController
public class ProductoController {

    private final CatalogoProductosService catalogoProductosService;

    public ProductoController(CatalogoProductosService catalogoProductosService) {
        this.catalogoProductosService = catalogoProductosService;
    }

    @GetMapping(value = "/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CatalogoAgrupadoDTO> listarProductos() {
        return ResponseEntity.ok(catalogoProductosService.obtenerCatalogoAgrupado());
    }
}
