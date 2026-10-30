package dao;
import database.DatabaseConnection; import model.Service; import java.sql.*; import java.util.*;
public class ServiceDAO {
 public List<Service> getAll(){return query("SELECT * FROM services ORDER BY service_name");}
 public List<Service> getActive(){return query("SELECT * FROM services WHERE status='Active' ORDER BY service_name");}
 public Service findByName(String name){try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM services WHERE service_name=?")){p.setString(1,name.trim());try(ResultSet r=p.executeQuery()){if(r.next())return map(r);}}catch(SQLException e){e.printStackTrace();}return null;}
 public boolean add(String name,String category,String desc,double fee,String status){try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO services(service_name,category,description,fee,status) VALUES(?,?,?,?,?)")){p.setString(1,name);p.setString(2,category);p.setString(3,desc);p.setDouble(4,fee);p.setString(5,status);return p.executeUpdate()>0;}catch(SQLException e){e.printStackTrace();return false;}}
 public boolean update(int id,String name,String category,String desc,double fee,String status){try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE services SET service_name=?,category=?,description=?,fee=?,status=? WHERE id=?")){p.setString(1,name);p.setString(2,category);p.setString(3,desc);p.setDouble(4,fee);p.setString(5,status);p.setInt(6,id);return p.executeUpdate()>0;}catch(SQLException e){e.printStackTrace();return false;}}
 public boolean delete(int id){try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM services WHERE id=?")){p.setInt(1,id);return p.executeUpdate()>0;}catch(SQLException e){e.printStackTrace();return false;}}
 private List<Service> query(String sql){List<Service> l=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())l.add(map(r));}catch(SQLException e){e.printStackTrace();}return l;}
 private Service map(ResultSet r)throws SQLException{return new Service(r.getInt("id"),r.getString("service_name"),r.getString("category"),r.getString("description"),r.getDouble("fee"),r.getString("status"));}
}
