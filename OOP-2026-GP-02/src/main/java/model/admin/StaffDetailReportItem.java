package main.java.model.admin;

public class StaffDetailReportItem {
    private int userId;
    private String name;
    private String role;
    private String designation;
    private String departmentName;
    private String email;
    private String phone;
    private String status;

    public StaffDetailReportItem(int userId, String name, String role, String designation,
                               String departmentName, String email, String phone, String status) {
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.designation = designation;
        this.departmentName = departmentName;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getDesignation() { return designation; }
    public String getDepartmentName() { return departmentName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
}
