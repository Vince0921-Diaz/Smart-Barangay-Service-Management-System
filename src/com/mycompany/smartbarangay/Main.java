package com.mycompany.smartbarangay;

import database.DatabaseConnection;
import gui.Home;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Keep the default Swing look and feel if the system one is unavailable.
            }

            try {
                DatabaseConnection.testConnection();
                new Home().setVisible(true);
            } catch (SQLException ex) {
                String message = "SBSMS could not connect to MySQL.\n\n"
                        + "Check that XAMPP MySQL is running and that the sbsms database is accessible.\n"
                        + "Connection: localhost:3306 / database sbsms / user root\n\n"
                        + "Details: " + rootMessage(ex) + "\n\n"
                        + "If the message mentions the MySQL JDBC Driver, add "
                        + "mysql-connector-j-9.4.0.jar to the project's lib folder, then Clean and Build.";
                JOptionPane.showMessageDialog(null, message, "Database Connection Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }
}
