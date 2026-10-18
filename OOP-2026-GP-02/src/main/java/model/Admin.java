package main.java.model;

/**
 * Model class representing an Administrator in TecFAMS.
 * Extends the base User class.
 */
public class Admin extends User {
    private String adminId;

    public Admin() {
        super();
        this.role = "Admin";
    }

    public Admin(String adminId, String username, String name, String email,
                 String phone, String department) {
        super(adminId, username, "", name, email, phone, "Admin", department);
        this.adminId = adminId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
        this.userId = adminId;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "adminId='" + adminId + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
