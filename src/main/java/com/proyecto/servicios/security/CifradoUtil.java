package com.proyecto.servicios.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utilidad de cifrado AES usada por EncryptedStringConverter.
 *
 * Se implementa como metodos estaticos respaldados por una clave cargada
 * una sola vez al arrancar la app (via @PostConstruct), porque Hibernate
 * instancia los AttributeConverter por su cuenta (con new, sin pasar por
 * Spring), asi que un @Value normal dentro del converter nunca se
 * llenaria. Este componente si es manejado por Spring y expone la clave
 * ya cargada a traves de un campo estatico.
 */
@Slf4j
@Component
public class CifradoUtil {

    private static final String ALGORITMO = "AES/ECB/PKCS5Padding";

    @Value("${seguridad.cifrado.clave}")
    private String claveBase64;

    private static String claveEstatica;

    @PostConstruct
    void init() {
        claveEstatica = claveBase64;
    }

    public static String encriptar(String valorPlano) {
        if (valorPlano == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, claveSecreta());
            byte[] cifrado = cipher.doFinal(valorPlano.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cifrado);
        } catch (Exception e) {
            log.error("Error al cifrar un campo: {}", e.getMessage());
            throw new IllegalStateException("No fue posible cifrar la informacion");
        }
    }

    public static String descifrar(String valorCifrado) {
        if (valorCifrado == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, claveSecreta());
            byte[] plano = cipher.doFinal(Base64.getDecoder().decode(valorCifrado));
            return new String(plano, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error al descifrar un campo: {}", e.getMessage());
            throw new IllegalStateException("No fue posible leer la informacion cifrada");
        }
    }

    private static SecretKeySpec claveSecreta() {
        byte[] key = Base64.getDecoder().decode(claveEstatica);
        return new SecretKeySpec(key, "AES");
    }
}
