package com.proyecto.servicios.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.entity.producto.CatalogoProductosEntity;
import com.proyecto.servicios.model.gestopago.producto.CatalogoAgrupadoDTO;
import com.proyecto.servicios.model.gestopago.producto.ProductoDTO;
import com.proyecto.servicios.repositorys.producto.CatalogoProductosRepository;
import com.proyecto.servicios.service.CatalogoProductosService;
import com.proyecto.servicios.service.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CatalogoProductosServiceImpl implements CatalogoProductosService {

    private static final String SERVICIO = "CatalogoProductos";

    private final ProductoService productoService;
    private final StringRedisTemplate redisTemplate;
    private final CatalogoProductosRepository catalogoProductosRepository;
    private final ObjectMapper objectMapper;

    @Value("${productos.cache.redis-key}")
    private String redisKey;

    @Value("${productos.cache.ttl-horas}")
    private long ttlHoras;

    public CatalogoProductosServiceImpl(ProductoService productoService,
                                        StringRedisTemplate redisTemplate,
                                        CatalogoProductosRepository catalogoProductosRepository,
                                        ObjectMapper objectMapper) {
        this.productoService = productoService;
        this.redisTemplate = redisTemplate;
        this.catalogoProductosRepository = catalogoProductosRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Scheduled(cron = "${productos.sync.cron}")
    @Transactional
    public void sincronizarCatalogo() {
        log.info("Iniciando sincronizacion diaria de {}", SERVICIO);
        try {
            List<ProductoDTO> productos = productoService.obtenerListaProductos();
            LocalDateTime ahora = LocalDateTime.now();

            List<CatalogoProductosEntity> entidades = productos.stream()
                    .map(dto -> toEntity(dto, ahora))
                    .collect(Collectors.toList());

            catalogoProductosRepository.deleteAllInBatch();
            catalogoProductosRepository.saveAll(entidades);

            Map<Integer, List<ProductoDTO>> agrupados = productos.stream()
                    .collect(Collectors.groupingBy(ProductoDTO::getTipoFront));

            CatalogoAgrupadoDTO catalogo = new CatalogoAgrupadoDTO();
            catalogo.setFechaActualizacion(ahora);
            catalogo.setProductosPorTipoFront(agrupados);

            guardarEnRedis(objectMapper.writeValueAsString(catalogo));

            log.info("Sincronizacion de {} finalizada correctamente. Productos: {}, grupos de tipoFront: {}",
                    SERVICIO, productos.size(), agrupados.size());

        } catch (Exception e) {
            log.error("Error al sincronizar {}: {}", SERVICIO, e.getMessage());
        }
    }

    private void guardarEnRedis(String json) {
        try {
            redisTemplate.opsForValue().set(redisKey, json, Duration.ofHours(ttlHoras));
            log.info("Catalogo guardado en Redis correctamente");
        } catch (Exception e) {
            log.warn("No se pudo guardar el catalogo en Redis, queda disponible solo en base de datos: {}",
                    e.getMessage());
        }
    }

    @Override
    public CatalogoAgrupadoDTO obtenerCatalogoAgrupado() {
        CatalogoAgrupadoDTO desdeRedis = leerDeRedis();
        if (desdeRedis != null) {
            return desdeRedis;
        }

        log.info("Catalogo no disponible en Redis, consultando respaldo en base de datos");
        return leerDeBaseDeDatos();
    }

    private CatalogoAgrupadoDTO leerDeRedis() {
        try {
            String json = redisTemplate.opsForValue().get(redisKey);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, CatalogoAgrupadoDTO.class);
        } catch (Exception e) {
            log.warn("Redis no disponible, se usara la base de datos como respaldo: {}", e.getMessage());
            return null;
        }
    }

    private CatalogoAgrupadoDTO leerDeBaseDeDatos() {
        List<CatalogoProductosEntity> entidades = catalogoProductosRepository.findAll();

        if (entidades.isEmpty()) {
            return catalogoVacio("Aun no se ha sincronizado el catalogo de productos");
        }

        Map<Integer, List<ProductoDTO>> agrupados = entidades.stream()
                .map(this::toDTO)
                .collect(Collectors.groupingBy(ProductoDTO::getTipoFront));

        LocalDateTime fechaActualizacion = entidades.get(0).getFechaActualizacion();

        CatalogoAgrupadoDTO catalogo = new CatalogoAgrupadoDTO();
        catalogo.setFechaActualizacion(fechaActualizacion);
        catalogo.setProductosPorTipoFront(agrupados);
        return catalogo;
    }

    private CatalogoAgrupadoDTO catalogoVacio(String mensaje) {
        CatalogoAgrupadoDTO catalogo = new CatalogoAgrupadoDTO();
        catalogo.setProductosPorTipoFront(Collections.emptyMap());
        catalogo.setMensaje(mensaje);
        return catalogo;
    }

    private CatalogoProductosEntity toEntity(ProductoDTO dto, LocalDateTime fecha) {
        CatalogoProductosEntity entidad = new CatalogoProductosEntity();
        entidad.setIdProducto(dto.getIdProducto());
        entidad.setIdServicio(dto.getIdServicio());
        entidad.setIdCatTipoServicio(dto.getIdCatTipoServicio());
        entidad.setTipoFront(dto.getTipoFront());
        entidad.setServicio(dto.getServicio());
        entidad.setProducto(dto.getProducto());
        entidad.setPrecio(dto.getPrecio());
        entidad.setTipoReferencia(dto.getTipoReferencia());
        entidad.setLegend(dto.getLegend());
        entidad.setFechaActualizacion(fecha);
        return entidad;
    }

    private ProductoDTO toDTO(CatalogoProductosEntity entidad) {
        ProductoDTO dto = new ProductoDTO();
        dto.setIdProducto(entidad.getIdProducto());
        dto.setIdServicio(entidad.getIdServicio());
        dto.setIdCatTipoServicio(entidad.getIdCatTipoServicio());
        dto.setTipoFront(entidad.getTipoFront());
        dto.setServicio(entidad.getServicio());
        dto.setProducto(entidad.getProducto());
        dto.setPrecio(entidad.getPrecio());
        dto.setTipoReferencia(entidad.getTipoReferencia());
        dto.setLegend(entidad.getLegend());
        return dto;
    }
}