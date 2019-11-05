package edu.hawaii.its.filedrop.crypto;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.NullCipher;

class NullCipherFilterFactory extends CipherFilterFactory {
    NullCipherFilterFactory() {
        super("null");
    }

    @Override
    protected Cipher makeDecryptionCipher(String pw) throws GeneralSecurityException {
        return new NullCipher();
    }

    @Override
    protected Cipher makeEncryptionCipher(String pw) throws GeneralSecurityException {
        return new NullCipher();
    }
}