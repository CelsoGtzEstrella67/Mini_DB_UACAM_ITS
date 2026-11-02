

package minidbeaver.database.local;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public class SQLiteTabla {

    private Connection connection;

    public SQLiteTabla(Connection connection) {
        this.connection = connection;
    }

    // Crear una tabla con las columnas recibidas
    public void createTable(String tableName, List<String> columns) {

        StringBuilder sql = new StringBuilder(
                "CREATE TABLE IF NOT EXISTS \"" +
                        tableName.replace("\"", "\"\"") + "\" ("
        );

        for (int i = 0; i < columns.size(); i++) {

            sql.append("\"")
                    .append(columns.get(i).replace("\"", "\"\""))
                    .append("\" TEXT");

            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }

        sql.append(")");

        try (Statement statement = connection.createStatement()) {

            statement.execute(sql.toString());
            System.out.println("Tabla creada: " + tableName);

        } catch (SQLException e) {
            System.out.println("Error al crear la tabla: " + e.getMessage());
        }
    }

    // Insertar registros
    public void insertRecord(String tableName, Map<String, Object> values) {

        StringBuilder columns = new StringBuilder();
        StringBuilder parameters = new StringBuilder();

        for (String column : values.keySet()) {
            if (columns.length() > 0) {
                columns.append(", ");
                parameters.append(", ");
            }

            columns.append("\"")
                    .append(column.replace("\"", "\"\""))
                    .append("\"");

            parameters.append("?");
        }

        String sql = "INSERT INTO \"" +
                tableName.replace("\"", "\"\"") +
                "\" (" + columns + ") VALUES (" + parameters + ")";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            int index = 1;

            for (Object value : values.values()) {
                statement.setObject(index++, value);
            }

            statement.executeUpdate();
            System.out.println("Registro agregado correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al insertar registro: " + e.getMessage());
        }
    }

    // Consultar registros
    public void showRecords(String tableName) {

        String sql = "SELECT * FROM \"" +
                tableName.replace("\"", "\"\"") + "\"";

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            int columns = result.getMetaData().getColumnCount();

            while (result.next()) {

                for (int i = 1; i <= columns; i++) {
                    System.out.print(
                            result.getMetaData().getColumnName(i) +
                                    ": " + result.getObject(i) + " | "
                    );
                }

                System.out.println();
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar registros: " + e.getMessage());
        }
    }

    // Modificar un registro
    public void updateRecord(
            String tableName,
            String column,
            Object newValue,
            String idColumn,
            Object idValue) {

        String sql = "UPDATE \"" +
                tableName.replace("\"", "\"\"") +
                "\" SET \"" + column.replace("\"", "\"\"") +
                "\" = ? WHERE \"" + idColumn.replace("\"", "\"\"") + "\" = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, newValue);
            statement.setObject(2, idValue);

            int rows = statement.executeUpdate();

            System.out.println(rows > 0
                    ? "Registro modificado correctamente."
                    : "No se encontró el registro.");

        } catch (SQLException e) {
            System.out.println("Error al modificar: " + e.getMessage());
        }
    }

    // Eliminar un registro
    public void deleteRecord(
            String tableName,
            String idColumn,
            Object idValue) {

        String sql = "DELETE FROM \"" +
                tableName.replace("\"", "\"\"") +
                "\" WHERE \"" + idColumn.replace("\"", "\"\"") + "\" = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, idValue);

            int rows = statement.executeUpdate();

            System.out.println(rows > 0
                    ? "Registro eliminado correctamente."
                    : "No se encontró el registro.");

        } catch (SQLException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }

    // Obtener los registros de una tabla
    public ResultSet getRecords(String tableName) throws SQLException {
        String sql = "SELECT * FROM \"" +
                tableName.replace("\"", "\"\"") + "\"";

        Statement statement = connection.createStatement();

        return statement.executeQuery(sql);
    }
}
