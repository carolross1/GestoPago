package com.proyecto.servicios.service.transaccion;

import com.proyecto.servicios.model.transaccion.PagoRequest;
import com.proyecto.servicios.model.transaccion.PagoResponse;

public interface TransaccionService {

    PagoResponse procesarPago(PagoRequest request);
}
