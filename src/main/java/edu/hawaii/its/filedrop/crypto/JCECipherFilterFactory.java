package edu.hawaii.its.filedrop.crypto;

import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

class JCECipherFilterFactory extends CipherFilterFactory {

    private String algorithmName;

    /*
     * I don't see that a salt is going to be of much use for us, but the
     * algorithms (at least the DES version) seem to require one.
     */
    private byte[] salt = {
            (byte) 0x23, (byte) 0x12, (byte) 0xd3, (byte) 0x31,
            (byte) 0x1f, (byte) 0x33, (byte) 0xbc, (byte) 0xf0
    };

    private int iterationCount = 13;

    static CipherFilterFactory makeDESCipherFilterFactory() {
        return new JCECipherFilterFactory("des", "PBEWithMD5AndDES");
    }

    static CipherFilterFactory make3DESCipherFilterFactory() {
        return new JCECipherFilterFactory("3des", "PBEWithSHA1AndDESede");
    }

    static CipherFilterFactory makeRC2FilterFactory() {
        return new JCECipherFilterFactory("rc2", "PBEWithSHA1AndRC2_40");
    }

    private JCECipherFilterFactory(String type, String alg) {
        super(type);
        this.algorithmName = alg;
    }

    @Override
    protected Cipher makeDecryptionCipher(String pw) throws GeneralSecurityException {
        SecretKeyFactory kf = SecretKeyFactory.getInstance(algorithmName);
        PBEKeySpec keySpec = new PBEKeySpec(pw.toCharArray(), salt, iterationCount);
        SecretKey k = kf.generateSecret(keySpec);
        Cipher c = Cipher.getInstance(algorithmName);
        PBEParameterSpec pSpec = new PBEParameterSpec(salt, iterationCount);
        c.init(Cipher.DECRYPT_MODE, k, pSpec);
        return c;
    }

    @Override
    protected Cipher makeEncryptionCipher(String pw) throws GeneralSecurityException {
        SecretKeyFactory kf = SecretKeyFactory.getInstance(algorithmName);
        PBEKeySpec keySpec = new PBEKeySpec(pw.toCharArray());
        SecretKey k = kf.generateSecret(keySpec);
        Cipher c = Cipher.getInstance(algorithmName);
        PBEParameterSpec pSpec = new PBEParameterSpec(salt, iterationCount);
        c.init(Cipher.ENCRYPT_MODE, k, pSpec);
        return c;
    }
}