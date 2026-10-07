package com.proyecto.servicios.config;

import com.proyecto.servicios.exception.ExternalServiceException;
import com.proyecto.servicios.repositorys.catalogo.NacionalidadRepository;
import com.proyecto.servicios.service.catalogo.NacionalidadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Mantiene lleno el catalogo de nacionalidades:
 *  - Al arrancar la app, si la tabla esta vacia, la llena desde la API.
 *  - Una vez al mes (cron configurable) la vuelve a sincronizar.
 * Si la API falla, solo se registra en el log: la app arranca igual y se
 * sigue usando lo que ya este guardado en la base de datos.
 */
@Slf4j
@Component
public class NacionalidadesInicializador implements ApplicationRunner {

    private final NacionalidadService nacionalidadService;
    private final NacionalidadRepository nacionalidadRepository;

    public NacionalidadesInicializador(NacionalidadService nacionalidadService,
                                       NacionalidadRepository nacionalidadRepository) {
        this.nacionalidadService = nacionalidadService;
        this.nacionalidadRepository = nacionalidadRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (nacionalidadRepository.count() == 0) {
            log.info("Catalogo de nacionalidades vacio, se carga desde restcountries.com");
            sincronizarSinDetener();
        }
    }

    @Scheduled(cron = "${restcountries.sync-cron:0 0 3 1 * *}")
    public void sincronizacionMensual() {
        sincronizarSinDetener();
    }

    private void sincronizarSinDetener() {
        try {
            nacionalidadService.sincronizar();
        } catch (ExternalServiceException e) {
            log.warn("No se pudo sincronizar el catalogo de nacionalidades: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Error inesperado al sincronizar nacionalidades: {}", e.getMessage());
        }
    }
}
