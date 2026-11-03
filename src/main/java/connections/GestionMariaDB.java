package connections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GestionMariaDB {
    private Connection connection;
    private String nombreBaseDatos;

    public boolean conectar(String nombreBD, String dbUser, String dbPassword) {
        try {
            this.nombreBaseDatos = nombreBD;
            // Forzamos "localhost:3307" porque es donde tu máquina está escuchando el túnel SSH,
            // pero concatenamos la Base de Datos dinámica que escribió el usuario.
            String urlConexion = "jdbc:mariadb://localhost:3307/" + nombreBD;

            System.out.println("Servicio MariaDB: Conectando a " + urlConexion + "...");
            Class.forName("org.mariadb.jdbc.Driver");

            connection = DriverManager.getConnection(urlConexion, dbUser, dbPassword);
            System.out.println("Servicio MariaDB: ¡Conexión exitosa!");
            return true;

        } catch (Exception e) {
            System.err.println("Servicio MariaDB: Error de conexión JDBC.");
            e.printStackTrace();
            return false;
        }
    }

    //Listado de tablas
    public List<String> obtenerListadoTablas() throws Exception {
        List<String> tablas = new ArrayList<>();

        if (connection == null || connection.isClosed()) {
            throw new IllegalStateException("Servicio MariaDB: No hay una conexión activa.");
        }

        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(nombreBaseDatos, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tablas.add(rs.getString("TABLE_NAME"));
            }
        }
        return tablas;
    }

    public Connection getConnection() {
        return connection;
    }

    public void cerrarConexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Servicio MariaDB: Conexión cerrada.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}