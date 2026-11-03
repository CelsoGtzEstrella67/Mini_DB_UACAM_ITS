package minidbeaver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import connections.GestionSQLite;
import connections.GestionMariaDB;
import connections.ConexionSSH;

public class App extends Application {

    private static GestionSQLite database;
    private static ConexionSSH sshConnection;
    private static GestionMariaDB mariaDB;

    @Override
    public void start(Stage stage) throws Exception {
        database = new GestionSQLite();
        sshConnection = new ConexionSSH();
        mariaDB = new GestionMariaDB();

        database.conectar();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/minidbeaver/views/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage.setTitle("MiniDBeaver - Iniciar Sesión");
        stage.setScene(scene);
        stage.show();
    }

    public static GestionSQLite getDatabase() { return database; } // <-- Retorna GestionSQLite
    public static ConexionSSH getSshConnection() { return sshConnection; }
    public static GestionMariaDB getMariaDB() { return mariaDB; }

    @Override
    public void stop() {
        System.out.println("Cerrando aplicación...");
        if (mariaDB != null) { mariaDB.cerrarConexion(); }
        if (database != null) { database.cerrarConexion(); }
        if (sshConnection != null) { sshConnection.cerrarTunel(); }
    } //Asegura cerrar conexiones de forma segura

    public static void main(String[] args) { launch(args); }
}
