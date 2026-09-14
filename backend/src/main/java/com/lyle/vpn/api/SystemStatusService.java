package com.lyle.vpn.api;

import com.lyle.vpn.server.DatabaseManager;
import com.lyle.vpn.server.VpnServer;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SystemStatusService {

    private final DatabaseManager databaseManager;
    private final VpnServer vpnServer;

    /*
     * Spring sees that this constructor requires a VpnServer.
     *
     * Because VpnServer is annotated with @Service,
     * Spring finds its managed VpnServer bean and passes
     * it into this constructor automatically.
     */
    public SystemStatusService(VpnServer vpnServer) {
        this.vpnServer = vpnServer;
        this.databaseManager = new DatabaseManager();
    }

    public Map<String, Object> getStatus() {

        Map<String, Object> status = new LinkedHashMap<>();

        status.put("api", "UP");

        status.put(
                "vpnServer",
                vpnServer.isRunning() ? "UP" : "DOWN"
        );

        status.put(
                "database",
                checkDatabase() ? "UP" : "DOWN"
        );

        status.put("vpnHost", "localhost");
        status.put("vpnPort", vpnServer.getPort());
        status.put("apiPort", 8080);

        return status;
    }

    private boolean checkDatabase() {

        try (Connection connection = databaseManager.connection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}