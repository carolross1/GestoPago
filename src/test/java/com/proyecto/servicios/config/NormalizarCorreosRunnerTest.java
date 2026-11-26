package com.proyecto.servicios.config;

import com.proyecto.servicios.entity.cliente.Cliente;
import com.proyecto.servicios.repositorys.cliente.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NormalizarCorreosRunnerTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private NormalizarCorreosRunner runner;

    @Test
    @DisplayName("run convierte correos con mayusculas a minusculas y guarda los cambios")
    void runNormalizaCorreos() {
        Cliente c1 = new Cliente();
        c1.setId(1L);
        ReflectionTestUtils.setField(c1, "correo", "Juan.Perez@CORREO.com");

        Cliente c2 = new Cliente();
        c2.setId(2L);
        c2.setCorreo("maria@correo.com");

        when(clienteRepository.findAll()).thenReturn(List.of(c1, c2));

        runner.run(mock(ApplicationArguments.class));

        assertEquals("juan.perez@correo.com", c1.getCorreo());
        verify(clienteRepository).save(c1);
        verify(clienteRepository, never()).save(c2);
    }
}
