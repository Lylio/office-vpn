package com.lyle.vpn.server;

import com.lyle.vpn.common.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseManager {

    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager() {
        this.url = env("DB_URL", "jdbc:postgresql://localhost:5432/vpn_app");
        this.user = env("DB_USER", "vpn_user");
        this.password = env("DB_PASSWORD", "vpn_password");
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    public Connection connection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public User authenticate(String username, String plainPassword) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, enabled FROM users WHERE username=?";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                if (!rs.getBoolean("enabled")) return null;
                if (!PasswordUtils.verify(plainPassword, rs.getString("password_hash"))) return null;
                return new User(rs.getLong("id"), rs.getString("username"),
                        rs.getString("role"), true);
            }
        }
    }

    public boolean hasAdmin() throws SQLException {
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE role='ADMIN'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public void createUser(String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO users(username,password_hash,role,enabled) VALUES(?,?,?,TRUE)";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtils.hash(password));
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    public void setEnabled(String username, boolean enabled) throws SQLException {
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET enabled=? WHERE username=?")) {
            ps.setBoolean(1, enabled);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }

    public List<User> listUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id,username,role,enabled FROM users ORDER BY username";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new User(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBoolean(4)));
            }
        }
        return users;
    }

    public long logConnection(String username, String ip, String status) throws SQLException {
        String sql = "INSERT INTO connection_logs(username,ip_address,status) VALUES(?,?,?)";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, ip);
            ps.setString(3, status);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void closeConnectionLog(long id) throws SQLException {
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE connection_logs SET disconnected_at=CURRENT_TIMESTAMP,status='DISCONNECTED' WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public List<String> recentLogs() throws SQLException {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT id,username,ip_address,connected_at,disconnected_at,status " +
                     "FROM connection_logs ORDER BY id DESC LIMIT 100";
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(String.format("%d | %s | %s | %s | %s | %s",
                        rs.getLong(1), rs.getString(2), rs.getString(3),
                        rs.getTimestamp(4), rs.getTimestamp(5), rs.getString(6)));
            }
        }
        return rows;
    }

    public record User(long id, String username, String role, boolean enabled) {}
}
