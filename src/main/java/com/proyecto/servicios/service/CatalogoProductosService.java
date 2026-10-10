package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.producto.CatalogoAgrupadoDTO;

public interface CatalogoProductosService {

    void sincronizarCatalogo();

    CatalogoAgrupadoDTO obtenerCatalogoAgrupado();
}
