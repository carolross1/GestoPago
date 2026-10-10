package com.proyecto.servicios.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.proyecto.servicios.entity.producto.CatalogoProductosEntity;
import com.proyecto.servicios.model.gestopago.producto.CatalogoAgrupadoDTO;
import com.proyecto.servicios.model.gestopago.producto.ProductoDTO;
import com.proyecto.servicios.repositorys.producto.CatalogoProductosRepository;
import com.proyecto.servicios.service.Impl.CatalogoProductosServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoProductosServiceImplTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private CatalogoProductosRepository catalogoProductosRepository;

    private ObjectMapper objectMapper;
    private CatalogoProductosServiceImpl catalogoProductosService;

    private static final String REDIS_KEY = "catalogo:productos";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        catalogoProductosService = new CatalogoProductosServiceImpl(
                productoService, redisTemplate, catalogoProductosRepository, objectMapper);

        ReflectionTestUtils.setField(catalogoProductosService, "redisKey", REDIS_KEY);
        ReflectionTestUtils.setField(catalogoProductosService, "ttlHoras", 25L);
    }

    private ProductoDTO producto(Integer idProducto, Integer tipoFront) {
        ProductoDTO p = new ProductoDTO();
        p.setIdProducto(idProducto);
        p.setIdServicio(idProducto * 10);
        p.setTipoFront(tipoFront);
        p.setProducto("Producto " + idProducto);
        return p;
    }

    private CatalogoProductosEntity entidad(Integer idProducto, Integer tipoFront) {
        CatalogoProductosEntity e = new CatalogoProductosEntity();
        e.setIdProducto(idProducto);
        e.setIdServicio(idProducto * 10);
        e.setTipoFront(tipoFront);
        e.setProducto("Producto " + idProducto);
        e.setFechaActualizacion(LocalDateTime.now());
        return e;
    }

    @Test
    void sincronizarCatalogo_reemplazaTablaYGuardaEnRedis() {
        when(productoService.obtenerListaProductos())
                .thenReturn(List.of(producto(1, 1), producto(2, 1), producto(3, 2)));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        catalogoProductosService.sincronizarCatalogo();

        verify(catalogoProductosRepository, times(1)).deleteAllInBatch();
        verify(catalogoProductosRepository, times(1)).saveAll(any());
        verify(valueOperations).set(anyString(), anyString(), any());
    }

    @Test
    void sincronizarCatalogo_siRedisFalla_igualReemplazaTabla() {
        when(productoService.obtenerListaProductos()).thenReturn(List.of(producto(1, 1)));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Redis no disponible"))
                .when(valueOperations).set(anyString(), anyString(), any());

        catalogoProductosService.sincronizarCatalogo();

        verify(catalogoProductosRepository, times(1)).deleteAllInBatch();
        verify(catalogoProductosRepository, times(1)).saveAll(any());
    }

    @Test
    void obtenerCatalogoAgrupado_conDatosEnRedis_regresaEsosDatos() throws Exception {
        CatalogoAgrupadoDTO catalogo = new CatalogoAgrupadoDTO();
        catalogo.setFechaActualizacion(LocalDateTime.now());
        catalogo.setProductosPorTipoFront(java.util.Map.of(1, List.of(producto(1, 1))));
        String json = objectMapper.writeValueAsString(catalogo);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REDIS_KEY)).thenReturn(json);

        CatalogoAgrupadoDTO resultado = catalogoProductosService.obtenerCatalogoAgrupado();

        assertNotNull(resultado);
        assertTrue(resultado.getProductosPorTipoFront().containsKey(1));
        verify(catalogoProductosRepository, times(0)).findAll();
    }

    @Test
    void obtenerCatalogoAgrupado_siRedisFalla_caeABaseDeDatosYAgrupaPorTipoFront() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REDIS_KEY)).thenThrow(new RuntimeException("Redis no disponible"));
        when(catalogoProductosRepository.findAll())
                .thenReturn(List.of(entidad(1, 1), entidad(2, 1), entidad(3, 2)));

        CatalogoAgrupadoDTO resultado = catalogoProductosService.obtenerCatalogoAgrupado();

        assertNotNull(resultado);
        assertEquals(2, resultado.getProductosPorTipoFront().get(1).size());
        assertEquals(1, resultado.getProductosPorTipoFront().get(2).size());
    }

    @Test
    void obtenerCatalogoAgrupado_sinDatosEnNingunLado_regresaCatalogoVacioConMensaje() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(catalogoProductosRepository.findAll()).thenReturn(List.of());

        CatalogoAgrupadoDTO resultado = catalogoProductosService.obtenerCatalogoAgrupado();

        assertNotNull(resultado);
        assertTrue(resultado.getProductosPorTipoFront().isEmpty());
        assertEquals("Aun no se ha sincronizado el catalogo de productos", resultado.getMensaje());
    }
}