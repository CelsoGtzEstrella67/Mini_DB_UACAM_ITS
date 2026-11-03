module minidbeaver {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.mariadb.jdbc;
    requires org.xerial.sqlitejdbc;

    requires com.jcraft.jsch;

    exports minidbeaver;
    opens minidbeaver.controller to javafx.fxml;
}