package edu.hawaii.its.filedrop.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.GeneralSecurityException;

public class Rc2 implements Cipher {

    private final int iterationCount = 13;

    @Override
    public javax.crypto.Cipher decrypt(String key) throws GeneralSecurityException {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(getAlgorithmName());
        PBEKeySpec keySpec = new PBEKeySpec(key.toCharArray(), getSalt(), iterationCount);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(getAlgorithmName());
        PBEParameterSpec parameterSpec = new PBEParameterSpec(getSalt(), iterationCount);
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        return cipher;
    }

    @Override
    public javax.crypto.Cipher encrypt(String key) throws GeneralSecurityException {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(getAlgorithmName());
        PBEKeySpec keySpec = new PBEKeySpec(key.toCharArray());
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(getAlgorithmName());
        PBEParameterSpec parameterSpec = new PBEParameterSpec(getSalt(), iterationCount);
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        return cipher;
    }

    @Override
    public byte[] getSalt() {
        return new byte[] {
                (byte) 0x23, (byte) 0x12, (byte) 0xd3, (byte) 0x31,
                (byte) 0x1f, (byte) 0x33, (byte) 0xbc, (byte) 0xf0
        };
    }

    @Override
    public String getAlgorithmName() {
        return "PBEWithSHA1AndRC2_40";
    }

    @Override
    public String toString() {
        return "Rc2 [algorithmName=" + getAlgorithmName() + "]";
    }
}
