package edu.hawaii.its.filedrop.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CipherLocator {

    @Value("${app.crypto.salt}")
    private String salt;

    @Value("${app.crypto.iv}")
    private String iv;

    public Cipher find(String code) {
        Cipher cipher;

        switch (code) {
            case "rc2":
                cipher = new Rc2();
                break;
            default:
                cipher = new Aes256(iv, salt);
                break;
        }

        return cipher;
    }

}
