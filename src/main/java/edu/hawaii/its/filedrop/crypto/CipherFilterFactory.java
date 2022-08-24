package edu.hawaii.its.filedrop.crypto;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;

abstract class CipherFilterFactory {

    private final String type;

    CipherFilterFactory(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public CipherFilter get(String pw) throws GeneralSecurityException {
        return new CipherFilter(type + ":" + pw,
                makeEncryptionCipher(pw),
                makeDecryptionCipher(pw));
    }

    abstract protected Cipher makeEncryptionCipher(String pw) throws GeneralSecurityException;

    abstract protected Cipher makeDecryptionCipher(String pw) throws GeneralSecurityException;

}
