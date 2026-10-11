package main.java.model.admin;

import main.java.model.User;

public class Admin extends User {
    private String phone;
    private String address;

    public Admin() {
        super();
    }

    public Admin(int id, String name, String username, String role, String email, String status, String phone, String address) {
        super(id, name, username, role, email, status);
        this.phone = phone;
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
