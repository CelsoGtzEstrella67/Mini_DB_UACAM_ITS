package minidbeaver.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import minidbeaver.App;
import connections.GestionSQLite; // <-- Tu modelo
import connections.GestionMariaDB;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TablasController {

    @FXML
    private ListView<String> tablesList;

    @FXML
    private TableView<Map<String, Object>> recordsTable;

    @FXML
    private Label messageLabel;

    private GestionSQLite sqliteDb; // <-- Tu manejador de SQLite

    public void initialize() {
        tablesList.getItems().clear();
        // Al iniciar la pantalla, recuperamos de App la instancia ya abierta de tu SQLite
        this.sqliteDb = App.getDatabase();
    }

    @FXML
    private void onConnect() {
        try {
            TextInputDialog dbDialog = new TextInputDialog("its5a");
            dbDialog.setTitle("Conectar a MariaDB");
            dbDialog.setHeaderText("Configuración de Base de Datos");
            dbDialog.setContentText("Nombre de la Base de Datos:");
            Optional<String> dbResult = dbDialog.showAndWait();
            if (dbResult.isEmpty()) return;

            TextInputDialog userDialog = new TextInputDialog("root");
            userDialog.setTitle("Conectar a MariaDB");
            userDialog.setHeaderText("Credenciales de Acceso");
            userDialog.setContentText("Usuario de la BD:");
            Optional<String> userResult = userDialog.showAndWait();
            if (userResult.isEmpty()) return;

            TextInputDialog passDialog = new TextInputDialog();
            passDialog.setTitle("Conectar a MariaDB");
            passDialog.setHeaderText("Credenciales de Acceso");
            passDialog.setContentText("Contraseña de la BD:");
            Optional<String> passResult = passDialog.showAndWait();
            if (passResult.isEmpty()) return;

            messageLabel.setText("Conectando a MariaDB a través del túnel...");

            GestionMariaDB mariaDB = App.getMariaDB();
            if (mariaDB.conectar(dbResult.get(), userResult.get(), passResult.get())) {
                messageLabel.setText("¡Conectado a MariaDB! Cargando listado de tablas...");
                List<String> tablasRemotas = mariaDB.obtenerListadoTablas();
                tablesList.getItems().clear();
                tablesList.getItems().addAll(tablasRemotas);
            } else {
                messageLabel.setText("Error: Credenciales de MariaDB incorrectas.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error al conectar MariaDB: " + e.getMessage());
        }
    }

    /**
     * BOTÓN DESCARGAR: Clona la tabla seleccionada de MariaDB a SQLite y la muestra
     */
    @FXML
    private void onDownload() {
        String selectedTable = tablesList.getSelectionModel().getSelectedItem();
        if (selectedTable == null) {
            messageLabel.setText("Selecciona una tabla de la lista primero.");
            return;
        }

        try {
            Connection mariaConn = App.getMariaDB().getConnection();
            if (mariaConn == null || mariaConn.isClosed()) {
                messageLabel.setText("Error: No hay conexión activa a MariaDB.");
                return;
            }

            messageLabel.setText("Clonando tabla '" + selectedTable + "' a SQLite local...");

            // 1. Ejecuta tu grandiosa función del Backend
            sqliteDb.clonarTablaDesdeMariaDB(mariaConn, selectedTable);

            // 2. Refresca y muestra visualmente los registros ahora locales
            refreshRecords(selectedTable);
            messageLabel.setText("Tabla '" + selectedTable + "' descargada y lista localmente.");

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error al clonar tabla: " + e.getMessage());
        }
    }

    @FXML
    private void onAdd() {
        String selectedTable = tablesList.getSelectionModel().getSelectedItem();
        if (selectedTable == null) {
            messageLabel.setText("Selecciona una tabla primero.");
            return;
        }

        Map<String, Object> values = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder("INSERT INTO " + selectedTable + " (");
        StringBuilder placeholders = new StringBuilder();
        ArrayList<Object> params = new ArrayList<>();

        int count = 0;
        for (TableColumn<Map<String, Object>, ?> column : recordsTable.getColumns()) {
            String columnName = column.getText();
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Agregar registro");
            dialog.setHeaderText("Nuevo valor para: " + columnName);
            dialog.setContentText(columnName + ":");
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) return;

            sql.append(columnName);
            placeholders.append("?");
            params.add(result.get());

            if (++count < recordsTable.getColumns().size()) {
                sql.append(", ");
                placeholders.append(", ");
            }
        }
        sql.append(") VALUES (").append(placeholders).append(")");

        // Ejecuta usando tu método CRUD genérico
        if (sqliteDb.ejecutarInstruccionCRUD(sql.toString(), params.toArray())) {
            refreshRecords(selectedTable);
            messageLabel.setText("Registro agregado localmente.");
        } else {
            messageLabel.setText("Error al insertar el registro.");
        }
    }

    @FXML
    private void onUpdate() {
        String selectedTable = tablesList.getSelectionModel().getSelectedItem();
        Map<String, Object> selectedRecord = recordsTable.getSelectionModel().getSelectedItem();

        if (selectedTable == null || selectedRecord == null) {
            messageLabel.setText("Selecciona un registro de la tabla primero.");
            return;
        }

        // 🔍 BUSCADOR INTELIGENTE DE LA LLAVE ID
        String idKey = null;
        for (String key : selectedRecord.keySet()) {
            String cleanKey = key.trim();
            if (cleanKey.equalsIgnoreCase("id") || cleanKey.toLowerCase().endsWith("_id") || cleanKey.toLowerCase().startsWith("id_")) {
                idKey = key; // Encontramos la columna exacta (ej: "id", "ID", "id_usuario", "Id")
                break;
            }
        }

        if (idKey == null) {
            messageLabel.setText("La tabla debe tener una columna 'id' para actualizar.");
            return;
        }

        ArrayList<String> columns = new ArrayList<>();
        for (TableColumn<Map<String, Object>, ?> column : recordsTable.getColumns()) {
            if (!column.getText().equalsIgnoreCase(idKey)) {
                columns.add(column.getText());
            }
        }
        if (columns.isEmpty()) return;

        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(columns.get(0), columns);
        choiceDialog.setTitle("Modificar registro");
        choiceDialog.setHeaderText("Selecciona la columna a modificar");
        Optional<String> selectedColumn = choiceDialog.showAndWait();
        if (selectedColumn.isEmpty()) return;

        String columnName = selectedColumn.get();
        TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedRecord.get(columnName)));
        dialog.setTitle("Modificar registro");
        dialog.setHeaderText("Nuevo valor para: " + columnName);
        dialog.setContentText("Valor:");
        Optional<String> newValue = dialog.showAndWait();
        if (newValue.isEmpty()) return;

        String sql = "UPDATE " + selectedTable + " SET " + columnName + " = ? WHERE " + idKey + " = ?";

        if (sqliteDb.ejecutarInstruccionCRUD(sql, newValue.get(), selectedRecord.get(idKey))) {
            refreshRecords(selectedTable);
            messageLabel.setText("Registro modificado en SQLite.");
        } else {
            messageLabel.setText("Error al ejecutar la actualización en SQLite.");
        }
    }

    @FXML
    private void onDelete() {
        String selectedTable = tablesList.getSelectionModel().getSelectedItem();
        Map<String, Object> selectedRecord = recordsTable.getSelectionModel().getSelectedItem();

        if (selectedTable == null || selectedRecord == null) {
            messageLabel.setText("Selecciona un registro primero.");
            return;
        }

        // 🔍 BUSCADOR INTELIGENTE DE LA LLAVE ID
        String idKey = null;
        for (String key : selectedRecord.keySet()) {
            String cleanKey = key.trim();
            if (cleanKey.equalsIgnoreCase("id") || cleanKey.toLowerCase().endsWith("_id") || cleanKey.toLowerCase().startsWith("id_")) {
                idKey = key;
                break;
            }
        }

        if (idKey == null) {
            messageLabel.setText("No se encontró una columna llave id.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar registro");
        alert.setHeaderText("¿Deseas eliminar este registro local?");
        alert.setContentText("Columna Llave: " + idKey + " | Valor ID: " + selectedRecord.get(idKey));
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM " + selectedTable + " WHERE " + idKey + " = ?";
            if (sqliteDb.ejecutarInstruccionCRUD(sql, selectedRecord.get(idKey))) {
                refreshRecords(selectedTable);
                messageLabel.setText("Registro eliminado localmente.");
            } else {
                messageLabel.setText("Error al eliminar el registro de SQLite.");
            }
        }
    }


    private void refreshRecords(String tableName) {
        try {
            Connection conn = sqliteDb.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
                displayRecords(rs);
            }
        } catch (Exception e) {
            messageLabel.setText("Error al refrescar la tabla local: " + e.getMessage());
        }
    }
    private void displayRecords(ResultSet result) throws Exception {
        recordsTable.getColumns().clear();
        ResultSetMetaData metadata = result.getMetaData();
        int numberOfColumns = metadata.getColumnCount();
        for (int i = 1; i <= numberOfColumns; i++) {
            String columnName = metadata.getColumnName(i);
            TableColumn<Map<String, Object>, Object> column = new TableColumn<>(columnName);
            column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().get(columnName)));
            recordsTable.getColumns().add(column);
        }
        ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList();
        while (result.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= numberOfColumns; i++) {
                String columnName = metadata.getColumnName(i);
                row.put(columnName, result.getObject(i));
            }
            rows.add(row);
        }
        recordsTable.setItems(rows);
    }
    // Ya no necesitas recibir bases de datos viejas desde afuera gracias al inicio global en App
    public void setSQLiteDatabase(Object dummy) {}
}
