package model;

public class Service {
    private int id;
    private String name;
    private String category;
    private String description;
    private double fee;
    private String status;

    public Service() {}
    public Service(int id, String name, String category, String description, double fee, String status) {
        this.id=id; this.name=name; this.category=category; this.description=description; this.fee=fee; this.status=status;
    }
    public int getId(){return id;} public void setId(int v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public double getFee(){return fee;} public void setFee(double v){fee=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
