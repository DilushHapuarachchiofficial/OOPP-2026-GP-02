package main.java.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import main.java.model.User;
import main.java.dao.admin.UserDAO;

public class UserManagementPanel extends JPanel {
    private CardLayout cardLayout;
    private JLabel showingLabel;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTable table;
    
    private JPanel listPanel;
    private JPanel formPanel;
    
    private JTextField searchBox;
    private JComboBox<String> roleFilter;
    
    // Form Components
    private JLabel formTitle;
    private JLabel formSubtitle;
    private JTextField nameField, usernameField, emailField, passField, picField;
    private JComboBox<String> roleCombo;
    
    // Dynamic Fields
    private JComboBox<String> deptCombo, yearCombo, genderCombo, studentStatusCombo, designationCombo;
    private JTextField regNoField, dobField, nicField, phoneField, enrollDateField;
    private JTextArea addressArea;
    private JLabel picPreview;
    
    private JPanel dynamicPanel;
    private JButton saveBtn;
    private int editingUserId = -1; 
    
    private Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
    private Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

    public UserManagementPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setBackground(new Color(245, 247, 250));
        
        buildListPanel();
        buildFormPanel();
        
        add(listPanel, "List");
        add(formPanel, "Form");
        
        cardLayout.show(this, "List");
    }
    
    private void buildListPanel() {
        listPanel = new JPanel(new BorderLayout(0, 20));
        listPanel.setBackground(new Color(245, 247, 250));
        listPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("User Management");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pageTitle.setForeground(new Color(23, 32, 48));
        JLabel pageSubtitle = new JLabel("Manage all system users");
        pageSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pageSubtitle.setForeground(new Color(120, 130, 140));
        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);
        
        JButton addUserBtn = new JButton("  + Add User...  ");
        addUserBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addUserBtn.setForeground(Color.WHITE);
        addUserBtn.setBackground(new Color(30, 100, 220));
        addUserBtn.setFocusPainted(false);
        addUserBtn.setBorderPainted(false);
        addUserBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addUserBtn.setPreferredSize(new Dimension(130, 36));
        
        addUserBtn.addActionListener(e -> openFormForAdd());
        
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(addUserBtn, BorderLayout.EAST);
        
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setOpaque(false);
        
        searchBox = new JTextField("Search by name or username...", 30);
        searchBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchBox.setForeground(Color.GRAY);
        searchBox.setPreferredSize(new Dimension(250, 36));
        
        searchBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if(searchBox.getText().equals("Search by name or username...")) {
                    searchBox.setText("");
                    searchBox.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if(searchBox.getText().isEmpty()) {
                    searchBox.setForeground(Color.GRAY);
                    searchBox.setText("Search by name or username...");
                }
            }
        });
        
        String[] roles = {"All Roles", "Undergraduate", "Lecturer", "Technical Officer", "Admin"};
        roleFilter = new JComboBox<>(roles);
        roleFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roleFilter.setBackground(Color.WHITE);
        roleFilter.setPreferredSize(new Dimension(150, 36));
        
        JPanel filterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        filterRight.setOpaque(false);
        filterRight.add(searchBox);
        filterRight.add(Box.createHorizontalStrut(15));
        filterRight.add(roleFilter);
        
        filterPanel.add(filterRight, BorderLayout.EAST);
        
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(headerPanel);
        topContainer.add(Box.createVerticalStrut(25));
        topContainer.add(filterPanel);
        listPanel.add(topContainer, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Username", "Role", "Email", "Status", "Actions"};
        tableModel = new DefaultTableModel(null, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 6; }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(240, 245, 255));
        table.setSelectionForeground(new Color(30, 40, 50));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        
        searchBox.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
        roleFilter.addActionListener(e -> filterTable());
        
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(Color.WHITE);
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setForeground(new Color(100, 110, 120));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        tableHeader.setPreferredSize(new Dimension(100, 40));
        
        table.getColumnModel().getColumn(3).setCellRenderer(new RoleCellRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionsCellRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionsCellEditor(table));
        
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);
        listPanel.add(tableCard, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        showingLabel = new JLabel("Showing users");
        showingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showingLabel.setForeground(new Color(120, 130, 140));
        
        footerPanel.add(showingLabel, BorderLayout.WEST);
        listPanel.add(footerPanel, BorderLayout.SOUTH);
        
        refreshTableData();
    }
    
    private void filterTable() {
        String searchTxt = searchBox.getText().equals("Search by name or username...") ? "" : searchBox.getText();
        String roleTxt = (String) roleFilter.getSelectedItem();
        
        RowFilter<DefaultTableModel, Object> searchFilter = null;
        RowFilter<DefaultTableModel, Object> roleF = null;
        
        try {
            if (!searchTxt.isEmpty()) {
                searchFilter = RowFilter.regexFilter("(?i)" + searchTxt, 1, 2); 
            }
            if (!"All Roles".equals(roleTxt)) {
                roleF = RowFilter.regexFilter("^" + roleTxt + "$", 3);
            }
            
            if (searchFilter != null && roleF != null) {
                rowSorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(searchFilter, roleF)));
            } else if (searchFilter != null) {
                rowSorter.setRowFilter(searchFilter);
            } else if (roleF != null) {
                rowSorter.setRowFilter(roleF);
            } else {
                rowSorter.setRowFilter(null);
            }
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
    }
    
    private void buildFormPanel() {
        formPanel = new JPanel(new BorderLayout(0, 20));
        formPanel.setBackground(new Color(245, 247, 250));
        formPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        formTitle = new JLabel("Add New User");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        formTitle.setForeground(new Color(23, 32, 48));
        formSubtitle = new JLabel("Enter user details below");
        formSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formSubtitle.setForeground(new Color(120, 130, 140));
        titleBlock.add(formTitle);
        titleBlock.add(formSubtitle);
        
        JButton backBtn = new JButton("  Back to Users  ");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(new Color(100, 110, 120));
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> cardLayout.show(this, "List"));
        
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);
        
        formPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setOpaque(false);
        
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            new EmptyBorder(30, 40, 30, 40)
        ));
        
        // --- Static Fields ---
        JPanel staticPanel = new JPanel(new GridLayout(6, 2, 15, 15));
        staticPanel.setOpaque(false);
        
        String[] roles = {"Admin", "Lecturer", "Technical Officer", "Undergraduate"};
        roleCombo = new JComboBox<>(roles);
        roleCombo.setFont(fieldFont);
        roleCombo.addActionListener(e -> updateDynamicFields());
        
        nameField = new JTextField(); nameField.setFont(fieldFont);
        usernameField = new JTextField(); usernameField.setFont(fieldFont);
        emailField = new JTextField(); emailField.setFont(fieldFont);
        passField = new JTextField(); passField.setFont(fieldFont);
        
        // Profile Pic
        JPanel picPanel = new JPanel(new BorderLayout(10, 0));
        picPanel.setOpaque(false);
        picField = new JTextField("default_avatar.png");
        picField.setFont(fieldFont);
        picField.setEditable(false);
        
        JButton browseBtn = new JButton("Browse...");
        browseBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        browseBtn.setBackground(new Color(230, 230, 230));
        browseBtn.setFocusPainted(false);
        
        picPreview = new JLabel();
        picPreview.setPreferredSize(new Dimension(50, 50));
        picPreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        picPreview.setHorizontalAlignment(SwingConstants.CENTER);
        
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                picField.setText(path);
                try {
                    ImageIcon icon = new ImageIcon(path);
                    Image scaled = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                    picPreview.setIcon(new ImageIcon(scaled));
                } catch (Exception ex) {
                    picPreview.setIcon(null);
                    picPreview.setText("Error");
                }
            }
        });
        
        picPanel.add(picField, BorderLayout.CENTER);
        picPanel.add(browseBtn, BorderLayout.EAST);
        JPanel picContainer = new JPanel(new BorderLayout(10, 0));
        picContainer.setOpaque(false);
        picContainer.add(picPreview, BorderLayout.WEST);
        picContainer.add(picPanel, BorderLayout.CENTER);
        
        JLabel lRole = new JLabel("Role:"); lRole.setFont(labelFont);
        JLabel lName = new JLabel("Full Name:"); lName.setFont(labelFont);
        JLabel lUser = new JLabel("Username:"); lUser.setFont(labelFont);
        JLabel lEmail = new JLabel("Email Address:"); lEmail.setFont(labelFont);
        JLabel lPass = new JLabel("Password:"); lPass.setFont(labelFont);
        JLabel lPic = new JLabel("Profile Picture:"); lPic.setFont(labelFont);
        
        // Role is first!
        staticPanel.add(lRole); staticPanel.add(roleCombo);
        staticPanel.add(lName); staticPanel.add(nameField);
        staticPanel.add(lUser); staticPanel.add(usernameField);
        staticPanel.add(lEmail); staticPanel.add(emailField);
        staticPanel.add(lPass); staticPanel.add(passField);
        staticPanel.add(lPic); staticPanel.add(picContainer);
        
        box.add(staticPanel);
        
        // --- Dynamic Fields Setup ---
        dynamicPanel = new JPanel();
        dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.Y_AXIS));
        dynamicPanel.setOpaque(false);
        dynamicPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        
        String[] depts = {"1 (ICT)", "2 (ET)", "3 (BST)"};
        deptCombo = new JComboBox<>(depts); deptCombo.setFont(fieldFont);
        String[] years = {"1", "2", "3", "4"};
        yearCombo = new JComboBox<>(years); yearCombo.setFont(fieldFont);
        regNoField = new JTextField(); regNoField.setFont(fieldFont);
        String[] genders = {"Male", "Female", "Other"};
        genderCombo = new JComboBox<>(genders); genderCombo.setFont(fieldFont);
        dobField = new JTextField("2000-01-01"); dobField.setFont(fieldFont);
        nicField = new JTextField(); nicField.setFont(fieldFont);
        phoneField = new JTextField(); phoneField.setFont(fieldFont);
        addressArea = new JTextArea(2, 20); addressArea.setFont(fieldFont);
        addressArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        enrollDateField = new JTextField("2024-01-01"); enrollDateField.setFont(fieldFont);
        String[] statuses = {"Regular", "Repeat", "Batch_Missed"};
        studentStatusCombo = new JComboBox<>(statuses); studentStatusCombo.setFont(fieldFont);
        String[] designations = {"Professor", "Senior Lecturer", "Lecturer"};
        designationCombo = new JComboBox<>(designations); designationCombo.setFont(fieldFont);
        
        box.add(dynamicPanel);
        
        saveBtn = new JButton("Save User");
        saveBtn.setBackground(new Color(30, 100, 220));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        saveBtn.addActionListener(e -> saveUser());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(saveBtn);
        box.add(btnPanel);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(box, BorderLayout.NORTH);
        
        formContainer.add(wrapper, gbc);
        
        JScrollPane sp = new JScrollPane(formContainer);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        
        formPanel.add(sp, BorderLayout.CENTER);
        
        updateDynamicFields();
    }
    
    private void updateDynamicFields() {
        dynamicPanel.removeAll();
        String role = (String) roleCombo.getSelectedItem();
        
        if ("Undergraduate".equals(role)) {
            JPanel grid = new JPanel(new GridLayout(10, 2, 15, 15));
            grid.setOpaque(false);
            
            grid.add(new JLabel("Department:")); grid.add(deptCombo);
            grid.add(new JLabel("Academic Year:")); grid.add(yearCombo);
            grid.add(new JLabel("Reg No:")); grid.add(regNoField);
            grid.add(new JLabel("Gender:")); grid.add(genderCombo);
            grid.add(new JLabel("Date of Birth (YYYY-MM-DD):")); grid.add(dobField);
            grid.add(new JLabel("NIC:")); grid.add(nicField);
            grid.add(new JLabel("Phone:")); grid.add(phoneField);
            grid.add(new JLabel("Address:")); grid.add(new JScrollPane(addressArea));
            grid.add(new JLabel("Enrollment Date (YYYY-MM-DD):")); grid.add(enrollDateField);
            grid.add(new JLabel("Student Status:")); grid.add(studentStatusCombo);
            
            dynamicPanel.add(grid);
        } else if ("Lecturer".equals(role)) {
            JPanel grid = new JPanel(new GridLayout(2, 2, 15, 15));
            grid.setOpaque(false);
            grid.add(new JLabel("Department:")); grid.add(deptCombo);
            grid.add(new JLabel("Designation:")); grid.add(designationCombo);
            dynamicPanel.add(grid);
        } else if ("Technical Officer".equals(role)) {
            JPanel grid = new JPanel(new GridLayout(2, 2, 15, 15));
            grid.setOpaque(false);
            grid.add(new JLabel("Department:")); grid.add(deptCombo);
            grid.add(new JLabel("Phone:")); grid.add(phoneField);
            dynamicPanel.add(grid);
        }
        
        // Re-apply fonts dynamically
        for(Component c : dynamicPanel.getComponents()) {
            if (c instanceof JPanel) {
                for (Component subC : ((JPanel) c).getComponents()) {
                    if (subC instanceof JLabel) subC.setFont(labelFont);
                }
            }
        }
        
        dynamicPanel.revalidate();
        dynamicPanel.repaint();
    }
    
    private void clearForm() {
        roleCombo.setSelectedIndex(0);
        nameField.setText("");
        usernameField.setText("");
        emailField.setText("");
        passField.setText("");
        picField.setText("default_avatar.png");
        picPreview.setIcon(null);
        
        deptCombo.setSelectedIndex(0);
        yearCombo.setSelectedIndex(0);
        regNoField.setText("");
        genderCombo.setSelectedIndex(0);
        dobField.setText("2000-01-01");
        nicField.setText("");
        phoneField.setText("");
        addressArea.setText("");
        enrollDateField.setText("2024-01-01");
        studentStatusCombo.setSelectedIndex(0);
        designationCombo.setSelectedIndex(0);
        
        updateDynamicFields();
    }
    
    private void openFormForAdd() {
        editingUserId = -1;
        formTitle.setText("Add New User");
        formSubtitle.setText("Enter user details below");
        saveBtn.setText("Create User");
        
        clearForm();
        
        roleCombo.setEnabled(true);
        passField.setEnabled(true);
        
        cardLayout.show(this, "Form");
    }
    
    private void openFormForEdit(int id, String name, String username, String email, String role) {
        editingUserId = id;
        formTitle.setText("Edit User (ID: " + id + ")");
        formSubtitle.setText("Update user information");
        saveBtn.setText("Save Changes");
        
        clearForm();
        
        nameField.setText(name);
        usernameField.setText(username);
        emailField.setText(email);
        
        passField.setText("********");
        passField.setEnabled(false);
        
        roleCombo.setSelectedItem(role);
        roleCombo.setEnabled(false); 
        
        cardLayout.show(this, "Form");
    }
    
    private void saveUser() {
        String name = nameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String role = (String) roleCombo.getSelectedItem();
        String pass = passField.getText();
        String pic = picField.getText();
        
        int deptId = 1;
        int acadYear = 1;
        if (deptCombo.getParent() != null) {
            deptId = Integer.parseInt(((String) deptCombo.getSelectedItem()).split(" ")[0]);
        }
        if (yearCombo.getParent() != null) {
            acadYear = Integer.parseInt((String) yearCombo.getSelectedItem());
        }
        
        String regNo = regNoField.getText();
        String gender = (String) genderCombo.getSelectedItem();
        String dob = dobField.getText();
        String nic = nicField.getText();
        String phone = phoneField.getText();
        String address = addressArea.getText();
        String enrollDate = enrollDateField.getText();
        String studentStatus = (String) studentStatusCombo.getSelectedItem();
        String designation = (String) designationCombo.getSelectedItem();
        
        if(name.isEmpty() || username.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in required fields.");
            return;
        }
        
        if (editingUserId == -1) {
            // Add Mode
            boolean success = UserDAO.addUser(
                name, username, email, role, pass, pic,
                deptId, acadYear, regNo, gender, dob, nic, phone, address, enrollDate, studentStatus, designation
            );
            if (success) {
                JOptionPane.showMessageDialog(this, "User added successfully!");
                refreshTableData();
                clearForm();
                cardLayout.show(this, "List");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add user.");
            }
        } else {
            // Edit Mode
            boolean success = UserDAO.updateUser(editingUserId, name, username, email, role);
            if (success) {
                JOptionPane.showMessageDialog(this, "User updated successfully!");
                refreshTableData();
                clearForm();
                cardLayout.show(this, "List");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user.");
            }
        }
    }
    
    private void refreshTableData() {
        if(tableModel == null) return;
        tableModel.setRowCount(0);
        List<User> userList = UserDAO.getAllUsers();
        
        for (User u : userList) {
            String prefix = "U";
            if ("Admin".equals(u.getRole())) prefix = "A";
            else if ("Lecturer".equals(u.getRole())) prefix = "L";
            else if ("Technical Officer".equals(u.getRole())) prefix = "T";
            
            String displayId = String.format("%s%03d", prefix, u.getUserId());
            tableModel.addRow(new Object[]{
                displayId, u.getName(), u.getUsername(), u.getRole(), u.getEmail(), u.getStatus(), ""
            });
        }
        if (showingLabel != null) {
            showingLabel.setText("Showing 1 - " + userList.size() + " of " + userList.size() + " users");
        }
    }
    
    class RoleCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            String role = value.toString();
            JLabel label = new JLabel(role, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (role.equals("Undergraduate")) g2.setColor(new Color(195, 237, 208));
                    else if (role.equals("Lecturer")) g2.setColor(new Color(230, 235, 245));
                    else if (role.equals("Technical Officer")) g2.setColor(new Color(254, 239, 213));
                    else g2.setColor(new Color(215, 225, 255));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            if (role.equals("Undergraduate")) label.setForeground(new Color(40, 167, 69));
            else if (role.equals("Lecturer")) label.setForeground(new Color(100, 110, 130));
            else if (role.equals("Technical Officer")) label.setForeground(new Color(210, 130, 40));
            else label.setForeground(new Color(30, 100, 220));
            label.setBorder(new EmptyBorder(4, 12, 4, 12));
            panel.add(label);
            return panel;
        }
    }
    
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String status = value != null ? value.toString() : "Active";
            
            JLabel label = new JLabel(status, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (status.equals("Deactivated")) g2.setColor(new Color(255, 200, 200));
                    else g2.setColor(new Color(200, 240, 210));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            if (status.equals("Deactivated")) label.setForeground(new Color(220, 53, 69));
            else label.setForeground(new Color(40, 167, 69));
            label.setBorder(new EmptyBorder(4, 12, 4, 12));
            panel.add(label);
            return panel;
        }
    }
    
    class ActionsCellRenderer extends DefaultTableCellRenderer {
        private ImageIcon editIconImg;
        private ImageIcon deleteIconImg;
        public ActionsCellRenderer() {
            editIconImg = createScaledIcon("src/main/resources/images/admin-icons/action_edit.png", 14, 14);
            deleteIconImg = createScaledIcon("src/main/resources/images/admin-icons/action_delete.png", 14, 14);
        }
        private ImageIcon createScaledIcon(String path, int width, int height) {
            try {
                ImageIcon icon = new ImageIcon(path);
                Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            } catch (Exception e) { return null; }
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            if (editIconImg != null) panel.add(new JLabel(editIconImg));
            if (deleteIconImg != null) panel.add(new JLabel(deleteIconImg));
            return panel;
        }
    }
    
    class ActionsCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private JPanel panel;
        private int currentRow;
        public ActionsCellEditor(JTable t) {
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
            panel.setBackground(t.getSelectionBackground());
            ActionsCellRenderer renderer = new ActionsCellRenderer();
            
            JLabel editIcon = new JLabel(renderer.editIconImg);
            editIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editIcon.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    fireEditingStopped();
                    int modelRow = t.convertRowIndexToModel(currentRow);
                    String idStr = (String) tableModel.getValueAt(modelRow, 0);
                    int id = Integer.parseInt(idStr.substring(1));
                    String name = (String) tableModel.getValueAt(modelRow, 1);
                    String username = (String) tableModel.getValueAt(modelRow, 2);
                    String role = (String) tableModel.getValueAt(modelRow, 3);
                    String email = (String) tableModel.getValueAt(modelRow, 4);
                    
                    openFormForEdit(id, name, username, email, role);
                }
            });
            
            JLabel deleteIcon = new JLabel(renderer.deleteIconImg);
            deleteIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteIcon.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    fireEditingStopped();
                    int modelRow = t.convertRowIndexToModel(currentRow);
                    String displayId = (String) tableModel.getValueAt(modelRow, 0);
                    int userId = Integer.parseInt(displayId.substring(1));
                    
                    int confirm = JOptionPane.showConfirmDialog(panel, "Delete user " + displayId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (UserDAO.deleteUser(userId)) {
                            tableModel.removeRow(modelRow);
                            JOptionPane.showMessageDialog(panel, "User deleted.");
                        } else {
                            JOptionPane.showMessageDialog(panel, "Failed to delete user.");
                        }
                    }
                }
            });
            
            panel.add(editIcon);
            panel.add(deleteIcon);
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            return panel;
        }
        @Override
        public Object getCellEditorValue() { return ""; }
    }
}
