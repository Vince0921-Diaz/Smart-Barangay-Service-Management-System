package dao;

import database.DatabaseConnection;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Database operations for staff accounts stored in the shared users table. */
public class StaffDAO {
    public boolean createStaff(String id, String fullName, String password) {
        String sql = "INSERT INTO users(id_number,full_name,password,role,status) VALUES(?,?,?,'Staff','Approved')";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, id.trim()); p.setString(2, fullName.trim()); p.setString(3, password);
            return p.executeUpdate() == 1;
        } catch (SQLException e) { System.err.println("Unable to create staff account: " + e.getMessage()); return false; }
    }

    public List<User> getAllStaff() {
        List<User> staff = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role='Staff' ORDER BY full_name";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) staff.add(new User(r.getString("id_number"), r.getString("full_name"), r.getString("password"), r.getString("role"), r.getString("status")));
        } catch (SQLException e) { System.err.println("Unable to load staff accounts: " + e.getMessage()); }
        return staff;
    }

    public boolean setStatus(String id, String status) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE users SET status=? WHERE id_number=? AND role='Staff'")) {
            p.setString(1, status); p.setString(2, id); return p.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Unable to update staff account: " + e.getMessage()); return false; }
    }

    public boolean deleteStaff(String id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM users WHERE id_number=? AND role='Staff'")) {
            p.setString(1, id); return p.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Unable to delete staff account: " + e.getMessage()); return false; }
    }
}
