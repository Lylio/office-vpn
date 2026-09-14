package com.lyle.vpn.api;

import com.lyle.vpn.server.DatabaseManager;
import org.springframework.stereotype.Service;

@Service
public class VpnSessionService {

    private final DatabaseManager databaseManager = new DatabaseManager();

    private boolean connected = false;
    private Long currentLogId = null;

    public synchronized boolean connect() throws Exception {
        if (connected) {
            return true;
        }

        currentLogId = databaseManager.logConnection(
                "admin",
                "127.0.0.1",
                "CONNECTED"
        );

        connected = true;

        return true;
    }

    public synchronized boolean disconnect() throws Exception {
        if (!connected) {
            return true;
        }

        if (currentLogId != null) {
            databaseManager.closeConnectionLog(currentLogId);
        }

        connected = false;
        currentLogId = null;

        return true;
    }

    public synchronized boolean isConnected() {
        return connected;
    }
}
