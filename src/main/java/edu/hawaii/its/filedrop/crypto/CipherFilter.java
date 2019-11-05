package edu.hawaii.its.filedrop.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

public class CipherFilter {

    private static final int BUFSIZ = 512;

    private String key;

    private Cipher encrypt, decrypt;

    public CipherFilter(String key, Cipher e, Cipher d) {
        this.key = key;
        this.encrypt = e;
        this.decrypt = d;
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
    public void write(InputStream in, OutputStream out) throws IOException, GeneralSecurityException {
        OutputStream cipherOut = new CipherOutputStream(out, encrypt);
        copy(in, cipherOut);
        cipherOut.close(); // flush the output.  this is important.
    }

    /**
     * read encrypted bytes from in, and write unencrypted bytes to out
     * 
     * @param in
     * @param out
     * @throws IOException
     */
    public void read(InputStream in, OutputStream out) throws IOException, GeneralSecurityException {
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
