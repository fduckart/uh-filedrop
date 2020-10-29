package edu.hawaii.its.filedrop.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.crypto.Cipher;
import edu.hawaii.its.filedrop.crypto.CipherFilter;
import edu.hawaii.its.filedrop.crypto.CipherLocator;
import edu.hawaii.its.filedrop.crypto.Ciphers;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;

@Service
public class CipherService {

    private static final Log logger = LogFactory.getLog(CipherService.class);

    @Autowired
    private Ciphers ciphers;

    @Autowired
    private CipherLocator cipherLocator;

    public File encrypt(File resource, FileDrop fileDrop)
        throws GeneralSecurityException, IOException {
        String[] encryptionKey = fileDrop.getEncryptionKey().split(":");
        Cipher cipher = cipherLocator.find(encryptionKey[0]);
        javax.crypto.Cipher xcipher = cipher.encrypt(encryptionKey[1]);

        FileInputStream inputStream = new FileInputStream(resource);
        byte[] input = new byte[(int) resource.length()];
        inputStream.read(input);

        byte[] output = xcipher.doFinal(input);

        File encryptedFile = new File(resource.getAbsolutePath() + ".enc");
        FileOutputStream outputStream = new FileOutputStream(encryptedFile);
        outputStream.write(output);

        inputStream.close();
        outputStream.close();

        resource.delete();

        return encryptedFile;
    }

    public File decrypt(File resource, FileDrop fileDrop)
        throws GeneralSecurityException, IOException {
        String[] encryptionKey = fileDrop.getEncryptionKey().split(":");
        Cipher cipher = cipherLocator.find(encryptionKey[0]);
        javax.crypto.Cipher xcipher = cipher.decrypt(encryptionKey[1]);

        FileInputStream inputStream = new FileInputStream(resource);
        byte[] input = new byte[(int) resource.length()];
        inputStream.read(input);

        byte[] output = xcipher.doFinal(input);

        File decryptedFile = new File(resource.getAbsolutePath().substring(0, resource.getAbsolutePath().length() - 5));
        FileOutputStream outputStream = new FileOutputStream(decryptedFile);
        outputStream.write(output);

        inputStream.close();
        outputStream.close();

        return decryptedFile;
    }

    public void encryptFile(String encryptionKey, File original, File encrypted) {
        try {
            CipherFilter filter = ciphers.getDefault(encryptionKey);
            filter.write(new FileInputStream(original), new FileOutputStream(encrypted));
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Cipher Error: " + e.toString());
        }
    }

    public void decryptFile(String encryptionKey, File encrypted, File original) {
        try {
            CipherFilter filter = ciphers.getDefault(encryptionKey);
            filter.read(new FileInputStream(encrypted), new FileOutputStream(original));
        } catch (GeneralSecurityException  | IOException e) {
            logger.error("Cipher Error: " + e.toString());
        }
    }

    public void encryptFileSet(FileSet fileSet, File original, File encrypted) {
        encryptFile(fileSet.getFileDrop().getEncryptionKey(), original, encrypted);
    }

    public void decryptFileSet(FileSet fileSet, File original, File encrypted) {
        decryptFile(fileSet.getFileDrop().getEncryptionKey(), original, encrypted);
    }

}
