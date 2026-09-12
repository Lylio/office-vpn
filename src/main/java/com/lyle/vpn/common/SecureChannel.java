package com.lyle.vpn.common;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public final class SecureChannel {
    private final DataInputStream in;
    private final DataOutputStream out;
    private SecretKey key;

    public SecureChannel(InputStream input, OutputStream output) {
        this.in = new DataInputStream(new BufferedInputStream(input));
        this.out = new DataOutputStream(new BufferedOutputStream(output));
    }

    public void setKey(SecretKey key) {
        this.key = key;
    }

    public synchronized void send(String message) throws IOException, GeneralSecurityException {
        if (key == null) throw new IOException("Secure key not established");
        byte[] plaintext = message.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = CryptoUtils.encrypt(key, plaintext);
        out.writeInt(encrypted.length);
        out.write(encrypted);
        out.flush();
    }

    public String receive() throws IOException, GeneralSecurityException {
        if (key == null) throw new IOException("Secure key not established");
        int length = in.readInt();
        if (length < 1 || length > 1024 * 1024) {
            throw new IOException("Invalid frame length");
        }
        byte[] encrypted = new byte[length];
        in.readFully(encrypted);
        return new String(CryptoUtils.decrypt(key, encrypted), StandardCharsets.UTF_8);
    }

    public DataInputStream rawIn() {
        return in;
    }

    public DataOutputStream rawOut() {
        return out;
    }
}
