package entity.model;

public class Customer {
    private int customerId;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private int creditScore;

    // Default Constructor
    public Customer() {}

    // Parameterized Constructor
    public Customer(int customerId, String name, String email, String phoneNumber, String address, int creditScore) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.creditScore = creditScore;
    }

    // Getters and Setters
    public int getCustomerId() {
        return customerId;
    }
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public int getCreditScore() {
        return creditScore;
    }
    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    // toString Method
    @Override
    public String toString() {
        return "Customer [ID=" + customerId + ", Name=" + name + ", Email=" + email + ", Phone=" + phoneNumber
                + ", Address=" + address + ", Credit Score=" + creditScore + "]";
    }
}
