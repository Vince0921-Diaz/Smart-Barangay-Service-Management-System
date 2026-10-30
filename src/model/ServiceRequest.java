package model;

public class ServiceRequest {
    private int id;
    private String residentId;
    private String residentName;
    private int serviceId;
    private String serviceName;
    private String dateTime;
    private String status;
    private double fee;
    private String purpose;

    public ServiceRequest() {}
    public ServiceRequest(int id,String residentId,String residentName,int serviceId,String serviceName,String dateTime,String status,double fee,String purpose){
        this.id=id;this.residentId=residentId;this.residentName=residentName;this.serviceId=serviceId;this.serviceName=serviceName;this.dateTime=dateTime;this.status=status;this.fee=fee;this.purpose=purpose;
    }
    public int getId(){return id;} public void setId(int v){id=v;}
    public String getResidentId(){return residentId;} public void setResidentId(String v){residentId=v;}
    public String getResidentName(){return residentName;} public void setResidentName(String v){residentName=v;}
    public int getServiceId(){return serviceId;} public void setServiceId(int v){serviceId=v;}
    public String getServiceName(){return serviceName;} public void setServiceName(String v){serviceName=v;}
    public String getDateTime(){return dateTime;} public void setDateTime(String v){dateTime=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public double getFee(){return fee;} public void setFee(double v){fee=v;}
    public String getPurpose(){return purpose;} public void setPurpose(String v){purpose=v;}
}
