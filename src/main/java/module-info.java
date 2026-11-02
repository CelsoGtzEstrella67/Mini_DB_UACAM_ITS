module minidbeaver {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports minidbeaver;
    opens minidbeaver.controller to javafx.fxml;
}