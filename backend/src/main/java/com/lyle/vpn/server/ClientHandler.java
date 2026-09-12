package com.lyle.vpn.server;

import com.lyle.vpn.common.CryptoUtils;
import com.lyle.vpn.common.Protocol;
import com.lyle.vpn.common.SecureChannel;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;

public final class ClientHandler {
    private final Socket socket;
    private final DatabaseManager db;
    private String username;
    private long connectionLogId = -1;

    public ClientHandler(Socket socket, DatabaseManager db) {
        this.socket = socket;
        this.db = db;
    }

    public void run() {
        try (socket) {
            SecureChannel channel = new SecureChannel(socket.getInputStream(), socket.getOutputStream());
            establishEncryption(channel);
            authenticate(channel);
            commandLoop(channel);
        } catch (Exception e) {
            System.err.println("Client " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        } finally {
            if (connectionLogId > 0) {
                try { db.closeConnectionLog(connectionLogId); } catch (Exception ignored) {}
            }
        }
    }

    private void establishEncryption(SecureChannel channel)
            throws IOException, GeneralSecurityException {
        DataInputStream in = channel.rawIn();
        DataOutputStream out = channel.rawOut();

        KeyPair serverKeys = CryptoUtils.generateEcKeyPair();

        out.writeUTF(Protocol.HELLO);
        byte[] publicKey = CryptoUtils.encodePublicKey(serverKeys.getPublic());
        out.writeInt(publicKey.length);
        out.write(publicKey);
        out.flush();

        int clientKeyLength = in.readInt();
        if (clientKeyLength < 1 || clientKeyLength > 8192) throw new IOException("Invalid key");
        byte[] clientKey = new byte[clientKeyLength];
        in.readFully(clientKey);

        SecretKey aesKey = CryptoUtils.deriveAesKey(
                serverKeys.getPrivate(),
                CryptoUtils.decodePublicKey(clientKey)
        );
        channel.setKey(aesKey);
    }

    private void authenticate(SecureChannel channel)
            throws IOException, GeneralSecurityException {
        String request = channel.receive();
        if (!request.startsWith(Protocol.AUTH + "|")) throw new IOException("Authentication required");

        String[] parts = request.split("\\|", 3);
        if (parts.length != 3) throw new IOException("Invalid authentication request");

        UserResult result;
        try {
            DatabaseManager.User user = db.authenticate(parts[1], parts[2]);
            if (user == null) {
                channel.send(Protocol.AUTH_RESULT + "|FAIL|Invalid credentials or disabled account");
                throw new IOException("Authentication failed");
            }
            username = user.username();
            role = user.role();
            result = new UserResult(user.username(), user.role());
            connectionLogId = db.logConnection(username,
                    socket.getInetAddress().getHostAddress(), "CONNECTED");
        } catch (java.sql.SQLException e) {
            channel.send(Protocol.AUTH_RESULT + "|FAIL|Database error");
            throw new IOException("Database authentication failed", e);
        }

        channel.send(Protocol.AUTH_RESULT + "|OK|" + result.username + "|" + result.role);
    }

    private void commandLoop(SecureChannel channel) throws Exception {
        while (true) {
            String request = channel.receive();
            if (request.equals(Protocol.DISCONNECT)) return;

            if (request.startsWith(Protocol.MESSAGE + "|")) {
                channel.send(Protocol.MESSAGE + "|SERVER|Received securely: " +
                        request.substring((Protocol.MESSAGE + "|").length()));
            } else if (request.equals(Protocol.ADMIN_LIST_USERS)) {
                requireAdmin();
                List<DatabaseManager.User> users = db.listUsers();
                StringBuilder response = new StringBuilder(Protocol.RESPONSE + "|USERS|");
                for (DatabaseManager.User u : users) {
                    response.append(escape(u.username())).append(",")
                            .append(u.role()).append(",").append(u.enabled()).append(";");
                }
                channel.send(response.toString());
            } else if (request.startsWith(Protocol.ADMIN_CREATE_USER + "|")) {
                requireAdmin();
                String[] p = request.split("\\|", 4);
                if (p.length != 4) throw new IOException("Invalid user creation request");
                db.createUser(p[1], p[2], p[3]);
                channel.send(Protocol.RESPONSE + "|OK|User created");
            } else if (request.startsWith(Protocol.ADMIN_SET_ENABLED + "|")) {
                requireAdmin();
                String[] p = request.split("\\|", 3);
                if (p.length != 3) throw new IOException("Invalid enable request");
                db.setEnabled(p[1], Boolean.parseBoolean(p[2]));
                channel.send(Protocol.RESPONSE + "|OK|User updated");
            } else if (request.equals(Protocol.ADMIN_LOGS)) {
                requireAdmin();
                List<String> logs = db.recentLogs();
                channel.send(Protocol.RESPONSE + "|LOGS|" +
                        Base64.getEncoder().encodeToString(String.join("\n", logs)
                                .getBytes(StandardCharsets.UTF_8)));
            } else {
                channel.send(Protocol.RESPONSE + "|ERROR|Unknown command");
            }
        }
    }

    private void requireAdmin() throws IOException {
        // Role is set during authentication and retained by this handler.
        if (!"ADMIN".equals(role)) throw new IOException("Admin privileges required");
    }

    private String role;
    private record UserResult(String username, String role) {
        private UserResult {
            // no-op
        }
    }

    private static String escape(String value) {
        return value.replace(",", "%2C").replace(";", "%3B");
    }
}
