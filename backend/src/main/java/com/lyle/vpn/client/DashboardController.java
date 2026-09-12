package com.lyle.vpn.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class DashboardController {
    @FXML private Label identityLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea messagesArea;
    @FXML private TextField messageField;
    @FXML private TabPane tabs;
    @FXML private TableView<UserRow> usersTable;
    @FXML private TableColumn<UserRow,String> userColumn;
    @FXML private TableColumn<UserRow,String> roleColumn;
    @FXML private TableColumn<UserRow,String> enabledColumn;
    @FXML private TextArea logsArea;
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<String> newRoleBox;

    private VpnClient client;

    public void initialise(VpnClient client) {
        this.client = client;
        identityLabel.setText(client.username() + " (" + client.role() + ")");
        statusLabel.setText("● Connected");
        newRoleBox.getItems().setAll("USER", "ADMIN");
        newRoleBox.setValue("USER");

        boolean admin = "ADMIN".equals(client.role());
        tabs.getTabs().get(1).setDisable(!admin);
        if (admin) {
            userColumn.setCellValueFactory(d -> d.getValue().usernameProperty());
            roleColumn.setCellValueFactory(d -> d.getValue().roleProperty());
            enabledColumn.setCellValueFactory(d -> d.getValue().enabledProperty());
            refreshUsers();
            refreshLogs();
        }
    }

    @FXML
    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;
        try {
            String response = client.sendMessage(text);
            messagesArea.appendText("You: " + text + "\n");
            messagesArea.appendText(response.replace("MESSAGE|SERVER|", "Server: ") + "\n\n");
            messageField.clear();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void refreshUsers() {
        try {
            String response = client.listUsers();
            if (!response.startsWith("RESPONSE|USERS|")) return;
            usersTable.getItems().clear();
            String body = response.substring("RESPONSE|USERS|".length());
            for (String row : body.split(";")) {
                if (row.isBlank()) continue;
                String[] p = row.split(",", 3);
                if (p.length == 3) usersTable.getItems().add(new UserRow(p[0], p[1], p[2]));
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void createUser() {
        try {
            String username = newUsernameField.getText().trim();
            String password = newPasswordField.getText();
            String role = newRoleBox.getValue();
            if (username.isBlank() || password.isBlank()) {
                showError("Username and password are required.");
                return;
            }
            String response = client.createUser(username, password, role);
            messagesArea.appendText(response + "\n");
            newUsernameField.clear();
            newPasswordField.clear();
            refreshUsers();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void disableSelected() {
        UserRow selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            client.setEnabled(selected.username(), false);
            refreshUsers();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void enableSelected() {
        UserRow selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            client.setEnabled(selected.username(), true);
            refreshUsers();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void refreshLogs() {
        try {
            String response = client.logs();
            if (!response.startsWith("RESPONSE|LOGS|")) return;
            String encoded = response.substring("RESPONSE|LOGS|".length());
            logsArea.setText(new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8));
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void disconnect() {
        client.close();
        Platform.exit();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message == null ? "Unknown error" : message,
                ButtonType.OK).showAndWait();
    }
}
