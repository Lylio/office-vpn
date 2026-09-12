package com.lyle.vpn.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class VpnClientApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                VpnClientApp.class.getResource("/com/lyle/vpn/client/login.fxml"));
        Scene scene = new Scene(loader.load(), 480, 520);
        scene.getStylesheets().add(
                VpnClientApp.class.getResource("/com/lyle/vpn/client/styles.css").toExternalForm());
        stage.setTitle("Office VPN Simulator");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
