package dao;

import database.DatabaseConnection;
import model.ServiceRequest;
import java.sql.*;
import java.util.*;

public class ServiceRequestDAO {
    private double calculateFee(Connection c, String residentId, int serviceId, double configuredFee) throws SQLException {
        String serviceSql = "SELECT service_name FROM services WHERE id=?";
        String serviceName;
        try (PreparedStatement p = c.prepareStatement(serviceSql)) {
            p.setInt(1, serviceId);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Service not found.");
                serviceName = r.getString(1);
            }
        }
        if (!"First-Time Jobseeker Certification".equalsIgnoreCase(serviceName)) return configuredFee;
        String countSql = "SELECT COUNT(*) FROM service_requests WHERE resident_id=? AND service_id=?";
        try (PreparedStatement p = c.prepareStatement(countSql)) {
            p.setString(1, residentId);
            p.setInt(2, serviceId);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getLong(1) == 0 ? 0.0 : configuredFee;
            }
        }
    }

    public int add(String residentId, int serviceId, double fee, String purpose) {
        String sql = "INSERT INTO service_requests(resident_id,service_id,fee,purpose,status) VALUES(?,?,?,?, 'Pending')";
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            double actualFee = calculateFee(c, residentId, serviceId, fee);
            try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1, residentId);
                p.setInt(2, serviceId);
                p.setDouble(3, actualFee);
                p.setString(4, purpose);
                p.executeUpdate();
                try (ResultSet r = p.getGeneratedKeys()) {
                    if (!r.next()) { c.rollback(); return -1; }
                    int id = r.getInt(1);
                    c.commit();
                    return id;
                }
            } catch (SQLException e) { c.rollback(); throw e; }
        } catch (SQLException e) { e.printStackTrace(); return -1; }
    }

    public int addWithAppointment(String residentId, int serviceId, double fee, String purpose, String date, String time) {
        String slotSql = "SELECT COUNT(*) FROM service_requests WHERE appointment_date=? AND appointment_time=? AND LOWER(status) IN ('pending','approved')";
        String requestSql = "INSERT INTO service_requests(resident_id,service_id,fee,purpose,status,appointment_date,appointment_time) VALUES(?,?,?,?, 'Pending',?,?)";
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement slot = c.prepareStatement(slotSql)) {
                slot.setDate(1, java.sql.Date.valueOf(date));
                slot.setTime(2, java.sql.Time.valueOf(normalizeTime(time)));
                try (ResultSet r = slot.executeQuery()) {
                    r.next();
                    if (r.getInt(1) > 0) { c.rollback(); return -1; }
                }
            }
            double actualFee = calculateFee(c, residentId, serviceId, fee);
            int requestId;
            try (PreparedStatement p = c.prepareStatement(requestSql, Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1, residentId);
                p.setInt(2, serviceId);
                p.setDouble(3, actualFee);
                p.setString(4, purpose);
                p.setDate(5, java.sql.Date.valueOf(date));
                p.setString(6, normalizeTime(time));
                if (p.executeUpdate() == 0) { c.rollback(); return -1; }
                try (ResultSet keys = p.getGeneratedKeys()) {
                    if (!keys.next()) { c.rollback(); return -1; }
                    requestId = keys.getInt(1);
                }
            }
            c.commit();
            return requestId;
        } catch (SQLException e) { e.printStackTrace(); return -1; }
    }

    private String normalizeTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Appointment time is required.");
        }
        String v = value.trim().toUpperCase(Locale.US);
        for (String pattern : new String[]{"h:mm a", "hh:mm a"}) {
            try {
                java.time.LocalTime t = java.time.LocalTime.parse(v,
                    java.time.format.DateTimeFormatter.ofPattern(pattern, Locale.US));
                return t.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US));
            } catch (java.time.format.DateTimeParseException ignored) { }
        }
        try {
            java.time.LocalTime t = java.time.LocalTime.parse(v);
            return t.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US));
        } catch (java.time.format.DateTimeParseException ignored) { }
        throw new IllegalArgumentException("Invalid appointment time: " + value);
    }

    public List<ServiceRequest> getAll() {
        return query("SELECT sr.*, CONCAT(r.first_name,' ',r.last_name) resident_name,s.service_name FROM service_requests sr JOIN residents r ON r.id_number=sr.resident_id JOIN services s ON s.id=sr.service_id ORDER BY sr.id DESC", null);
    }
    public List<ServiceRequest> getByResident(String residentId) {
        return query("SELECT sr.*, CONCAT(r.first_name,' ',r.last_name) resident_name,s.service_name FROM service_requests sr JOIN residents r ON r.id_number=sr.resident_id JOIN services s ON s.id=sr.service_id WHERE sr.resident_id=? ORDER BY sr.id DESC", residentId);
    }
    public boolean setStatus(int id, String status) {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("UPDATE service_requests SET status=? WHERE id=? AND status NOT IN ('Cancelled','Completed')")) {
                p.setString(1, status); p.setInt(2, id);
                if (p.executeUpdate() == 0) { c.rollback(); return false; }
            }
            c.commit(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean cancelPending(int id, String residentId) {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("UPDATE service_requests SET status='Cancelled' WHERE id=? AND resident_id=? AND status='Pending'")) {
                p.setInt(1, id); p.setString(2, residentId);
                if (p.executeUpdate() == 0) { c.rollback(); return false; }
            }
            c.commit(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public long countByService(int serviceId) {
        String sql = "SELECT COUNT(*) FROM service_requests WHERE service_id=?";
        try (Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) { p.setInt(1,serviceId); try(ResultSet r=p.executeQuery()){r.next();return r.getLong(1);} }
        catch(SQLException e){e.printStackTrace();return 0;}
    }
    public long countByMonth(int month) {
        String sql="SELECT COUNT(*) FROM service_requests WHERE MONTH(request_date)=? AND YEAR(request_date)=YEAR(CURDATE())";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,month);try(ResultSet r=p.executeQuery()){r.next();return r.getLong(1);}}
        catch(SQLException e){e.printStackTrace();return 0;}
    }
    public long count(String status) {
        String sql=status==null?"SELECT COUNT(*) FROM service_requests":"SELECT COUNT(*) FROM service_requests WHERE status=?";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){if(status!=null)p.setString(1,status);try(ResultSet r=p.executeQuery()){r.next();return r.getLong(1);}}
        catch(SQLException e){return 0;}
    }
    private List<ServiceRequest> query(String sql,String arg) {
        List<ServiceRequest> list=new ArrayList<>();
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            if(arg!=null)p.setString(1,arg);
            try(ResultSet r=p.executeQuery()){while(r.next())list.add(new ServiceRequest(r.getInt("id"),r.getString("resident_id"),r.getString("resident_name"),r.getInt("service_id"),r.getString("service_name"),r.getString("request_date"),r.getString("status"),r.getDouble("fee"),r.getString("purpose")));}
        }catch(SQLException e){e.printStackTrace();}
        return list;
    }
}
