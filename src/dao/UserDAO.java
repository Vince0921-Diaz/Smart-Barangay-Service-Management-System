package dao;
import database.DatabaseConnection;
import model.User;
import java.sql.*;
import java.util.*;
import util.PasswordUtil;
public class UserDAO {
    private static volatile String lastAuthenticationError;
    public static String getLastAuthenticationError() { return lastAuthenticationError; }
    public boolean createUser(String id,String name,String password){ return createUser(id,name,password,"Pending"); }
    public boolean createUser(String id,String name,String password,String status){String sql="INSERT INTO users(id_number,full_name,password,role,status) VALUES(?,?,?,'Resident',?)";try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,id);p.setString(2,name);p.setString(3,PasswordUtil.hash(password));p.setString(4,status);return p.executeUpdate()>0;}catch(SQLException e){e.printStackTrace();return false;}}
    public User authenticate(String id,String password){
        lastAuthenticationError = null;
        String sql="SELECT * FROM users WHERE id_number=?";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,id);
            try(ResultSet r=p.executeQuery()){
                if(r.next()){
                    String stored=r.getString("password");
                    if(!PasswordUtil.verify(password,stored)) return null;
                    if(!PasswordUtil.isHashed(stored)){
                        try(PreparedStatement upgrade=c.prepareStatement("UPDATE users SET password=? WHERE id_number=?")){
                            upgrade.setString(1,PasswordUtil.hash(password));
                            upgrade.setString(2,id);
                            upgrade.executeUpdate();
                        }
                    }
                    return new User(r.getString("id_number"),r.getString("full_name"),
                            stored,r.getString("role"),r.getString("status"));
                }
            }
        }catch(SQLException e){e.printStackTrace(); lastAuthenticationError = e.getMessage();}
        return null;
    }
    public boolean approve(String id){return setStatus(id,"Approved");}
    public boolean reject(String id){return setStatus(id,"Rejected");}
    public boolean setStatus(String id,String status){try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET status=? WHERE id_number=?")){p.setString(1,status);p.setString(2,id);int changed=p.executeUpdate();if(changed>0)return true;return id!=null&&id.startsWith("REG-")&&"Approved".equalsIgnoreCase(status);}catch(SQLException e){e.printStackTrace();return false;}}
    public List<User> getResidents(){return getByRole("Resident");}
    public List<User> getByRole(String role){List<User> out=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM users WHERE role=? ORDER BY id_number")){p.setString(1,role);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new User(r.getString("id_number"),r.getString("full_name"),r.getString("password"),r.getString("role"),r.getString("status")));}}catch(SQLException e){e.printStackTrace();}return out;}
    public boolean resetPassword(String id, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE id_number=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, PasswordUtil.hash(newPassword)); p.setString(2, id);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean addStaff(String id, String name, String password) {
        String sql = "INSERT INTO users(id_number,full_name,password,role,status) VALUES(?,?,?,'Staff','Approved')";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, id); p.setString(2, name); p.setString(3, PasswordUtil.hash(password));
            return p.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean deleteStaff(String id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM users WHERE id_number=? AND role='Staff'")) {
            p.setString(1, id); return p.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    public boolean resetPasswordVerified(String id, String fullName, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE id_number=? AND LOWER(TRIM(full_name))=LOWER(TRIM(?))";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, PasswordUtil.hash(newPassword)); p.setString(2, id); p.setString(3, fullName);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
