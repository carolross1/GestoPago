package com.proyecto.servicios.service.catalogo;

import com.proyecto.servicios.entity.catalogo.Nacionalidad;
import com.proyecto.servicios.model.catalogo.NacionalidadResponse;
import com.proyecto.servicios.model.catalogo.SincronizacionResponse;

import java.util.List;
import java.util.Optional;

public interface NacionalidadService {

    /** Descarga los paises de restcountries.com y actualiza la tabla del catalogo. */
    SincronizacionResponse sincronizar();

    /** Catalogo activo ordenado por nombre (para llenar un combo en el front). */
    List<NacionalidadResponse> listar();

    /** Regresa la nacionalidad si el codigo existe y esta activo; si no, lanza ValidacionNegocioException. */
    Nacionalidad validar(String codigo);

    Optional<Nacionalidad> buscar(String codigo);
}
