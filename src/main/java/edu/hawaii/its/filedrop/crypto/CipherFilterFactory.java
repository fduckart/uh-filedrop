package edu.hawaii.its.filedrop.crypto;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;

abstract class CipherFilterFactory {

    private String type;

    CipherFilterFactory(String t) {
        this.type = t;
    }

    public String getType() {
        return this.type;
    }

    public CipherFilter get(String pw) throws GeneralSecurityException {
        return new CipherFilter(type + ":" + pw,
                makeEncryptionCipher(pw),
                makeDecryptionCipher(pw));
    }

    abstract protected Cipher makeEncryptionCipher(String pw) throws GeneralSecurityException;

    abstract protected Cipher makeDecryptionCipher(String pw) throws GeneralSecurityException;

}