package model;

public class Appointment {

    private int id;
    private String residentId;
    private String residentName;
    private String serviceName;
    private String purpose;
    private String date;
    private String time;
    private String status;

    public Appointment() {
    }

    public Appointment(int id, String residentId, String residentName, String serviceName, String purpose, String date, String time, String status) {
        this.id = id;
        this.residentId = residentId;
        this.residentName = residentName;
        this.serviceName = serviceName;
        this.purpose = purpose;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int v) {
        id = v;
    }

    public String getResidentId() {
        return residentId;
    }

    public void setResidentId(String v) {
        residentId = v;
    }

    public String getResidentName() {
        return residentName;
    }

    public void setResidentName(String v) {
        residentName = v;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String v) {
        serviceName = v;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String v) {
        purpose = v;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String v) {
        date = v;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String v) {
        time = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }
}
