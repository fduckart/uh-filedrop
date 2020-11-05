package edu.hawaii.its.filedrop.crypto;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public interface Cipher {

    javax.crypto.Cipher decrypt(String key) throws GeneralSecurityException, UnsupportedEncodingException;

    javax.crypto.Cipher encrypt(String key) throws GeneralSecurityException, UnsupportedEncodingException;

    byte[] getSalt();

    String getAlgorithmName();

}
