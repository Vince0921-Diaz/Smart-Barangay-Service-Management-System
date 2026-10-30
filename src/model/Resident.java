package model;

public class Resident {
    private String idNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private int age;
    private String address;
    private String governmentIdType;
    private String contactNumber;
    private String disability;
    private boolean originalIdConfirmed;
    private String status;

    public Resident() {}

    public Resident(String idNumber, String firstName, String lastName, String gender,
                     int age, String address, String status) {
        this(idNumber, firstName, "", lastName, gender, age, address, "", "", "None", false, status);
    }

    public Resident(String idNumber, String firstName, String middleName, String lastName,
                    String gender, int age, String address, String governmentIdType,
                    boolean originalIdConfirmed, String status) {
        this(idNumber, firstName, middleName, lastName, gender, age, address, governmentIdType, "", "None", originalIdConfirmed, status);
    }

    public Resident(String idNumber, String firstName, String middleName, String lastName,
                    String gender, int age, String address, String governmentIdType,
                    String contactNumber, String disability, boolean originalIdConfirmed, String status) {
        this.idNumber = idNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.governmentIdType = governmentIdType;
        this.contactNumber = contactNumber;
        this.disability = disability;
        this.originalIdConfirmed = originalIdConfirmed;
        this.status = status;
    }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFullName() {
        String middle = middleName == null || middleName.isBlank() ? "" : " " + middleName;
        return (firstName + middle + " " + lastName).trim();
    }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getGovernmentIdType() { return governmentIdType; }
    public void setGovernmentIdType(String governmentIdType) { this.governmentIdType = governmentIdType; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getDisability() { return disability; }
    public void setDisability(String disability) { this.disability = disability; }
    public boolean isOriginalIdConfirmed() { return originalIdConfirmed; }
    public void setOriginalIdConfirmed(boolean originalIdConfirmed) { this.originalIdConfirmed = originalIdConfirmed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
