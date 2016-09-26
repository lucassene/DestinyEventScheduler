package com.destiny.event.scheduler.utils;

import com.destiny.event.scheduler.BuildConfig;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtils {

    private Cipher desCipher;
    private SecretKey aesKey;

    public CipherUtils() {
        try {
            String encryptKey = BuildConfig.ENCRYPT_KEY;
            byte[] key = (encryptKey).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);

            aesKey = new SecretKeySpec(key, "AES");
            desCipher = Cipher.getInstance("AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String value) throws Exception {
        desCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] text = value.getBytes();
        byte[] textEncrypted = desCipher.doFinal(text);
        return bytesToString(textEncrypted);
    }

    public String decrypt(String value) throws Exception {
        desCipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] textDecrypted = desCipher.doFinal(stringToBytes(value));
        return new String(textDecrypted, Charset.forName("ISO_8859_1"));
    }

    private String bytesToString(byte[] b) {
        byte[] b2 = new byte[b.length + 1];
        b2[0] = 1;
        System.arraycopy(b, 0, b2, 1, b.length);
        return new BigInteger(b2).toString(36);
    }

    private byte[] stringToBytes(String s) {
        byte[] b2 = new BigInteger(s, 36).toByteArray();
        return Arrays.copyOfRange(b2, 1, b2.length);
    }

}