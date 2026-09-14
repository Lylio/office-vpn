package com.lyle.vpn.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lyle.vpn")
public class OfficeVpnApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfficeVpnApplication.class, args);
    }
}