package edu.hawaii.its.filedrop.crypto;

import org.springframework.stereotype.Service;

@Service
public class CipherLocator {

    public Cipher find(String code) {
        Cipher foundCipher;

        switch(code) {
            case "rc2":
                foundCipher = new Rc2();
                break;
            default:
                foundCipher = new Aes256();
        }

        return foundCipher;
    }

}
