CREATE DATABASE IF NOT EXISTS office_vpn
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE office_vpn;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(500) NOT NULL,
    role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS connection_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NULL,
    ip_address VARCHAR(100) NOT NULL,
    connected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    disconnected_at TIMESTAMP NULL,
    status VARCHAR(30) NOT NULL
);

-- The application creates the first admin account if none exists.
-- Do not store a plaintext password in this SQL file.
