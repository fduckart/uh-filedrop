package edu.hawaii.its.filedrop.crypto;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Ciphers {

    private static final Logger logger = LoggerFactory.getLogger(Ciphers.class);
    private final CipherFilterFactory defaultCipher;
    private final Map<String, CipherFilterFactory> ciphers;
    private CipherFilterFactory nullCipher;

    public Ciphers() {

        ciphers = new HashMap<>();

        CipherFilterFactory cff;
        cff = JCECipherFilterFactory.makeRC2FilterFactory();
        this.defaultCipher = cff;
        ciphers.put(cff.getType(), cff);

        cff = JCECipherFilterFactory.makeDESCipherFilterFactory();
        ciphers.put(cff.getType(), cff);

        cff = JCECipherFilterFactory.make3DESCipherFilterFactory();
        ciphers.put(cff.getType(), cff);

    }

    public CipherFilter getDefault(String pw) throws GeneralSecurityException {
        return defaultCipher.get(pw);
    }

    public CipherFilter get(String k) throws GeneralSecurityException {
        if (k == null || k.length() == 0) {
            logger.warn("null encrption key ... using 'null' filter");
            return nullCipher.get("");
        }
        int i = k.indexOf(':');
        if (i <= 0 || k.length() == i) {
            throw new IllegalArgumentException("invalid key '" + k + "'");
        }
        CipherFilterFactory cff = ciphers.get(k.substring(0, i));
        if (cff == null) {
            throw new IllegalArgumentException("invalid key '" + k + "' --  bad algorithm");
        }
        return cff.get(k.substring(i + 1));
    }

    /**
     * might be useful in testing
     *
     * @param pw
     * @return
     * @throws GeneralSecurityException
     */
    public Map<String, CipherFilter> getCiphers(String pw) throws GeneralSecurityException {
        Map<String, CipherFilter> c = new HashMap<String, CipherFilter>();
        for (CipherFilterFactory cff : ciphers.values()) {
            c.put(cff.getType(), cff.get(pw));
        }
        return c;
    }
}
