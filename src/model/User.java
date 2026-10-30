package model;
public class User {
    private String idNumber, fullName, password, role, status;
    public User(){}
    public User(String id,String name,String password,String role,String status){this.idNumber=id;this.fullName=name;this.password=password;this.role=role;this.status=status;}
    public String getIdNumber(){return idNumber;} public void setIdNumber(String v){idNumber=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public String getRole(){return role;} public void setRole(String v){role=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
