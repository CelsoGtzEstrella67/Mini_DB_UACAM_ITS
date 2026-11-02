package minidbeaver.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

public class LoginController {

    @FXML
    private TextField hostField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void onConnect() {

        if (hostField.getText().isEmpty() ||
                usernameField.getText().isEmpty() ||
                passwordField.getText().isEmpty()) {

            messageLabel.setText("Completa todos los campos.");

        } else {

            messageLabel.setText("Datos ingresados correctamente.");
        }
    }
}