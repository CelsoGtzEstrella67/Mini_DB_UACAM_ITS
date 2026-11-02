
package minidbeaver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import minidbeaver.database.local.SQLiteDatabase;
import minidbeaver.controller.TablasController;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Conectar con SQLite
        SQLiteDatabase database = new SQLiteDatabase();
        database.connect();

        // Cargar la pantalla
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/minidbeaver/views/tablas.fxml")
        );

        Scene scene = new Scene(loader.load());

        // Enviar la conexión al controlador
        TablasController controller = loader.getController();
        controller.setSQLiteDatabase(database);

        stage.setTitle("MiniDBeaver");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Cerrar la conexión al cerrar la aplicación
    }

    public static void main(String[] args) {
        launch(args);
    }
}
