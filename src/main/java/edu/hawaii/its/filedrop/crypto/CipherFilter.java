package edu.hawaii.its.filedrop.crypto;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CipherFilter {

    private static final int BUFSIZ = 512;

    private final String key;
    private final Cipher encrypt;
    private final Cipher decrypt;

    public CipherFilter(String key, Cipher encrypt, Cipher decrypt) {
        this.key = key;
        this.encrypt = encrypt;
        this.decrypt = decrypt;
    }

    public String getKey() {
        return this.key;
    }

    /**
     * read unencrypted bytes from in, and write encrypted bytes to out
     *
     * @param in
     * @param out
     * @throws IOException
     */
    public void write(InputStream in, OutputStream out) throws IOException {
        try (OutputStream cipherOut = new CipherOutputStream(out, encrypt)) {
            copy(in, cipherOut);
        }
    }

    /**
     * read encrypted bytes from in, and write unencrypted bytes to out
     *
     * @param in
     * @param out
     * @throws IOException
     */
    public void read(InputStream in, OutputStream out) throws IOException {
        copy(new CipherInputStream(in, decrypt), out);
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[BUFSIZ];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }

}
