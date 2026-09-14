package com.lyle.vpn.api;

public record ConnectRequest(
        String username,
        String password
) {
}