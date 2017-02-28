package com.example.user.preconsumerapp;

import org.bouncycastle.openssl.PEMReader;

import java.io.FileReader;
import java.security.Key;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;

/**
 * Created by CheahHong on 2/20/2017.
 */

public class VerifyHash {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    //public String DecryptHash(PublicKey key,String encodedEncryptedHash)throws Exception{
    public String DecryptHash(PublicKey key, byte [] EncryptedHash)throws Exception{
        //byte[] encryptedHash = hexStringToByteArray(encodedEncryptedHash);
        String decryptedHash = decrypt(EncryptedHash,key);
        return decryptedHash;
    }

    public PublicKey ReadPemFile(String path) throws Exception {
        PEMReader reader = new PEMReader(new FileReader(path));
        Object pemObject = reader.readObject();
        if (pemObject instanceof X509Certificate) {
            X509Certificate cert = (X509Certificate)pemObject;
            cert.checkValidity();
            return cert.getPublicKey();
        }
        return null;
    }

    public String hashStringWithSHA(String json) throws Exception{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(json.getBytes("UTF-8"));

        byte byteData[] = md.digest();

        //convert the byte to hex format
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
    public Boolean CompareHash(String original,String rehash){
        if(original.equals(rehash)){
            return true;
        }
        return false;
    }

    public String decrypt(byte[]text,Key key) throws Exception{
        byte[] decryptedText = null;
        try {
            // get an RSA cipher object and print the provider
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedText = cipher.doFinal(text);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(decryptedText,"UTF-8");
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
