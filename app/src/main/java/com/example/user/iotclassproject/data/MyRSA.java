package com.example.user.iotclassproject.data;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by YUAN on 2017/6/13.
 */

public class MyRSA {

    public byte[] encrypt(byte[] encrypted, byte[] bPrivateKey){
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(bPrivateKey));
            // 加密
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);

            byte[] enBytes = cipher.doFinal(encrypted);

            return enBytes;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] decrypt(byte[] encrypted, byte[] bPublicKey){
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            KeyFactory kf = KeyFactory.getInstance("RSA");

            PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(bPublicKey));
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            byte[] deBytes = cipher.doFinal(encrypted);

            return deBytes;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }


}
