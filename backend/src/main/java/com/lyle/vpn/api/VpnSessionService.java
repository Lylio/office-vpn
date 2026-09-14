package com.lyle.vpn.api;

import com.lyle.vpn.client.VpnClient;
import org.springframework.stereotype.Service;

@Service
public class VpnSessionService {

    private static final String VPN_HOST = "localhost";
    private static final int VPN_PORT = 5555;

    private VpnClient vpnClient;

    public synchronized void connect(String username, String password) throws Exception {
        if (isConnected()) {
            return;
        }

        VpnClient newClient = new VpnClient(VPN_HOST, VPN_PORT);

        try {
            newClient.connect(username, password);
            this.vpnClient = newClient;
        } catch (Exception e) {
            newClient.close();
            throw e;
        }
    }

    public synchronized void disconnect() {
        if (vpnClient != null) {
            vpnClient.close();
            vpnClient = null;
        }
    }

    public synchronized boolean isConnected() {
        return vpnClient != null;
    }

    public synchronized String getUsername() {
        return vpnClient != null
                ? vpnClient.username()
                : null;
    }

    public synchronized String getRole() {
        return vpnClient != null
                ? vpnClient.role()
                : null;
    }
}