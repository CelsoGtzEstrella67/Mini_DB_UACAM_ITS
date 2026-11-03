package connections;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class ConexionSSH {
    private Session session;
    private final int PUERTO_LOCAL = 3307; // Puerto puente intermedio en tu máquina
    private final int PUERTO_REMOTO_BD = 3306; // Puerto estándar de MariaDB en el servidor

    public boolean iniciarTunel(String host, String user, String password) {
        try {
            System.out.println("Servicio SSH: Intentando abrir canal seguro con " + host + "...");
            JSch jsch = new JSch();
            // Puerto 22 es el estándar para SSH
            session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            // Evitar validación estricta de firmas digitales (necesario en entornos escolares)
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            // Intentar conectar al servidor remoto
            session.connect();

            // Configurar el Port Forwarding (Redirección de puertos)
            session.setPortForwardingL(PUERTO_LOCAL, "localhost", PUERTO_REMOTO_BD);
            System.out.println("Servicio SSH: ¡Túnel establecido con éxito en el puerto local " + PUERTO_LOCAL + "!");
            return true;

        } catch (Exception e) {
            System.err.println("Servicio SSH: Error de autenticación o conexión.");
            e.printStackTrace();
            cerrarTunel(); // Limpiar en caso de fallo parcial
            return false;
        }
    }

    public boolean estaConectado() {
        return session != null && session.isConnected();
    }


    public void cerrarTunel() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("Servicio SSH: Canal seguro cerrado correctamente.");
        }
    }

}