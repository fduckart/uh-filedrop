package edu.hawaii.its.filedrop.crypto;

import java.security.GeneralSecurityException;

public class Aes256 implements Cipher {

    @Override
    public javax.crypto.Cipher decrypt(String key) throws GeneralSecurityException {
        return null;
    }

    @Override
    public javax.crypto.Cipher encrypt(String key) throws GeneralSecurityException {
        return null;
    }

    @Override
    public byte[] getSalt() {
        return new byte[0];
    }

    @Override
    public String getAlgorithmName() {
        return null;
    }

}
