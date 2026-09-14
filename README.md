# Office VPN Simulator

### Tech Stack

| Component    | Tech                                                                                                                                                                                                             |
|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| App Link     | [offline]                                                                                                                                                     |                                                                                                                                                                                                                                                                                               
| Frontend     | ![JavaFX](https://img.shields.io/badge/%E2%80%8E%20-JavaFX-orange?logo=coffeescript&logoColor=white)                                                                                               |
| Backend      | ![Java](https://img.shields.io/badge/JAVA%20-JDK%2011-green?style=for-the-badge) ![Spring Boot](https://img.shields.io/badge/spring%20boot%202.1-white.svg?style=for-the-badge&logo=springboot&logoColor=6DB33F)|
| Database     | ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)                                                                                                     |
| Cloud        | [offline]                                                                                                        |
| Client Build | ![NPM](https://img.shields.io/badge/npm-white.svg?style=for-the-badge&logo=npm&logoColor=CB3837)               |                                                                                                                                                                                                                                                                                               
| Server Build | ![Maven](https://img.shields.io/badge/maven-white.svg?style=for-the-badge&logo=apache%20maven&logoColor=C71A36)                                                                                                  |
| API          | ![Swagger](https://img.shields.io/badge/swagger-85EA2D.svg?style=for-the-badge&logo=swagger&logoColor=FFF)                                                                                                       |
| Repo Size    | ![Repo Size](https://img.shields.io/github/repo-size/lylio/office-vpn?style=for-the-badge)                                                                                                                  |

A learning-focused Java 21 application demonstrating:

- Java TCP sockets
- ECDH key agreement
- AES-256-GCM authenticated encryption
- PBKDF2 password hashing
- MySQL persistence
- JavaFX GUI
- Multi-client server handling
- User roles and access control
- Connection logging

## Important security note

This is a **VPN simulator / secure application tunnel**, not a production operating-system VPN. It does not create a Windows/Linux virtual network adapter or route arbitrary IP packets.

Do not expose this project directly to the public Internet without further hardening, TLS/certificate authentication, firewall rules, rate limiting, key rotation, audit controls, and security testing.

## Requirements

- JDK 21+
- Maven 3.9+
- MySQL 8+
- Windows/Linux/macOS

## 1. Create the database

Run:

    mysql -u root -p < database/schema.sql

## 2. Configure the server

Set environment variables:

    DB_URL=jdbc:mysql://localhost:3306/office_vpn
    DB_USER=root
    DB_PASSWORD=your_mysql_password
    VPN_PORT=5555

On Windows PowerShell:

    $env:DB_URL="jdbc:mysql://localhost:3306/office_vpn"
    $env:DB_USER="root"
    $env:DB_PASSWORD="your_mysql_password"
    $env:VPN_PORT="5555"

## 3. Start the server

From the project directory:

    mvn -q exec:java -Dexec.mainClass=com.lyle.vpn.server.VpnServer

If your Maven installation does not have the exec plugin cached, use your IDE to run:

    com.lyle.vpn.server.VpnServer

On first startup, if no admin exists, the server prints a generated temporary admin password. Change it after logging in.

## 4. Start the JavaFX client

Run:

    mvn javafx:run

The GUI starts in client mode. Enter the server address, port, username and password.

## 5. Admin functions

An ADMIN account can:

- list users
- create users
- enable/disable users
- view recent connection logs

Normal users can connect and exchange encrypted messages with the server.

## Architecture

    JavaFX Client
          |
          | TCP
          v
    ECDH handshake
          |
          v
    AES-256-GCM encrypted frames
          |
          v
    Multi-client Java server
          |
       +--+--+
       |     |
     MySQL  Audit logs

## Cryptography

The server and client perform an ephemeral ECDH exchange using Java's standard security APIs. The resulting shared secret is expanded with SHA-256 to obtain an AES-256 key.

Application frames are encrypted with AES/GCM/NoPadding and a fresh 12-byte nonce for every encrypted frame.

Passwords use PBKDF2WithHmacSHA256 with a per-user random salt.

These choices are for learning. A production VPN should normally use a mature VPN protocol and implementation rather than inventing a protocol.

## Project structure

    office-vpn/
    ├── pom.xml
    ├── README.md
    ├── database/schema.sql
    └── src/main/
        ├── java/com/lyle/vpn/
        │   ├── common/
        │   ├── server/
        │   └── client/
        └── resources/com/lyle/vpn/client/
            ├── login.fxml
            ├── dashboard.fxml
            └── styles.css
