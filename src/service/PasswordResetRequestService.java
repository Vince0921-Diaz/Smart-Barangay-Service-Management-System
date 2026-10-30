package service;

import database.DatabaseConnection;
import java.sql.*;
import util.PasswordUtil;

/** Admin-assisted password recovery; no OTP or external delivery service is required. */
public class PasswordResetRequestService {
    public boolean submit(String accountId, String firstName, String middleName, String lastName, String reason) throws SQLException {
        if (blank(accountId) || blank(firstName) || blank(lastName)) throw new IllegalArgumentException("Enter your ID number, first name, and last name.");
        String id=accountId.trim();
        try(Connection c=DatabaseConnection.getConnection()) {
            String q="SELECT u.id_number FROM users u JOIN residents r ON r.id_number=u.id_number WHERE u.id_number=? AND LOWER(TRIM(r.first_name))=LOWER(TRIM(?)) AND LOWER(TRIM(COALESCE(r.middle_name,'')))=LOWER(TRIM(?)) AND LOWER(TRIM(r.last_name))=LOWER(TRIM(?)) AND LOWER(u.role)='resident' AND LOWER(u.status)='approved' AND LOWER(r.status)='approved'";
            try(PreparedStatement p=c.prepareStatement(q)){p.setString(1,id);p.setString(2,firstName.trim());p.setString(3,middleName==null?"":middleName.trim());p.setString(4,lastName.trim());try(ResultSet rs=p.executeQuery()){if(!rs.next()) return false;}}
            try(PreparedStatement p=c.prepareStatement("SELECT id FROM password_reset_requests WHERE user_id=? AND status='Pending' LIMIT 1")){p.setString(1,id);try(ResultSet rs=p.executeQuery()){if(rs.next()) return true;}}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO password_reset_requests(user_id,reason,status,requested_at) VALUES(?,?,'Pending',CURRENT_TIMESTAMP)")){p.setString(1,id);p.setString(2,reason==null?"":reason.trim());return p.executeUpdate()==1;}
        }
    }
    public boolean review(int requestId, String adminId, boolean approve, String note) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection()) { c.setAutoCommit(false); try {
            String q="UPDATE password_reset_requests SET status=?, reviewed_at=CURRENT_TIMESTAMP, reviewed_by=?, review_note=? WHERE id=? AND status='Pending'";
            try(PreparedStatement p=c.prepareStatement(q)){p.setString(1,approve?"Approved":"Rejected");p.setString(2,adminId);p.setString(3,note==null?"":note.trim());p.setInt(4,requestId);if(p.executeUpdate()!=1){c.rollback();return false;}}
            c.commit();return true;
        } catch(SQLException e){c.rollback();throw e;} finally {c.setAutoCommit(true);} }
    }
    public boolean resetApproved(int requestId, String accountId, String newPassword, String confirm) throws SQLException {
        if(newPassword==null || !newPassword.equals(confirm)) throw new IllegalArgumentException("Passwords do not match.");
        if(newPassword.length()<8 || !newPassword.matches(".*[A-Za-z].*") || !newPassword.matches(".*\\d.*") || !newPassword.matches(".*[^A-Za-z0-9].*")) throw new IllegalArgumentException("Password must be at least 8 characters and include a letter, a number, and a symbol.");
        try(Connection c=DatabaseConnection.getConnection()) { c.setAutoCommit(false); try {
            String q="SELECT user_id FROM password_reset_requests WHERE id=? AND status='Approved' FOR UPDATE"; String userId;
            try(PreparedStatement p=c.prepareStatement(q)){p.setInt(1,requestId);try(ResultSet rs=p.executeQuery()){if(!rs.next()){c.rollback();return false;}userId=rs.getString(1);}}
            if(!userId.equals(accountId)){c.rollback();return false;}
            try(PreparedStatement p=c.prepareStatement("UPDATE users SET password=? WHERE id_number=?")){p.setString(1,PasswordUtil.hash(newPassword));p.setString(2,userId);if(p.executeUpdate()!=1){c.rollback();return false;}}
            try(PreparedStatement p=c.prepareStatement("UPDATE password_reset_requests SET status='Completed', completed_at=CURRENT_TIMESTAMP WHERE id=? AND status='Approved'")){p.setInt(1,requestId);if(p.executeUpdate()!=1){c.rollback();return false;}}
            c.commit();return true;
        } catch(SQLException e){c.rollback();throw e;} finally {c.setAutoCommit(true);} }
    }

    /** Returns the latest reset-request status for the matching resident account. */
    public String getLatestStatus(String accountId, String firstName, String middleName, String lastName) throws SQLException {
        if (blank(accountId) || blank(firstName) || blank(lastName)) throw new IllegalArgumentException("Enter your ID number, first name, and last name.");
        String sql = "SELECT pr.status FROM password_reset_requests pr JOIN residents r ON r.id_number=pr.user_id WHERE pr.user_id=? AND LOWER(TRIM(r.first_name))=LOWER(TRIM(?)) AND LOWER(TRIM(COALESCE(r.middle_name,'')))=LOWER(TRIM(?)) AND LOWER(TRIM(r.last_name))=LOWER(TRIM(?)) ORDER BY pr.requested_at DESC, pr.id DESC LIMIT 1";
        try (Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1, accountId.trim()); p.setString(2, firstName.trim()); p.setString(3,middleName==null?"":middleName.trim()); p.setString(4,lastName.trim());
            try (ResultSet rs=p.executeQuery()) { return rs.next() ? rs.getString(1) : null; }
        }
    }

    /** Resets the password using the newest approved request, on the resident's side. */
    public boolean resetLatestApproved(String accountId, String firstName, String middleName, String lastName, String newPassword, String confirm) throws SQLException {
        if (blank(accountId) || blank(firstName) || blank(lastName)) throw new IllegalArgumentException("Enter your ID number, first name, and last name.");
        if (newPassword==null || !newPassword.equals(confirm)) throw new IllegalArgumentException("Passwords do not match.");
        if (newPassword.length()<8 || !newPassword.matches(".*[A-Za-z].*") || !newPassword.matches(".*\\d.*") || !newPassword.matches(".*[^A-Za-z0-9].*")) throw new IllegalArgumentException("Password must be at least 8 characters and include a letter, a number, and a symbol.");
        try (Connection c=DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String q="SELECT pr.id FROM password_reset_requests pr JOIN residents r ON r.id_number=pr.user_id WHERE pr.user_id=? AND LOWER(TRIM(r.first_name))=LOWER(TRIM(?)) AND LOWER(TRIM(COALESCE(r.middle_name,'')))=LOWER(TRIM(?)) AND LOWER(TRIM(r.last_name))=LOWER(TRIM(?)) AND pr.status='Approved' ORDER BY pr.requested_at DESC, pr.id DESC LIMIT 1 FOR UPDATE";
                int requestId;
                try (PreparedStatement p=c.prepareStatement(q)) { p.setString(1,accountId.trim()); p.setString(2,firstName.trim()); p.setString(3,middleName==null?"":middleName.trim()); p.setString(4,lastName.trim()); try(ResultSet rs=p.executeQuery()){ if(!rs.next()){c.rollback();return false;} requestId=rs.getInt(1); } }
                try (PreparedStatement p=c.prepareStatement("UPDATE users SET password=? WHERE id_number=?")) { p.setString(1,PasswordUtil.hash(newPassword)); p.setString(2,accountId.trim()); if(p.executeUpdate()!=1){c.rollback();return false;} }
                try (PreparedStatement p=c.prepareStatement("UPDATE password_reset_requests SET status='Completed', completed_at=CURRENT_TIMESTAMP WHERE id=? AND status='Approved'")) { p.setInt(1,requestId); if(p.executeUpdate()!=1){c.rollback();return false;} }
                c.commit(); return true;
            } catch(SQLException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }
    private boolean blank(String s){return s==null||s.trim().isEmpty();}
}
