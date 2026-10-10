package com.proyecto.servicios.config;

import com.proyecto.servicios.entity.cliente.Cliente;
import com.proyecto.servicios.repositorys.cliente.ClienteRepository;
import com.proyecto.servicios.util.CorreoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Corrige los correos que se guardaron con mayusculas ANTES de que existiera
 * la regla de minusculas. Corre al iniciar la app y solo modifica los que
 * lo necesitan; cuando ya todos estan en minusculas no hace nada.
 *
 * No se puede hacer con un script SQL de Flyway porque el correo esta
 * cifrado: hay que leerlo desde Java (Hibernate lo descifra), pasarlo a
 * minusculas y guardarlo (Hibernate lo vuelve a cifrar).
 */
@Slf4j
@Component
public class NormalizarCorreosRunner implements ApplicationRunner {

    private final ClienteRepository clienteRepository;

    public NormalizarCorreosRunner(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Cliente> clientes = clienteRepository.findAll();

        Set<String> yaUsados = new HashSet<>();
        for (Cliente c : clientes) {
            if (c.getCorreo() != null && c.getCorreo().equals(CorreoUtil.normalizar(c.getCorreo()))) {
                yaUsados.add(c.getCorreo());
            }
        }

        int corregidos = 0;
        for (Cliente c : clientes) {
            String actual = c.getCorreo();
            String normalizado = CorreoUtil.normalizar(actual);
            if (actual == null || actual.equals(normalizado)) {
                continue;
            }
            // Si ya existe otro cliente con ese mismo correo en minusculas,
            // no se toca para no romper la restriccion UNIQUE.
            if (!yaUsados.add(normalizado)) {
                log.warn("Cliente id={} no se normalizo: su correo en minusculas ya lo usa otro cliente", c.getId());
                continue;
            }
            c.setCorreo(normalizado);
            clienteRepository.save(c);
            corregidos++;
        }

        if (corregidos > 0) {
            log.info("Correos convertidos a minusculas: {}", corregidos);
        }
    }
}
