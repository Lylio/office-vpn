package com.lyle.vpn.common;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class CryptoUtils {
    private CryptoUtils() {}

    public static KeyPair generateEcKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(256);
        return generator.generateKeyPair();
    }

    public static byte[] encodePublicKey(PublicKey key) {
        return Base64.getEncoder().encode(key.getEncoded());
    }

    public static PublicKey decodePublicKey(byte[] encoded) throws GeneralSecurityException {
        byte[] der = Base64.getDecoder().decode(new String(encoded, StandardCharsets.US_ASCII));
        return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(der));
    }

    public static SecretKey deriveAesKey(PrivateKey privateKey, PublicKey publicKey)
            throws GeneralSecurityException {
        KeyAgreement agreement = KeyAgreement.getInstance("ECDH");
        agreement.init(privateKey);
        agreement.doPhase(publicKey, true);
        byte[] secret = agreement.generateSecret();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(secret);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] encrypt(SecretKey key, byte[] plaintext) throws GeneralSecurityException {
        byte[] nonce = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce));
        byte[] ciphertext = cipher.doFinal(plaintext);

        ByteBuffer buffer = ByteBuffer.allocate(4 + nonce.length + ciphertext.length);
        buffer.putInt(nonce.length);
        buffer.put(nonce);
        buffer.put(ciphertext);
        return buffer.array();
    }

    public static byte[] decrypt(SecretKey key, byte[] packet) throws GeneralSecurityException {
        ByteBuffer buffer = ByteBuffer.wrap(packet);
        int nonceLength = buffer.getInt();

        if (nonceLength != 12 || buffer.remaining() <= nonceLength) {
            throw new GeneralSecurityException("Invalid encrypted packet");
        }

        byte[] nonce = new byte[nonceLength];
        buffer.get(nonce);
        byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, nonce));
        return cipher.doFinal(ciphertext);
    }

    public static String hmacSha256(String secret, String value) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
