package com.proyecto.servicios.service.transaccion;

import com.proyecto.servicios.model.transaccion.PagoRequest;
import com.proyecto.servicios.model.transaccion.PagoResponse;
import com.proyecto.servicios.model.transaccion.Solicitar2faRequest;
import com.proyecto.servicios.model.transaccion.Solicitar2faResponse;

public interface TransaccionService {

    Solicitar2faResponse solicitarCodigo2fa(Solicitar2faRequest request);

    PagoResponse procesarPago(PagoRequest request);
}
