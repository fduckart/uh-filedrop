package edu.hawaii.its.filedrop.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.crypto.CipherFilter;
import edu.hawaii.its.filedrop.crypto.Ciphers;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;

@Service
public class CipherService {

    private static final Log logger = LogFactory.getLog(CipherService.class);

    @Autowired
    private Ciphers ciphers;

    public Resource encrypt(Resource resource, FileDrop fileDrop) {
        String encryptionKey = fileDrop.getEncryptionKey();
        return resource;
    }

    public Resource decrypt(Resource resource, FileDrop fileDrop) {
        String encryptionKey = fileDrop.getEncryptionKey();
        return resource;
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
