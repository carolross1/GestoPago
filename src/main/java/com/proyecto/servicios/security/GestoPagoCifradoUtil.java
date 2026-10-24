package com.proyecto.servicios.security;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utilidad de cifrado AES-256-CBC con PKCS5Padding requerida por la API
 * transaccional de GestoPago / PuntoRed (sendTx).
 */
@Slf4j
public final class GestoPagoCifradoUtil {

    private static final String ALGORITMO = "AES/CBC/PKCS5Padding";

    private GestoPagoCifradoUtil() {
    }

    public static String cifrar(String textoPlano, String secretKeyBase64, String ivBase64) {
        if (textoPlano == null) {
            return null;
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
            byte[] ivBytes = Base64.getDecoder().decode(ivBase64);

            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cifrado);
        } catch (Exception e) {
            log.error("Error al cifrar payload transaccional para GestoPago: {}", e.getMessage(), e);
            throw new IllegalStateException("No fue posible cifrar el payload transaccional de GestoPago", e);
        }
    }

    public static String descifrar(String textoCifrado, String secretKeyBase64, String ivBase64) {
        if (textoCifrado == null) {
            return null;
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
            byte[] ivBytes = Base64.getDecoder().decode(ivBase64);

            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] plano = cipher.doFinal(Base64.getDecoder().decode(textoCifrado));
            return new String(plano, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error al descifrar payload transaccional de GestoPago: {}", e.getMessage(), e);
            throw new IllegalStateException("No fue posible descifrar el payload transaccional de GestoPago", e);
        }
    }
}
