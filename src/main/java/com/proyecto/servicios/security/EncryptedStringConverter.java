package com.proyecto.servicios.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Cifra/descifra de forma transparente los campos de texto marcados con
 * @Convert(converter = EncryptedStringConverter.class).
 *
 * DECISION DE DISENO: se usa AES/ECB determinístico (mismo texto plano
 * siempre produce el mismo texto cifrado) en vez de AES/CBC con IV
 * aleatorio. Es intencional: CURP, RFC y correo deben poder buscarse
 * con WHERE = y tener restricciones UNIQUE directamente en la base de
 * datos; con cifrado no deterministico esas busquedas dejarian de
 * funcionar. El costo es que dos registros con el mismo valor producen
 * el mismo texto cifrado (se pierde semantic security), aceptado aqui
 * a cambio de mantener las consultas de negocio funcionando.
 *
 * No se marca como @Component: Hibernate instancia los converters por
 * su cuenta (con new), por eso la clave se obtiene de CifradoUtil, que
 * si es un bean de Spring y ya la tiene cargada al momento de usarse.
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String valorPlano) {
        return CifradoUtil.encriptar(valorPlano);
    }

    @Override
    public String convertToEntityAttribute(String valorCifrado) {
        return CifradoUtil.descifrar(valorCifrado);
    }
}
