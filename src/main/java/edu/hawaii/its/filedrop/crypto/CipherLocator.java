package edu.hawaii.its.filedrop.crypto;

import org.springframework.stereotype.Service;

@Service
public class CipherLocator {

    public Cipher find(String code) {
        Cipher cipher;

        switch(code) {
            case "rc2":
                cipher = new Rc2();
                break;
            default:
                cipher = new Aes256();
                break;
        }

        return cipher;
    }

}
