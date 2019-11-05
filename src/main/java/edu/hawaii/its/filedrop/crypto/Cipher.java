package edu.hawaii.its.filedrop.crypto;

import java.security.GeneralSecurityException;

public interface Cipher {

    javax.crypto.Cipher decrypt(String key) throws GeneralSecurityException;

    javax.crypto.Cipher encrypt(String key) throws GeneralSecurityException;

    byte[] getSalt();

    String getAlgorithmName();

}
