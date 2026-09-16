package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.producto.ProductoDTO;

import java.util.List;

public interface ProductoService {
    List<ProductoDTO> obtenerListaProductos();
}
