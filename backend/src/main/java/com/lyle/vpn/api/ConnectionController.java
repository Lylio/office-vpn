package com.lyle.vpn.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConnectionController {

    private final VpnSessionService vpnSessionService;

    public ConnectionController(VpnSessionService vpnSessionService) {
        this.vpnSessionService = vpnSessionService;
    }

    @PostMapping("/connect")
    public ResponseEntity<Map<String, Object>> connect(
            @RequestBody ConnectRequest request
    ) {
        try {

            if (request.username() == null ||
                    request.username().isBlank()) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "connected", false,
                                "message", "Username is required"
                        )
                );
            }

            if (request.password() == null ||
                    request.password().isBlank()) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "connected", false,
                                "message", "Password is required"
                        )
                );
            }

            vpnSessionService.connect(
                    request.username(),
                    request.password()
            );

            Map<String, Object> response = new HashMap<>();

            response.put(
                    "connected",
                    vpnSessionService.isConnected()
            );

            response.put(
                    "username",
                    vpnSessionService.getUsername()
            );

            response.put(
                    "role",
                    vpnSessionService.getRole()
            );

            response.put(
                    "message",
                    "Encrypted VPN session established"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "connected", false,
                                    "message",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "VPN connection failed"
                            )
                    );
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnect() {

        vpnSessionService.disconnect();

        return ResponseEntity.ok(
                Map.of(
                        "connected", false,
                        "message",
                        "VPN session disconnected"
                )
        );
    }

    @GetMapping("/connection")
    public Map<String, Object> connectionStatus() {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "connected",
                vpnSessionService.isConnected()
        );

        response.put(
                "username",
                vpnSessionService.getUsername()
        );

        response.put(
                "role",
                vpnSessionService.getRole()
        );

        return response;
    }
}