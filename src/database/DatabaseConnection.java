package database;

import java.sql.*;

public class DatabaseConnection {

    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sbsms?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static boolean initialized = false;

    public static synchronized Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-j to the project libraries.", e);
        }
        if (!initialized) {
            initializeDatabase();
        }
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    /**
     * Checks that MySQL is reachable and initializes the SBSMS schema if
     * needed.
     */
    public static synchronized void testConnection() throws SQLException {
        try (Connection ignored = getConnection()) {
            // Opening the connection is the test; getConnection initializes the schema first.
        }
    }

    private static void initializeDatabase() throws SQLException {
        try (Connection server = DriverManager.getConnection(SERVER_URL, USER, PASSWORD); Statement st = server.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS sbsms");
        }
        try (Connection con = DriverManager.getConnection(DB_URL, USER, PASSWORD); Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS residents ("
                    + "id_number VARCHAR(50) PRIMARY KEY, first_name VARCHAR(100) NOT NULL, "
                    + "middle_name VARCHAR(100) DEFAULT '', last_name VARCHAR(100) NOT NULL, "
                    + "gender VARCHAR(30) NOT NULL, age INT NOT NULL, address VARCHAR(255) NOT NULL, "
                    + "government_id_type VARCHAR(100) DEFAULT '', contact_number VARCHAR(30) DEFAULT '', disability VARCHAR(100) DEFAULT 'None', "
                    + "original_id_confirmed BOOLEAN DEFAULT FALSE, is_pregnant TINYINT(1) NOT NULL DEFAULT 0, status VARCHAR(30) NOT NULL DEFAULT 'Pending')");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "id_number VARCHAR(50) PRIMARY KEY, full_name VARCHAR(200) NOT NULL, "
                    + "password VARCHAR(255) NOT NULL, role VARCHAR(30) NOT NULL DEFAULT 'Resident', "
                    + "status VARCHAR(30) NOT NULL DEFAULT 'Pending')");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS password_reset_requests (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id VARCHAR(50) NOT NULL, reason VARCHAR(500) NOT NULL DEFAULT '', status ENUM('Pending','Approved','Rejected','Completed') NOT NULL DEFAULT 'Pending', requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, reviewed_at TIMESTAMP NULL DEFAULT NULL, reviewed_by VARCHAR(50) NULL, review_note VARCHAR(500) NOT NULL DEFAULT '', completed_at TIMESTAMP NULL DEFAULT NULL, INDEX idx_reset_request_status (status), INDEX idx_reset_request_user (user_id), CONSTRAINT fk_reset_request_user FOREIGN KEY (user_id) REFERENCES users(id_number) ON DELETE CASCADE) ENGINE=InnoDB");
            migrateLegacyResidents(con, st);
            ensureColumn(con, st, "residents", "middle_name", "VARCHAR(100) DEFAULT ''");
            ensureColumn(con, st, "residents", "government_id_type", "VARCHAR(100) DEFAULT ''");
            ensureColumn(con, st, "residents", "contact_number", "VARCHAR(30) DEFAULT ''");
            ensureColumn(con, st, "residents", "disability", "VARCHAR(100) DEFAULT 'None'");
            ensureColumn(con, st, "residents", "original_id_confirmed", "BOOLEAN DEFAULT FALSE");
            ensureColumn(con, st, "residents", "is_pregnant", "TINYINT(1) NOT NULL DEFAULT 0");
            ensureColumn(con, st, "residents", "status", "VARCHAR(30) NOT NULL DEFAULT 'Pending'");
            migrateLegacyUsers(con, st);
            st.executeUpdate("CREATE TABLE IF NOT EXISTS services ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, service_name VARCHAR(150) UNIQUE NOT NULL, "
                    + "category VARCHAR(100) NOT NULL, description VARCHAR(255) DEFAULT '', fee DECIMAL(10,2) DEFAULT 0, "
                    + "status VARCHAR(30) DEFAULT 'Active')");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS service_requests ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, resident_id VARCHAR(50) NOT NULL, service_id INT NOT NULL, "
                    + "request_date DATETIME DEFAULT CURRENT_TIMESTAMP, status VARCHAR(30) DEFAULT 'Pending', "
                    + "fee DECIMAL(10,2) DEFAULT 0, purpose VARCHAR(255) DEFAULT '', appointment_date DATE NULL, appointment_time VARCHAR(50) NULL, "
                    + "FOREIGN KEY (resident_id) REFERENCES residents(id_number) ON DELETE CASCADE, "
                    + "FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT)");
            ensureColumn(con, st, "service_requests", "appointment_date", "DATE NULL");
            ensureColumn(con, st, "service_requests", "appointment_time", "VARCHAR(50) NULL");
            seedServices(st);
            initialized = true;
        }
    }

    private static void migrateLegacyResidents(Connection con, Statement st) throws SQLException {
        if (!columnExists(con, "residents", "first_name") && columnExists(con, "residents", "full_name")) {
            st.executeUpdate("ALTER TABLE residents RENAME TO residents_legacy");
            st.executeUpdate("CREATE TABLE residents (id_number VARCHAR(50) PRIMARY KEY, first_name VARCHAR(100) NOT NULL, middle_name VARCHAR(100) DEFAULT '', last_name VARCHAR(100) NOT NULL, gender VARCHAR(30) NOT NULL, age INT NOT NULL, address VARCHAR(255) NOT NULL, government_id_type VARCHAR(100) DEFAULT '', contact_number VARCHAR(30) DEFAULT '', disability VARCHAR(100) DEFAULT 'None', original_id_confirmed BOOLEAN DEFAULT FALSE, status VARCHAR(30) NOT NULL DEFAULT 'Pending')");
            try (ResultSet r = st.executeQuery("SELECT id_number,full_name,gender,age,address,status FROM residents_legacy")) {
                while (r.next()) {
                    String full = r.getString("full_name");
                    String[] parts = full.trim().split("\\s+", 2);
                    String first = parts.length > 0 ? parts[0] : "Unknown";
                    String last = parts.length > 1 ? parts[1] : first;
                    try (PreparedStatement p = con.prepareStatement("INSERT INTO residents(id_number,first_name,last_name,gender,age,address,status) VALUES(?,?,?,?,?,?,?)")) {
                        p.setString(1, r.getString("id_number"));
                        p.setString(2, first);
                        p.setString(3, last);
                        p.setString(4, r.getString("gender"));
                        p.setInt(5, r.getInt("age"));
                        p.setString(6, r.getString("address"));
                        p.setString(7, r.getString("status"));
                        p.executeUpdate();
                    }
                }
            }
        }
    }

    private static void migrateLegacyUsers(Connection con, Statement st) throws SQLException {
        if (columnExists(con, "users", "id_number") && !columnExists(con, "users", "status")) {
            st.executeUpdate("ALTER TABLE users ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'Approved'");
        }
    }

    private static void ensureColumn(Connection con, Statement st, String table, String column, String definition) throws SQLException {
        if (!columnExists(con, table, column)) {
            st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private static boolean columnExists(Connection con, String table, String column) throws SQLException {
        String q = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='sbsms' AND table_name=? AND column_name=?";
        try (PreparedStatement p = con.prepareStatement(q)) {
            p.setString(1, table);
            p.setString(2, column);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1) > 0;
            }
        }
    }

    private static void seedServices(Statement st) throws SQLException {
        st.executeUpdate("INSERT IGNORE INTO services(service_name,category,description,fee,status) VALUES "
                + "('Barangay Clearance','Clearance','General barangay clearance',150,'Active'),"
                + "('Certificate of Indigency','Certificate','Proof of indigency',150,'Active'),"
                + "('Business Permit','Permit','Business permit operating within the barangay',150,'Active'),"
                + "('Certificate of Residency','Certificate','Proof of residency',150,'Active'),"
                + "('Certificate of Good Moral','Certificate','Certificate of good standing',150,'Active'),"
                + "('Solo Parent ID','Identification','Solo parent certification',150,'Active'),"
                + "('First-Time Jobseeker Certification','Certificate','Certification for first-time jobseekers',150,'Active')");
    }
}
