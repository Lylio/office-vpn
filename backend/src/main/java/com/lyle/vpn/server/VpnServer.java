package com.lyle.vpn.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class VpnServer {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("VPN_PORT", "5555"));
        DatabaseManager database = new DatabaseManager();

        if (!database.hasAdmin()) {
            String password = "Admin-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            database.createUser("admin", password, "ADMIN");
            System.out.println("============================================");
            System.out.println("Initial admin created");
            System.out.println("Username: admin");
            System.out.println("Temporary password: " + password);
            System.out.println("Change it after first login.");
            System.out.println("============================================");
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Office VPN server listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                Thread.startVirtualThread(() -> new ClientHandler(socket, database).run());
            }
        } catch (IOException e) {
            System.err.println("Server stopped: " + e.getMessage());
        }
    }
}
