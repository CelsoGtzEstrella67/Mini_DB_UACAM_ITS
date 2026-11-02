


package minidbeaver.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyObjectWrapper;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import minidbeaver.database.local.SQLiteDatabase;
import minidbeaver.database.local.SQLiteTabla;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Optional;

public class TablasController {

    @FXML
    private ListView<String> tablesList;

    @FXML
    private TableView<Map<String, Object>> recordsTable;

    @FXML
    private Label messageLabel;

    private SQLiteDatabase database;

    private SQLiteTabla sqliteTabla;

    public void initialize() {

        tablesList.getItems().addAll(
                "prueba_flexible"
        );

    }

    @FXML
    private void onDownload() {

        String selectedTable = tablesList.getSelectionModel().getSelectedItem();

        if (selectedTable == null) {
            messageLabel.setText("Selecciona una tabla primero.");
            return;
        }

        if (sqliteTabla == null) {
            messageLabel.setText("No hay conexión con SQLite.");
            return;
        }

        try {
            ResultSet result = sqliteTabla.getRecords(selectedTable);

            displayRecords(result);

            result.getStatement().close();

            messageLabel.setText("Registros cargados: " + selectedTable);

        } catch (SQLException e) {
            messageLabel.setText("Error al cargar los registros: " + e.getMessage());
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

        // Solicitar el valor de cada columna
        for (TableColumn<Map<String, Object>, ?> column : recordsTable.getColumns()) {

            String columnName = column.getText();

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Agregar registro");
            dialog.setHeaderText("Nuevo valor para: " + columnName);
            dialog.setContentText(columnName + ":");

            Optional<String> result = dialog.showAndWait();

            if (result.isEmpty()) {
                return;
            }

            values.put(columnName, result.get());
        }

        sqliteTabla.insertRecord(selectedTable, values);

        refreshRecords(selectedTable);

        messageLabel.setText("Registro agregado.");
    }


    @FXML
    private void onUpdate() {

        String selectedTable = tablesList.getSelectionModel().getSelectedItem();

        Map<String, Object> selectedRecord =
                recordsTable.getSelectionModel().getSelectedItem();

        if (selectedTable == null || selectedRecord == null) {
            messageLabel.setText("Selecciona un registro primero.");
            return;
        }

        if (!selectedRecord.containsKey("id")) {
            messageLabel.setText("No se encontró la columna id.");
            return;
        }

        // Obtener las columnas que se pueden modificar
        ArrayList<String> columns = new ArrayList<>();

        for (TableColumn<Map<String, Object>, ?> column : recordsTable.getColumns()) {
            if (!column.getText().equalsIgnoreCase("id")) {
                columns.add(column.getText());
            }
        }

        if (columns.isEmpty()) {
            messageLabel.setText("No hay columnas para modificar.");
            return;
        }

        // Elegir la columna
        ChoiceDialog<String> choiceDialog =
                new ChoiceDialog<>(columns.get(0), columns);

        choiceDialog.setTitle("Modificar registro");
        choiceDialog.setHeaderText("Selecciona la columna que deseas modificar");

        Optional<String> selectedColumn = choiceDialog.showAndWait();

        if (selectedColumn.isEmpty()) {
            return;
        }

        String columnName = selectedColumn.get();

        // Solicitar el nuevo valor
        TextInputDialog dialog = new TextInputDialog(
                String.valueOf(selectedRecord.get(columnName))
        );

        dialog.setTitle("Modificar registro");
        dialog.setHeaderText("Nuevo valor para: " + columnName);
        dialog.setContentText("Valor:");

        Optional<String> newValue = dialog.showAndWait();

        if (newValue.isEmpty()) {
            return;
        }

        sqliteTabla.updateRecord(
                selectedTable,
                columnName,
                newValue.get(),
                "id",
                selectedRecord.get("id")
        );

        refreshRecords(selectedTable);

        messageLabel.setText("Registro modificado.");
    }


    @FXML
    private void onDelete() {

        String selectedTable = tablesList.getSelectionModel().getSelectedItem();

        Map<String, Object> selectedRecord =
                recordsTable.getSelectionModel().getSelectedItem();

        if (selectedTable == null || selectedRecord == null) {
            messageLabel.setText("Selecciona un registro primero.");
            return;
        }

        if (!selectedRecord.containsKey("id")) {
            messageLabel.setText("No se encontró la columna id.");
            return;
        }

        // Confirmar eliminación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Eliminar registro");
        alert.setHeaderText("¿Deseas eliminar este registro?");
        alert.setContentText("ID: " + selectedRecord.get("id"));

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            sqliteTabla.deleteRecord(
                    selectedTable,
                    "id",
                    selectedRecord.get("id")
            );

            refreshRecords(selectedTable);

            messageLabel.setText("Registro eliminado.");
        }
    }


    // Mostrar los registros en la tabla visual
    private void displayRecords(ResultSet result) throws SQLException {

        recordsTable.getColumns().clear();

        ResultSetMetaData metadata = result.getMetaData();
        int numberOfColumns = metadata.getColumnCount();

        // Crear las columnas
        for (int i = 1; i <= numberOfColumns; i++) {

            String columnName = metadata.getColumnName(i);

            TableColumn<Map<String, Object>, Object> column =
                    new TableColumn<>(columnName);

            column.setCellValueFactory(data ->
                    new ReadOnlyObjectWrapper<>(
                            data.getValue().get(columnName)
                    )
            );

            recordsTable.getColumns().add(column);
        }

        // Obtener los registros
        ObservableList<Map<String, Object>> rows =
                FXCollections.observableArrayList();

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

    public void setSQLiteDatabase(SQLiteDatabase database) {
        this.database = database;
        this.sqliteTabla = new SQLiteTabla(database.getConnection());
    }


    private void refreshRecords(String tableName) {

        try {
            ResultSet result = sqliteTabla.getRecords(tableName);

            displayRecords(result);

            result.getStatement().close();

        } catch (SQLException e) {
            messageLabel.setText(
                    "Error al actualizar los registros: " + e.getMessage()
            );
        }
    }

}
