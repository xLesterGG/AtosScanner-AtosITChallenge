package com.example.user.preconsumerapp;

import android.util.Base64;

import java.io.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by CheahHong on 2/14/2017.
 */

public class VerSig {
    //private static final String dataFile_URL = "C:\\Users\\CheahHong\\Desktop\\test.txt";
    public VerSig(){}

    public Boolean verifySignature(String pubKey,String signature){
        try {
            // the rest of the code goes here

            // decode the base64 encoded string
            byte[] decodedKey = Base64.decode(pubKey,0);

            // rebuild key
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(decodedKey);

            //get keyfactory object and generate pub key from key specification
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            PublicKey repubKey = keyFactory.generatePublic(pubKeySpec);

            //get signature byte
            byte[] sigToVerify = Base64.decode(signature,0);

            //create signature instance class and initialize it
            Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
            sig.initVerify(repubKey);

            //supply data file to be verified
            sig.update(sigToVerify);

            //verify the signature
            boolean verifies = sig.verify(sigToVerify);

            return verifies;
        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
        }
        return false;
    }
}
