package dao;

import database.DatabaseConnection;
import model.Resident;
import java.sql.*;
import java.util.*;

public class ResidentDAO {
    /** Generates monotonically increasing IDs; deleting a resident never reuses an old ID. */
    public String generateResidentId() {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS resident_id_sequence (sequence_name VARCHAR(40) PRIMARY KEY, last_value BIGINT NOT NULL)");
                st.executeUpdate("INSERT IGNORE INTO resident_id_sequence(sequence_name,last_value) SELECT 'resident', COALESCE(MAX(CAST(id_number AS UNSIGNED)),0) FROM residents WHERE id_number REGEXP '^[0-9]+$'");
            }
            long next;
            try (PreparedStatement p = c.prepareStatement("SELECT last_value FROM resident_id_sequence WHERE sequence_name='resident' FOR UPDATE"); ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Resident ID sequence could not be initialized.");
                next = r.getLong(1) + 1;
            }
            try (PreparedStatement p = c.prepareStatement("UPDATE resident_id_sequence SET last_value=? WHERE sequence_name='resident'")) { p.setLong(1, next); p.executeUpdate(); }
            c.commit();
            return String.format(java.util.Locale.ROOT, "%03d", next);
        } catch (SQLException e) { e.printStackTrace(); return null; }
    }

    /** Finds a pre-existing barangay record using several fields, not the name alone. */
    public Resident findMatchingApprovedRecord(Resident candidate) {
        String sql = "SELECT * FROM residents WHERE LOWER(TRIM(first_name))=LOWER(TRIM(?)) " +
            "AND LOWER(TRIM(COALESCE(middle_name,'')))=LOWER(TRIM(?)) " +
            "AND LOWER(TRIM(last_name))=LOWER(TRIM(?)) AND age=? " +
            "AND LOWER(TRIM(address))=LOWER(TRIM(?)) AND REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(contact_number,''),' ',''),'-',''),'+',''),'(',''),')','')=REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(?,' ',''),'-',''),'+',''),'(',''),')','') " +
            "AND LOWER(TRIM(COALESCE(government_id_type,'')))=LOWER(TRIM(?)) " +
            "AND LOWER(TRIM(gender))=LOWER(TRIM(?)) AND LOWER(TRIM(COALESCE(disability,'None')))=LOWER(TRIM(?)) " +
            "AND LOWER(status)='approved' LIMIT 2";
        try (Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,candidate.getFirstName()); p.setString(2,candidate.getMiddleName()==null?"":candidate.getMiddleName());
            p.setString(3,candidate.getLastName()); p.setInt(4,candidate.getAge()); p.setString(5,candidate.getAddress());
            p.setString(6,candidate.getContactNumber()); p.setString(7,candidate.getGovernmentIdType());
            p.setString(8,candidate.getGender()); p.setString(9,candidate.getDisability());
            try(ResultSet r=p.executeQuery()) { if(!r.next()) return null; Resident found=new Resident(r.getString("id_number"),r.getString("first_name"),r.getString("middle_name"),r.getString("last_name"),r.getString("gender"),r.getInt("age"),r.getString("address"),r.getString("government_id_type"),r.getString("contact_number"),r.getString("disability"),r.getBoolean("original_id_confirmed"),r.getString("status")); if(r.next()) return null; return found; }
        } catch(SQLException e) { e.printStackTrace(); return null; }
    }

    public boolean hasUserAccount(String id) {
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement("SELECT 1 FROM users WHERE id_number=? LIMIT 1")) {
            p.setString(1,id); try(ResultSet r=p.executeQuery()){return r.next();}
        } catch(SQLException e){e.printStackTrace();return true;}
    }

    public String addResident(Resident x){
        String id=x.getIdNumber(); if(id==null||id.isBlank()) id=generateResidentId();
        String sql="INSERT INTO residents(id_number,first_name,middle_name,last_name,gender,age,address,government_id_type,contact_number,disability,original_id_confirmed,status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,id);p.setString(2,x.getFirstName());p.setString(3,x.getMiddleName());p.setString(4,x.getLastName());p.setString(5,x.getGender());p.setInt(6,x.getAge());p.setString(7,x.getAddress());p.setString(8,x.getGovernmentIdType());p.setString(9,x.getContactNumber());p.setString(10,x.getDisability());p.setBoolean(11,x.isOriginalIdConfirmed());p.setString(12,x.getStatus());
            if(p.executeUpdate()>0){x.setIdNumber(id);return id;}
        }catch(SQLException e){e.printStackTrace();} return null;
    }
    public List<Resident> getAllResidents(){return query("SELECT * FROM residents ORDER BY CAST(id_number AS UNSIGNED)");}
    public Resident findResident(String id){List<Resident> l=query("SELECT * FROM residents WHERE id_number=?",id);return l.isEmpty()?null:l.get(0);}
    public List<Resident> search(String q){String x="%"+q+"%"; return query("SELECT * FROM residents WHERE id_number LIKE ? OR first_name LIKE ? OR middle_name LIKE ? OR last_name LIKE ? ORDER BY CAST(id_number AS UNSIGNED)",x,x,x,x);}
    public boolean updateResident(Resident x){String sql="UPDATE residents SET first_name=?,middle_name=?,last_name=?,gender=?,age=?,address=?,government_id_type=?,contact_number=?,disability=?,original_id_confirmed=?,status=? WHERE id_number=?";try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,x.getFirstName());p.setString(2,x.getMiddleName());p.setString(3,x.getLastName());p.setString(4,x.getGender());p.setInt(5,x.getAge());p.setString(6,x.getAddress());p.setString(7,x.getGovernmentIdType());p.setString(8,x.getContactNumber());p.setString(9,x.getDisability());p.setBoolean(10,x.isOriginalIdConfirmed());p.setString(11,x.getStatus());p.setString(12,x.getIdNumber());return p.executeUpdate()>0;}catch(SQLException e){e.printStackTrace();return false;}}
    public boolean updateStatus(String id,String status){
        if (id == null) return false;
        if ("Approved".equalsIgnoreCase(status) && id.startsWith("REG-")) return approvePendingRegistration(id);
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE residents SET status=? WHERE id_number=?")){p.setString(1,status);p.setString(2,id);return p.executeUpdate()>0;}catch(SQLException e){e.printStackTrace();return false;}
    }

    private boolean approvePendingRegistration(String temporaryId) {
        try(Connection c=DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            String newId;
            try(PreparedStatement q=c.prepareStatement("SELECT id_number FROM residents WHERE id_number=? FOR UPDATE")){q.setString(1,temporaryId);try(ResultSet r=q.executeQuery()){if(!r.next()){c.rollback();return false;}}}
            // Allocate sequence in the same transaction.
            try(Statement st=c.createStatement()) { st.executeUpdate("CREATE TABLE IF NOT EXISTS resident_id_sequence (sequence_name VARCHAR(40) PRIMARY KEY, last_value BIGINT NOT NULL)"); st.executeUpdate("INSERT IGNORE INTO resident_id_sequence(sequence_name,last_value) SELECT 'resident', COALESCE(MAX(CAST(id_number AS UNSIGNED)),0) FROM residents WHERE id_number REGEXP '^[0-9]+$'"); }
            long next;
            try(PreparedStatement p=c.prepareStatement("SELECT last_value FROM resident_id_sequence WHERE sequence_name='resident' FOR UPDATE");ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("ID sequence unavailable");next=r.getLong(1)+1;}
            newId=String.format(java.util.Locale.ROOT,"%03d",next);
            try(PreparedStatement p=c.prepareStatement("UPDATE resident_id_sequence SET last_value=? WHERE sequence_name='resident'")){p.setLong(1,next);p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("UPDATE users SET id_number=?, status='Approved' WHERE id_number=? AND role='Resident'")){p.setString(1,newId);p.setString(2,temporaryId);p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("UPDATE residents SET id_number=?, status='Approved' WHERE id_number=?")){p.setString(1,newId);p.setString(2,temporaryId);if(p.executeUpdate()!=1)throw new SQLException("Pending resident record could not be approved");}
            c.commit(); return true;
        } catch(SQLException e){e.printStackTrace();return false;}
    }

    public boolean deleteResident(String id){
        try(Connection c=DatabaseConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement("DELETE FROM residents WHERE id_number=?")){p.setString(1,id);int deleted=p.executeUpdate();if(deleted==0){c.rollback();return false;}}
            try(PreparedStatement p=c.prepareStatement("DELETE FROM users WHERE id_number=? AND role='Resident'")){p.setString(1,id);p.executeUpdate();}
            c.commit(); return true;
        }catch(SQLException e){e.printStackTrace();return false;}
    }
    private List<Resident> query(String sql,String... args){List<Resident> out=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)p.setString(i+1,args[i]);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new Resident(r.getString("id_number"),r.getString("first_name"),r.getString("middle_name"),r.getString("last_name"),r.getString("gender"),r.getInt("age"),r.getString("address"),r.getString("government_id_type"),r.getString("contact_number"),r.getString("disability"),r.getBoolean("original_id_confirmed"),r.getString("status")));}}catch(SQLException e){e.printStackTrace();}return out;}

    public Resident findById(String residentId) {
        if (residentId == null || residentId.trim().isEmpty()) return null;
        return findResident(residentId.trim());
    }
}
