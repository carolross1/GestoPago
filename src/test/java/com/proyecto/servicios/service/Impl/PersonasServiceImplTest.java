package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Personas;
import com.proyecto.servicios.exception.sf.PersonaNoEncontradaException;
import com.proyecto.servicios.model.EliminaPersonaRequest;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.PersonaResponse;
import com.proyecto.servicios.model.PersonasRequest;
import com.proyecto.servicios.repositorys.sf.PersonasRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonasServiceImplTest {

    @Mock
    private PersonasRepository personasRepository;

    @InjectMocks
    private PersonasServiceImpl personasService;

    @Test
    @DisplayName("creaPersona guarda y retorna respuesta")
    void creaPersona() {
        PersonasRequest req = new PersonasRequest();
        req.setNombre("Juan");
        req.setApellidoP("Perez");
        req.setApellidoMaterno("Lopez");

        PersonaResponse resp = personasService.creaPersona(req);

        assertNotNull(resp);
        assertEquals(0, resp.getCodigo());
        assertEquals("Persona creada correctamente", resp.getMensaje());
        verify(personasRepository).save(any(Personas.class));
    }

    @Test
    @DisplayName("eliminaPersona elimina persona existente")
    void eliminaPersonaExistente() {
        Personas persona = new Personas();
        persona.setNombre("Carlos");
        when(personasRepository.findByNombre("Carlos")).thenReturn(Optional.of(persona));

        EliminaPersonaRequest req = new EliminaPersonaRequest();
        req.setNombre("Carlos");

        GenericResponse resp = personasService.eliminaPersona(req);

        assertEquals(0, resp.getCodigo());
        verify(personasRepository).delete(persona);
    }

    @Test
    @DisplayName("eliminaPersona lanza excepcion si la persona no existe")
    void eliminaPersonaNoExiste() {
        when(personasRepository.findByNombre("Inexistente")).thenReturn(Optional.empty());

        EliminaPersonaRequest req = new EliminaPersonaRequest();
        req.setNombre("Inexistente");

        assertThrows(PersonaNoEncontradaException.class, () -> personasService.eliminaPersona(req));
        verify(personasRepository, never()).delete(any());
    }

    @Test
    @DisplayName("actualizaPersona actualiza datos de persona existente")
    void actualizaPersonaExistente() {
        Personas persona = new Personas();
        persona.setNombre("Maria");
        when(personasRepository.findByNombre("Maria")).thenReturn(Optional.of(persona));

        PersonasRequest req = new PersonasRequest();
        req.setNombre("Maria");
        req.setApellidoP("Gomez");
        req.setApellidoMaterno("Diaz");

        GenericResponse resp = personasService.actualizaPersona(req);

        assertEquals(0, resp.getCodigo());
        assertEquals("Gomez", persona.getApellidoP());
        assertEquals("Diaz", persona.getApellidoMaterno());
        verify(personasRepository).save(persona);
    }

    @Test
    @DisplayName("actualizaPersona lanza excepcion si la persona no existe")
    void actualizaPersonaNoExiste() {
        when(personasRepository.findByNombre("NoExiste")).thenReturn(Optional.empty());

        PersonasRequest req = new PersonasRequest();
        req.setNombre("NoExiste");

        assertThrows(PersonaNoEncontradaException.class, () -> personasService.actualizaPersona(req));
        verify(personasRepository, never()).save(any());
    }
}
