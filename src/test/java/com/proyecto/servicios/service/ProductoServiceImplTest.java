package com.proyecto.servicios.service;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.exception.ExternalServiceAuthException;
import com.proyecto.servicios.exception.ExternalServiceException;
import com.proyecto.servicios.exception.ExternalServiceTimeoutException;
import com.proyecto.servicios.model.gestopago.producto.ProductoDTO;
import com.proyecto.servicios.model.gestopago.producto.RespuestaProductosDTO;
import com.proyecto.servicios.service.Impl.ProductoServiceImpl;
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private GestoPagoProductClient gestoPagoProductClient;

    private ProductoServiceImpl productoService;

    @BeforeEach
    void setUp() {
        productoService = new ProductoServiceImpl(gestoPagoProductClient);
    }

    @Test
    void obtenerListaProductos_respuestaExitosa_regresaLista() {
        ProductoDTO producto = new ProductoDTO();
        producto.setIdProducto(200);
        producto.setProducto("Amazon $100");

        RespuestaProductosDTO respuesta = new RespuestaProductosDTO();
        respuesta.setProductos(List.of(producto));

        when(gestoPagoProductClient.obtenerListaProductos()).thenReturn(respuesta);

        List<ProductoDTO> resultado = productoService.obtenerListaProductos();

        assertEquals(1, resultado.size());
        assertEquals(200, resultado.get(0).getIdProducto());
    }

    @Test
    void obtenerListaProductos_respuestaNula_regresaListaVacia() {
        when(gestoPagoProductClient.obtenerListaProductos()).thenReturn(null);

        List<ProductoDTO> resultado = productoService.obtenerListaProductos();

        assertEquals(0, resultado.size());
    }

    @Test
    void obtenerListaProductos_sinTokenActivo_lanzaExternalServiceAuthException() {
        // Simula lo que lanzaria el interceptor si no hay token en BD
        when(gestoPagoProductClient.obtenerListaProductos())
                .thenThrow(new ExternalServiceAuthException("GestoPago", "No hay token activo"));

        assertThrows(ExternalServiceAuthException.class,
                () -> productoService.obtenerListaProductos());
    }

    @Test
    void obtenerListaProductos_errorAutenticacion_lanzaExternalServiceAuthException() {
        FeignException.Unauthorized ex = mock(FeignException.Unauthorized.class);
        when(ex.status()).thenReturn(401);
        when(gestoPagoProductClient.obtenerListaProductos()).thenThrow(ex);

        assertThrows(ExternalServiceAuthException.class,
                () -> productoService.obtenerListaProductos());
    }

    @Test
    void obtenerListaProductos_timeout_lanzaExternalServiceTimeoutException() {
        RetryableException ex = mock(RetryableException.class);
        when(gestoPagoProductClient.obtenerListaProductos()).thenThrow(ex);

        assertThrows(ExternalServiceTimeoutException.class,
                () -> productoService.obtenerListaProductos());
    }

    @Test
    void obtenerListaProductos_errorServidor_lanzaExternalServiceException() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(500);
        when(gestoPagoProductClient.obtenerListaProductos()).thenThrow(ex);

        assertThrows(ExternalServiceException.class,
                () -> productoService.obtenerListaProductos());
    }
}
