package edu.hawaii.its.filedrop.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;

public class Aes256 implements Cipher {

    private final String iv;
    private final String salt;

    public Aes256(String iv, String salt) {
        this.iv = iv;
        this.salt = salt;
    }

    public javax.crypto.Cipher crypto(String key, int cipherMode) throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        int iterations = 65536;
        int length = 256;
        KeySpec keySpec = new PBEKeySpec(key.toCharArray(), getSalt(), iterations, length);
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        byte[] iv = this.iv.getBytes(StandardCharsets.UTF_8);
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(getAlgorithmName());
        cipher.init(cipherMode, secret, new IvParameterSpec(iv));
        return cipher;
    }

    @Override
    public javax.crypto.Cipher decrypt(String key) throws GeneralSecurityException {
        return crypto(key, javax.crypto.Cipher.DECRYPT_MODE);
    }

    @Override
    public javax.crypto.Cipher encrypt(String key) throws GeneralSecurityException {
        return crypto(key, javax.crypto.Cipher.ENCRYPT_MODE);
    }

    @Override
    public byte[] getSalt() {
        return this.salt.getBytes();
    }

    @Override
    public String getAlgorithmName() {
        return "AES/CBC/PKCS5Padding";
    }

}
