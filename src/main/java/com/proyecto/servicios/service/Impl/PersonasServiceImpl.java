package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Personas;
import com.proyecto.servicios.exception.sf.PersonaNoEncontradaException;
import com.proyecto.servicios.model.EliminaPersonaRequest;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.PersonaResponse;
import com.proyecto.servicios.model.PersonasRequest;
import com.proyecto.servicios.repositorys.sf.PersonasRepository;
import com.proyecto.servicios.service.PersonaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PersonasServiceImpl implements PersonaService {
    @Autowired
    private PersonasRepository personasRepository;
    @Override
    public PersonaResponse creaPersona(PersonasRequest personasRequest) {
        PersonaResponse person=new PersonaResponse();
     Personas persona=new Personas();
     persona.setNombre(personasRequest.getNombre());
     persona.setApellidoMaterno(personasRequest.getApellidoMaterno());
     persona.setApellidoP(personasRequest.getApellidoP());
     personasRepository.save(persona);
     person.setCodigo(0);
     person.setMensaje("Persona creada correctamente");
     BeanUtils.copyProperties(persona,person);

     return person;
    }

    @Override
    public GenericResponse eliminaPersona(EliminaPersonaRequest eliminaPersonaRequest) {
        GenericResponse genericResponse=new GenericResponse();

        Personas personaElimina = personasRepository.findByNombre(eliminaPersonaRequest.getNombre())
                .orElseThrow(() -> new PersonaNoEncontradaException("La persona no existe"));

        personasRepository.delete(personaElimina);
        genericResponse.setCodigo(0);
        genericResponse.setMensaje("La persona ha sido eliminada correctamente");
        return genericResponse;
    }

    @Override
    public GenericResponse actualizaPersona(PersonasRequest personasRequest) {
        GenericResponse genericResponse=new GenericResponse();
        Personas personaActualiza = personasRepository.findByNombre(personasRequest.getNombre())
                .orElseThrow(() -> new PersonaNoEncontradaException("La persona no existe"));

        personaActualiza.setApellidoP(personasRequest.getApellidoP());
        personaActualiza.setApellidoMaterno(personasRequest.getApellidoMaterno());
        personasRepository.save(personaActualiza);
        genericResponse.setCodigo(0);
        genericResponse.setMensaje("La persona ha sido actualizada correctamente");
        return genericResponse;
    }
}
