package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.exception.ExternalServiceAuthException;
import com.proyecto.servicios.exception.ExternalServiceException;
import com.proyecto.servicios.exception.ExternalServiceTimeoutException;
import com.proyecto.servicios.model.gestopago.producto.ProductoDTO;
import com.proyecto.servicios.model.gestopago.producto.RespuestaProductosDTO;
import com.proyecto.servicios.service.ProductoService;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ProductoServiceImpl implements ProductoService {

    private static final String SERVICIO = "GestoPago-getProductList";

    private final GestoPagoProductClient gestoPagoProductClient;

    public ProductoServiceImpl(GestoPagoProductClient gestoPagoProductClient) {
        this.gestoPagoProductClient = gestoPagoProductClient;
    }

    @Override
    public List<ProductoDTO> obtenerListaProductos() {
        log.info("Iniciando invocacion a {}", SERVICIO);
        try {
            RespuestaProductosDTO respuesta = gestoPagoProductClient.obtenerListaProductos();
            List<ProductoDTO> productos = (respuesta != null && respuesta.getProductos() != null)
                    ? respuesta.getProductos()
                    : Collections.emptyList();

            log.info("Invocacion a {} finalizada correctamente. Productos obtenidos: {}",
                    SERVICIO, productos.size());
            return productos;

        } catch (ExternalServiceException e) {
            // Ya viene tipificada (p.ej. desde el interceptor por falta de token)
            log.error("Error de integracion al consumir {}: {}", SERVICIO, e.getMessage());
            throw e;

        } catch (FeignException.Unauthorized | FeignException.Forbidden e) {
            log.error("Error de autenticacion al consumir {} (status={})", SERVICIO, e.status());
            throw new ExternalServiceAuthException(SERVICIO, "Token invalido, expirado o no autorizado");

        } catch (RetryableException e) {
            log.error("Timeout o error de comunicacion al consumir {}: {}", SERVICIO, e.getMessage());
            throw new ExternalServiceTimeoutException(SERVICIO, "Tiempo de espera agotado al contactar el servicio");

        } catch (FeignException e) {
            log.error("Respuesta no exitosa de {} (status={})", SERVICIO, e.status());
            throw new ExternalServiceException(SERVICIO, "El servicio externo respondio con error (status " + e.status() + ")");

        } catch (Exception e) {
            log.error("Error inesperado al consumir {}: {}", SERVICIO, e.getMessage());
            throw new ExternalServiceException(SERVICIO, "Error inesperado al consultar el servicio externo");
        }
    }
}
