
package minidbeaver.database.local;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase {

    private Connection connection;

    public void connect() {

        try {
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:MiniDBeaver.db"
            );

            System.out.println("Base de datos SQLite conectada.");

        } catch (SQLException e) {
            System.out.println("Error al conectar con SQLite: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void disconnect() {

        try {
            if (connection != null) {
                connection.close();
                System.out.println("Conexión SQLite cerrada.");
            }

        } catch (SQLException e) {
            System.out.println("Error al cerrar SQLite: " + e.getMessage());
        }
    }
}
