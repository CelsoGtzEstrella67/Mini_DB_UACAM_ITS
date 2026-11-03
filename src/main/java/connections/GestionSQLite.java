package connections;

import java.sql.*;

public class GestionSQLite {
    private Connection connection;
    private final String URL_SQLITE = "jdbc:sqlite:copia_local.db";

    public void conectar() throws Exception {
        if (connection == null || connection.isClosed()) {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(URL_SQLITE);
            System.out.println("Servicio SQLite: Base de datos local lista.");
        }
    }

    //Lee dinámicamente cualquier tabla de MariaDB y la clona en SQLite.
    public void clonarTablaDesdeMariaDB(Connection mariaDbConn, String nombreTabla) throws Exception {
        conectar(); // Asegurar que SQLite está abierto

        String querySelect = "SELECT * FROM " + nombreTabla;
        try (Statement stmtMaria = mariaDbConn.createStatement();
             ResultSet rsMaria = stmtMaria.executeQuery(querySelect)) {
            ResultSetMetaData metaData = rsMaria.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Construir consultas dinámicas
            StringBuilder createTableSql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + nombreTabla + " (");
            StringBuilder insertSql = new StringBuilder("INSERT INTO " + nombreTabla + " (");
            StringBuilder placeholders = new StringBuilder();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                String columnType = metaData.getColumnTypeName(i);

                String sqliteType = "TEXT";
                if (columnType.contains("INT")) sqliteType = "INTEGER";
                else if (columnType.contains("DOUBLE") || columnType.contains("FLOAT") || columnType.contains("DECIMAL")) sqliteType = "REAL";

                createTableSql.append(columnName).append(" ").append(sqliteType);
                insertSql.append(columnName);
                placeholders.append("?");

                if (i < columnCount) {
                    createTableSql.append(", ");
                    insertSql.append(", ");
                    placeholders.append(", ");
                }
            }
            createTableSql.append(")");
            insertSql.append(") VALUES (").append(placeholders).append(")");

            // Crear la tabla local
            try (Statement stmtSqlite = connection.createStatement()) {
                stmtSqlite.execute(createTableSql.toString());
            }

            // Insertar datos masivamente
            try (PreparedStatement pstmtSqlite = connection.prepareStatement(insertSql.toString())) {
                int filas = 0;
                while (rsMaria.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        pstmtSqlite.setObject(i, rsMaria.getObject(i));
                    }
                    pstmtSqlite.addBatch();
                    filas++;
                }
                if (filas > 0) {
                    pstmtSqlite.executeBatch();
                }
                System.out.println("Servicio SQLite: Clonación exitosa de " + filas + " filas.");
            }

        }
    }

    //Crud
    public boolean ejecutarInstruccionCRUD(String sql, Object... parametros) {
        try {
            conectar();
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < parametros.length; i++) {
                    pstmt.setObject(i + 1, parametros[i]);
                }
                pstmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Servicio SQLite: Error al ejecutar operación CRUD.");
            e.printStackTrace();
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void cerrarConexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Servicio SQLite: Conexión cerrada.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
