package com.lyle.vpn.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public final class LoginController {
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        hostField.setText("localhost");
        portField.setText("5555");
        statusLabel.setText("");
    }

    @FXML
    private void connect() {
        String host = hostField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (host.isBlank() || username.isBlank() || password.isBlank()) {
            statusLabel.setText("Please complete all fields.");
            return;
        }

        try {
            int port = Integer.parseInt(portField.getText().trim());
            VpnClient client = new VpnClient(host, port);
            client.connect(username, password);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/lyle/vpn/client/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 760, 620);
            scene.getStylesheets().add(
                    getClass().getResource("/com/lyle/vpn/client/styles.css").toExternalForm());

            DashboardController controller = loader.getController();
            controller.initialise(client);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(700);
            stage.setMinHeight(560);
            stage.setTitle("Office VPN - " + client.username());
        } catch (Exception e) {
            statusLabel.setText("Connection failed: " + e.getMessage());
        }
    }
}
