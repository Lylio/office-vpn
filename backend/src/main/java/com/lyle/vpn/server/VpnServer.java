package com.lyle.vpn.server;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Service
public final class VpnServer {

    private final int port =
            Integer.parseInt(System.getenv().getOrDefault("VPN_PORT", "5555"));

    private final DatabaseManager database = new DatabaseManager();

    private volatile boolean running = false;

    private ServerSocket serverSocket;

    @PostConstruct
    public void start() {

        Thread.startVirtualThread(() -> {

            try {

                createInitialAdminIfNeeded();

                serverSocket = new ServerSocket(port);

                running = true;

                System.out.println(
                        "Office VPN server listening on port " + port
                );

                while (running) {

                    try {

                        Socket socket = serverSocket.accept();

                        socket.setTcpNoDelay(true);

                        Thread.startVirtualThread(
                                () -> new ClientHandler(
                                        socket,
                                        database
                                ).run()
                        );

                    } catch (IOException e) {

                        if (running) {
                            System.err.println(
                                    "Error accepting VPN client: "
                                            + e.getMessage()
                            );
                        }
                    }
                }

            } catch (Exception e) {

                running = false;

                System.err.println(
                        "VPN server failed to start: "
                                + e.getMessage()
                );

                e.printStackTrace();
            }
        });
    }

    private void createInitialAdminIfNeeded() throws Exception {

        if (!database.hasAdmin()) {

            String password =
                    "Admin-"
                            + java.util.UUID.randomUUID()
                            .toString()
                            .substring(0, 8);

            database.createUser(
                    "admin",
                    password,
                    "ADMIN"
            );

            System.out.println(
                    "============================================"
            );

            System.out.println("Initial admin created");
            System.out.println("Username: admin");
            System.out.println(
                    "Temporary password: " + password
            );
            System.out.println(
                    "Change it after first login."
            );

            System.out.println(
                    "============================================"
            );
        }
    }

    @PreDestroy
    public void stop() {

        running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {

            try {

                serverSocket.close();

                System.out.println(
                        "Office VPN server stopped"
                );

            } catch (IOException e) {

                System.err.println(
                        "Error stopping VPN server: "
                                + e.getMessage()
                );
            }
        }
    }

    public boolean isRunning() {

        return running
                && serverSocket != null
                && !serverSocket.isClosed();
    }

    public int getPort() {
        return port;
    }
}