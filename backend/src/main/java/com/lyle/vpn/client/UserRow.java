package com.lyle.vpn.client;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class UserRow {
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty role = new SimpleStringProperty();
    private final StringProperty enabled = new SimpleStringProperty();

    public UserRow(String username, String role, String enabled) {
        this.username.set(username);
        this.role.set(role);
        this.enabled.set(enabled);
    }

    public String username() { return username.get(); }
    public StringProperty usernameProperty() { return username; }
    public StringProperty roleProperty() { return role; }
    public StringProperty enabledProperty() { return enabled; }
}
