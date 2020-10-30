package edu.hawaii.its.filedrop.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.crypto.Cipher;
import edu.hawaii.its.filedrop.crypto.CipherFilter;
import edu.hawaii.its.filedrop.crypto.CipherLocator;
import edu.hawaii.its.filedrop.crypto.Ciphers;
import edu.hawaii.its.filedrop.type.FileSet;

@Service
public class CipherService {

    private static final Log logger = LogFactory.getLog(CipherService.class);

    @Autowired
    private Ciphers ciphers;

    @Autowired
    private CipherLocator cipherLocator;

    @Autowired
    private FileSystemStorageService storageService;

    public Resource encrypt(Resource resource, FileSet fileSet, Path path)
        throws GeneralSecurityException, IOException {
        Path file;
        if (path == null) {
            file = Paths.get(storageService.getRootLocation().toString(), fileSet.getFileDrop().getDownloadKey(),
                fileSet.getId().toString());
        } else {
            file = path;
        }
        String[] encryptionKey = fileSet.getFileDrop().getEncryptionKey().split(":");
        Cipher cipher = cipherLocator.find(encryptionKey[0]);
        javax.crypto.Cipher xcipher = cipher.encrypt(encryptionKey[1]);
        FileInputStream inputStream = new FileInputStream(file.toFile());
        byte[] input = new byte[(int) resource.contentLength()];
        inputStream.read(input);

        byte[] output = xcipher.doFinal(input);

        File encryptedFile = new File(file.toFile().getAbsolutePath() + ".enc");
        FileOutputStream outputStream = new FileOutputStream(encryptedFile);
        outputStream.write(output);

        inputStream.close();
        outputStream.close();

        return storageService.loadAsResource(encryptedFile.getAbsolutePath());
    }

    public Resource decrypt(Resource resource, FileSet fileSet, Path encPath, Path decPath)
        throws GeneralSecurityException, IOException {
        String[] encryptionKey = fileSet.getFileDrop().getEncryptionKey().split(":");
        Cipher cipher = cipherLocator.find(encryptionKey[0]);
        javax.crypto.Cipher xcipher = cipher.decrypt(encryptionKey[1]);
        Path encFile;
        if (encPath != null) {
            encFile = encPath;
        } else {
            encFile = Paths.get(storageService.getRootLocation().toString(), fileSet.getFileDrop().getDownloadKey(),
                fileSet.getId().toString() + ".enc");
        }
        FileInputStream inputStream = new FileInputStream(encFile.toFile());
        byte[] input = new byte[(int) resource.contentLength()];
        inputStream.read(input);

        byte[] output = xcipher.doFinal(input);
        Path decFile;
        if (decPath != null) {
            decFile = decPath;
        } else {
            decFile = Paths.get(storageService.getRootLocation().toString(), fileSet.getFileDrop().getDownloadKey(),
                fileSet.getId().toString());
        }

        File decryptedFile = new File(decFile.toUri());
        FileOutputStream outputStream = new FileOutputStream(decryptedFile);
        outputStream.write(output);

        inputStream.close();
        outputStream.close();

        return storageService.loadAsResource(decryptedFile.getAbsolutePath());
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
