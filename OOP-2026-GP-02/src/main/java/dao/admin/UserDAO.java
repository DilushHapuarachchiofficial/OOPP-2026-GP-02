package main.java.dao.admin;

import main.java.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = 
            "SELECT " +
            "    u.user_id, " +
            "    u.username, " +
            "    u.role, " +
            "    COALESCE(a.name, l.lecturer_name, s.full_name, t.officer_name) as name, " +
            "    COALESCE(a.email, l.email, s.email, t.email) as email, " +
            "    u.status " +
            "FROM users u " +
            "LEFT JOIN admins a ON u.user_id = a.admin_id AND u.role = 'Admin' " +
            "LEFT JOIN lecturers l ON u.user_id = l.lecturer_id AND u.role = 'Lecturer' " +
            "LEFT JOIN students s ON u.user_id = s.student_id AND u.role = 'Student' " +
            "LEFT JOIN technical_officers t ON u.user_id = t.officer_id AND u.role = 'Technical_Officer' " +
            "ORDER BY u.user_id ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                int id = rs.getInt("user_id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                
                if ("Technical_Officer".equals(role)) role = "Technical Officer";
                else if ("Student".equals(role)) role = "Undergraduate";
                
                String name = rs.getString("name");
                if (name == null) name = "Unknown";
                
                String email = rs.getString("email");
                if (email == null) email = "No Email";
                
                String status = rs.getString("status");
                
                users.add(new User(id, name, username, role, email, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static boolean deleteUser(int userId) {
        String query = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addUser(
        String name, String username, String email, String role, String password, String profilePic,
        int deptId, int academicYear,
        String studentRegNo, String gender, String dob, String nic, String phone, String address, String enrollDate, String studentStatus,
        String designation
    ) {
        String dbRole = role;
        if ("Technical Officer".equals(role)) dbRole = "Technical_Officer";
        else if ("Undergraduate".equals(role)) dbRole = "Student";
        
        String insertUserQuery = "INSERT INTO users (username, password_hash, role, profile_picture) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, username);
                stmt.setString(2, password.isEmpty() ? "default123" : password);
                stmt.setString(3, dbRole);
                stmt.setString(4, profilePic.isEmpty() ? "default_avatar.png" : profilePic);
                stmt.executeUpdate();
                
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    
                    String roleQuery = "";
                    PreparedStatement roleStmt = null;
                    if ("Admin".equals(dbRole)) {
                        roleQuery = "INSERT INTO admins (admin_id, name, email) VALUES (?, ?, ?)";
                        roleStmt = conn.prepareStatement(roleQuery);
                        roleStmt.setInt(1, userId);
                        roleStmt.setString(2, name);
                        roleStmt.setString(3, email);
                    } else if ("Lecturer".equals(dbRole)) {
                        roleQuery = "INSERT INTO lecturers (lecturer_id, department_id, lecturer_name, email, designation) VALUES (?, ?, ?, ?, ?)";
                        roleStmt = conn.prepareStatement(roleQuery);
                        roleStmt.setInt(1, userId);
                        roleStmt.setInt(2, deptId);
                        roleStmt.setString(3, name);
                        roleStmt.setString(4, email);
                        roleStmt.setString(5, designation);
                    } else if ("Technical_Officer".equals(dbRole)) {
                        roleQuery = "INSERT INTO technical_officers (officer_id, department_id, officer_name, email, phone) VALUES (?, ?, ?, ?, ?)";
                        roleStmt = conn.prepareStatement(roleQuery);
                        roleStmt.setInt(1, userId);
                        roleStmt.setInt(2, deptId);
                        roleStmt.setString(3, name);
                        roleStmt.setString(4, email);
                        roleStmt.setString(5, phone);
                    } else if ("Student".equals(dbRole)) {
                        roleQuery = "INSERT INTO students (student_id, department_id, student_reg_no, full_name, gender, dob, nic, email, phone, address, academic_year, enrollment_date, student_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        roleStmt = conn.prepareStatement(roleQuery);
                        roleStmt.setInt(1, userId);
                        roleStmt.setInt(2, deptId);
                        roleStmt.setString(3, studentRegNo);
                        roleStmt.setString(4, name);
                        roleStmt.setString(5, gender);
                        roleStmt.setString(6, dob);
                        roleStmt.setString(7, nic);
                        roleStmt.setString(8, email);
                        roleStmt.setString(9, phone);
                        roleStmt.setString(10, address);
                        roleStmt.setInt(11, academicYear);
                        roleStmt.setString(12, enrollDate);
                        roleStmt.setString(13, studentStatus);
                    }
                    
                    if (roleStmt != null) {
                        roleStmt.executeUpdate();
                        roleStmt.close();
                    }
                }
                
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Simplistic updateUser logic (does not handle role change for simplicity)
    public static boolean updateUser(int userId, String name, String username, String email, String role) {
        String dbRole = role;
        if ("Technical Officer".equals(role)) dbRole = "Technical_Officer";
        else if ("Undergraduate".equals(role)) dbRole = "Student";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update users table
                try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET username = ? WHERE user_id = ?")) {
                    stmt.setString(1, username);
                    stmt.setInt(2, userId);
                    stmt.executeUpdate();
                }
                
                // Update role specific table
                String roleQuery = "";
                if ("Admin".equals(dbRole)) {
                    roleQuery = "UPDATE admins SET name = ?, email = ? WHERE admin_id = ?";
                } else if ("Lecturer".equals(dbRole)) {
                    roleQuery = "UPDATE lecturers SET lecturer_name = ?, email = ? WHERE lecturer_id = ?";
                } else if ("Technical_Officer".equals(dbRole)) {
                    roleQuery = "UPDATE technical_officers SET officer_name = ?, email = ? WHERE officer_id = ?";
                } else if ("Student".equals(dbRole)) {
                    roleQuery = "UPDATE students SET full_name = ?, email = ? WHERE student_id = ?";
                }
                
                try (PreparedStatement roleStmt = conn.prepareStatement(roleQuery)) {
                    roleStmt.setString(1, name);
                    roleStmt.setString(2, email);
                    roleStmt.setInt(3, userId);
                    roleStmt.executeUpdate();
                }
                
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static main.java.model.admin.Admin getAdminProfile(int adminId) {
        String query = 
            "SELECT a.admin_id, a.name, a.email, a.phone, a.address, u.username, u.role, u.status, u.profile_picture " +
            "FROM admins a " +
            "JOIN users u ON a.admin_id = u.user_id " +
            "WHERE a.admin_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, adminId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new main.java.model.admin.Admin(
                        rs.getInt("admin_id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("status"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("profile_picture")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateAdminProfile(int adminId, String name, String email, String phone, String address, String profilePicture) {
        String adminQuery = "UPDATE admins SET name = ?, email = ?, phone = ?, address = ? WHERE admin_id = ?";
        String userQuery = "UPDATE users SET profile_picture = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(adminQuery)) {
                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.setString(3, phone);
                    stmt.setString(4, address);
                    stmt.setInt(5, adminId);
                    stmt.executeUpdate();
                }

                if (profilePicture != null && !profilePicture.trim().isEmpty()) {
                    try (PreparedStatement stmt2 = conn.prepareStatement(userQuery)) {
                        stmt2.setString(1, profilePicture.trim());
                        stmt2.setInt(2, adminId);
                        stmt2.executeUpdate();
                    }
                }

                conn.commit();
                conn.setAutoCommit(autoCommit);
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                conn.setAutoCommit(autoCommit);
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateAdminProfile(int adminId, String name, String email, String phone, String address) {
        return updateAdminProfile(adminId, name, email, phone, address, null);
    }

    public static boolean changeUserPassword(int userId, String oldPassword, String newPassword) {
        String checkQuery = "SELECT password_hash FROM users WHERE user_id = ?";
        String updateQuery = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, userId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String currentPass = rs.getString("password_hash");
                        if (!currentPass.equals(oldPassword)) {
                            return false; // Old password doesn't match
                        }
                    } else {
                        return false;
                    }
                }
            }
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setString(1, newPassword);
                updateStmt.setInt(2, userId);
                return updateStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
