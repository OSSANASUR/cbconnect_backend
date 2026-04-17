package com.ossanasur.cbconnect.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class EncryptionToken {
    @Value("${encryption.secret.key}")
    private String secretKey;

    private SecretKeySpec getKey() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(secretKey.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }

    public String encrypt(String s) {
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, getKey());
            return Base64.getEncoder().encodeToString(c.doFinal(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Erreur de chiffrement", e);
        }
    }

    public String decrypt(String s) {
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, getKey());
            return new String(c.doFinal(Base64.getDecoder().decode(s)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de dechiffrement", e);
        }
    }
}
