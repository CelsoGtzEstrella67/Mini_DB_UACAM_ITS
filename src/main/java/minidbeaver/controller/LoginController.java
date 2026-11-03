package minidbeaver.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import minidbeaver.App;
import connections.ConexionSSH;

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
            return;
        }

        messageLabel.setText("Intentando establecer conexión SSH...");

        try {

            String host = hostField.getText();
            String user = usernameField.getText();
            String pass = passwordField.getText();

            ConexionSSH ssh = App.getSshConnection();

            boolean sshExitoso = ssh.iniciarTunel(host, user, pass);

            if (!sshExitoso) {
                messageLabel.setText("Error: Servidor caído o credenciales SSH incorrectas.");
                return;
            }

            messageLabel.setText("¡Túnel SSH listo! Conectando Base de Datos...");

            connections.GestionSQLite db = App.getDatabase();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/minidbeaver/views/tablas.fxml")
            );
            Parent root = loader.load();

            TablasController tablasController = loader.getController();
            tablasController.setSQLiteDatabase(db);

            Stage stage = (Stage) hostField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("MiniDBeaver - Gestor de Tablas");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error crítico al procesar el inicio de sesión.");
        }
    }
}
