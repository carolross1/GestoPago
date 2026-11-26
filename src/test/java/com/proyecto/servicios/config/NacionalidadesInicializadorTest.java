package com.proyecto.servicios.config;

import com.proyecto.servicios.exception.ExternalServiceException;
import com.proyecto.servicios.repositorys.catalogo.NacionalidadRepository;
import com.proyecto.servicios.service.catalogo.NacionalidadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NacionalidadesInicializadorTest {

    @Mock
    private NacionalidadService nacionalidadService;

    @Mock
    private NacionalidadRepository nacionalidadRepository;

    @InjectMocks
    private NacionalidadesInicializador inicializador;

    @Test
    @DisplayName("run sincroniza cuando el catalogo esta vacio")
    void runCatalogoVacio() {
        when(nacionalidadRepository.count()).thenReturn(0L);

        inicializador.run(mock(ApplicationArguments.class));

        verify(nacionalidadService).sincronizar();
    }

    @Test
    @DisplayName("run no sincroniza cuando el catalogo ya contiene registros")
    void runCatalogoLleno() {
        when(nacionalidadRepository.count()).thenReturn(150L);

        inicializador.run(mock(ApplicationArguments.class));

        verify(nacionalidadService, never()).sincronizar();
    }

    @Test
    @DisplayName("sincronizacionMensual captura excepciones sin propagarlas")
    void sincronizacionMensualManejaError() {
        doThrow(new ExternalServiceException("RestCountries", "API caída"))
                .when(nacionalidadService).sincronizar();

        assertDoesNotThrow(() -> inicializador.sincronizacionMensual());
    }
}
