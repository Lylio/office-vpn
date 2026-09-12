package com.lyle.vpn.client;

import com.lyle.vpn.common.CryptoUtils;
import com.lyle.vpn.common.Protocol;
import com.lyle.vpn.common.SecureChannel;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.KeyPair;

public final class VpnClient implements AutoCloseable {
    private final String host;
    private final int port;
    private Socket socket;
    private SecureChannel channel;
    private String username;
    private String role;

    public VpnClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect(String username, String password) throws Exception {
        socket = new Socket(host, port);
        socket.setTcpNoDelay(true);
        channel = new SecureChannel(socket.getInputStream(), socket.getOutputStream());

        DataInputStream in = channel.rawIn();
        DataOutputStream out = channel.rawOut();

        String hello = in.readUTF();
        if (!Protocol.HELLO.equals(hello)) throw new IOException("Invalid server handshake");

        int serverKeyLength = in.readInt();
        if (serverKeyLength < 1 || serverKeyLength > 8192) throw new IOException("Invalid server key");
        byte[] serverKey = new byte[serverKeyLength];
        in.readFully(serverKey);

        KeyPair clientKeys = CryptoUtils.generateEcKeyPair();
        byte[] clientPublic = CryptoUtils.encodePublicKey(clientKeys.getPublic());
        out.writeInt(clientPublic.length);
        out.write(clientPublic);
        out.flush();

        SecretKey aes = CryptoUtils.deriveAesKey(
                clientKeys.getPrivate(),
                CryptoUtils.decodePublicKey(serverKey)
        );
        channel.setKey(aes);

        channel.send(Protocol.AUTH + "|" + username + "|" + password);
        String result = channel.receive();

        String[] parts = result.split("\\|", 4);
        if (parts.length < 3 || !"OK".equals(parts[1])) {
            close();
            throw new IOException(parts.length >= 3 ? parts[2] : "Authentication failed");
        }

        this.username = parts[2];
        this.role = parts.length >= 4 ? parts[3] : "USER";
    }

    public String sendMessage(String message) throws Exception {
        channel.send(Protocol.MESSAGE + "|" + message);
        return channel.receive();
    }

    public String listUsers() throws Exception {
        channel.send(Protocol.ADMIN_LIST_USERS);
        return channel.receive();
    }

    public String createUser(String username, String password, String role) throws Exception {
        channel.send(Protocol.ADMIN_CREATE_USER + "|" + username + "|" + password + "|" + role);
        return channel.receive();
    }

    public String setEnabled(String username, boolean enabled) throws Exception {
        channel.send(Protocol.ADMIN_SET_ENABLED + "|" + username + "|" + enabled);
        return channel.receive();
    }

    public String logs() throws Exception {
        channel.send(Protocol.ADMIN_LOGS);
        return channel.receive();
    }

    public String username() {
        return username;
    }

    public String role() {
        return role;
    }

    @Override
    public void close() {
        if (channel != null) {
            try { channel.send(Protocol.DISCONNECT); } catch (Exception ignored) {}
        }
        if (socket != null) {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
