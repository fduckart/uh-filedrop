package edu.hawaii.its.filedrop.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

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

    public OutputStream encrypt(InputStream inputStream, FileSet fileSet, Path path)
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
        byte[] input = StreamUtils.copyToByteArray(inputStream);
        inputStream.read(input);

        byte[] output = xcipher.doFinal(input);

        File encryptedFile = new File(file.toAbsolutePath().toString() + ".enc");
        FileOutputStream outputStream = new FileOutputStream(encryptedFile);
        outputStream.write(output);

        inputStream.close();
        outputStream.close();

        return outputStream;
    }

    public OutputStream decrypt(InputStream inputStream, FileSet fileSet)
        throws GeneralSecurityException, IOException {
        String[] encryptionKey = fileSet.getFileDrop().getEncryptionKey().split(":");
        Cipher cipher = cipherLocator.find(encryptionKey[0]);
        javax.crypto.Cipher xcipher = cipher.decrypt(encryptionKey[1]);

        byte[] input = StreamUtils.copyToByteArray(inputStream);
        inputStream.read(input);

        byte[] output = xcipher.doFinal(input);

        OutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(output);

        inputStream.close();
        outputStream.close();
        return outputStream;
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
