/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.sdm.core.Globalizer;

import io.jsonwebtoken.impl.crypto.MacProvider;

/**
 *
 * @author Htoonlin
 */
public class SecurityManager {

    public static String generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            random = new SecureRandom();
            throw ex;
        }
        byte salt[] = new byte[64];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashString(String systemSalt, String salt, String input) throws GeneralSecurityException {
        final int iterations = 1000;
        final int keyLength = 512;
        char[] password = input.toCharArray();
        byte[] staticSalt = Base64.getDecoder().decode(systemSalt);
        try {
            PBEKeySpec spec = new PBEKeySpec(password, staticSalt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            String inputHex = DatatypeConverter.printHexBinary(skf.generateSecret(spec).getEncoded());

            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(salt.getBytes());
            String saltHex = DatatypeConverter.printHexBinary(digest.digest());
            String encryptedString = saltHex + inputHex;
            return encryptedString;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw e;
        }
    }

    public static String randomPassword(int length) {
        String passwordChars = "ABCDEFGHIJKLMNOPQRSTUVWHZ";
        passwordChars += passwordChars.toLowerCase();
        passwordChars += "0123456789";
        passwordChars += "!@#$%^&*()_+-=";
        return Globalizer.generateToken(passwordChars, length);
    }

    public static String base64Encode(String normal) {
        byte[] data;
        try {
            data = normal.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            data = normal.getBytes(Charset.defaultCharset());
        }
        return Base64.getEncoder().encodeToString(data);
    }

    public static String base64Decode(String base64) {
        byte[] data = Base64.getDecoder().decode(base64);
        return new String(data);
    }

    public static String generateJWTKey() {
        byte[] key = MacProvider.generateKey().getEncoded();
        return Base64.getEncoder().encodeToString(key);
    }
}
