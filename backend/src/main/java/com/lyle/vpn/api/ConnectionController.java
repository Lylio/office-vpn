package com.lyle.vpn.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConnectionController {

    private final VpnSessionService vpnSessionService;

    public ConnectionController(VpnSessionService vpnSessionService) {
        this.vpnSessionService = vpnSessionService;
    }

    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> connect() {
        try {
            vpnSessionService.connect();

            return ResponseEntity.ok(
                    Map.of(
                            "connected", true,
                            "message", "VPN session connected"
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "connected", false,
                            "message", e.getMessage()
                    )
            );
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnect() {
        try {
            vpnSessionService.disconnect();

            return ResponseEntity.ok(
                    Map.of(
                            "connected", false,
                            "message", "VPN session disconnected"
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "connected", vpnSessionService.isConnected(),
                            "message", e.getMessage()
                    )
            );
        }
    }

    @GetMapping("/connection")
    public Map<String, Object> connectionStatus() {
        return Map.of(
                "connected",
                vpnSessionService.isConnected()
        );
    }
}