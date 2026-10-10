package main.java.ui.admin;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import main.java.model.admin.Course;
import main.java.dao.admin.CourseDAO;

public class CourseManagementPanel extends JPanel {
    private CardLayout cardLayout;
    private JLabel showingLabel;
    private JTable table;
    
    private JPanel listPanel;
    private JPanel formPanel;
    
    // Form fields
    private JTextField codeField;
    private JTextField nameField;
    private JTextField creditField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> semCombo;
    private JComboBox<String> yearCombo;

    public CourseManagementPanel() {
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
        listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(245, 247, 250));
        
        // --- Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Course Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 40, 50));
        
        JLabel subtitleLabel = new JLabel("View and manage academic courses");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 130, 140));
        
        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(subtitleLabel);
        
        JButton addCourseBtn = new JButton("  + Add Course  ");
        addCourseBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addCourseBtn.setForeground(Color.WHITE);
        addCourseBtn.setBackground(new Color(30, 100, 220));
        addCourseBtn.setFocusPainted(false);
        addCourseBtn.setBorderPainted(false);
        addCourseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addCourseBtn.setPreferredSize(new Dimension(130, 36));
        addCourseBtn.addActionListener(e -> {
            clearForm();
            cardLayout.show(this, "Form");
        });
        
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(addCourseBtn, BorderLayout.EAST);
        
        // --- Content Section ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 25, 25, 25));
        
        // Table Wrapper
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true));
        
        String[] columns = {"ID", "Course Code", "Course Name", "Credits", "Type", "Year/Sem", "Actions"};
        DefaultTableModel model = new DefaultTableModel(null, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 6; }
        };
        
        table = new JTable(model);
        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(new Color(60, 70, 80));
        table.setSelectionBackground(new Color(240, 245, 250));
        table.setSelectionForeground(new Color(30, 40, 50));
        
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(Color.WHITE);
        tableHeader.setForeground(new Color(100, 110, 120));
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        tableHeader.setPreferredSize(new Dimension(100, 40));
        
        table.getColumnModel().getColumn(4).setCellRenderer(new TypeCellRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionsCellRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionsCellEditor(table));
        
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        showingLabel = new JLabel("Showing courses");
        showingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showingLabel.setForeground(new Color(120, 130, 140));
        
        footerPanel.add(showingLabel, BorderLayout.WEST);
        
        contentPanel.add(tableWrapper, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
        
        listPanel.add(headerPanel, BorderLayout.NORTH);
        listPanel.add(contentPanel, BorderLayout.CENTER);
        
        refreshTableData();
    }
    
    private void buildFormPanel() {
        formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(new Color(245, 247, 250));
        
        // Form Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Add New Course");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 40, 50));
        
        JLabel subtitleLabel = new JLabel("Enter course details below");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 130, 140));
        
        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(subtitleLabel);
        
        JButton backBtn = new JButton("  Back to Courses  ");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(new Color(100, 110, 120));
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> cardLayout.show(this, "List"));
        
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);
        
        // Form Container
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setOpaque(false);
        
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        // Add a subtle shadow-like effect with borders
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1, true),
            new EmptyBorder(40, 50, 40, 50)
        ));
        
        JPanel grid = new JPanel(new GridLayout(6, 2, 20, 25));
        grid.setOpaque(false);
        
        Font labelFont = new Font("Segoe UI Semibold", Font.PLAIN, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 15);
        Color labelColor = new Color(70, 80, 90);
        
        javax.swing.border.Border fieldBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 220), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        
        codeField = new JTextField(); 
        codeField.setFont(fieldFont);
        codeField.setBorder(fieldBorder);
        
        nameField = new JTextField(); 
        nameField.setFont(fieldFont);
        nameField.setBorder(fieldBorder);
        
        creditField = new JTextField(); 
        creditField.setFont(fieldFont);
        creditField.setBorder(fieldBorder);
        
        String[] types = {"Theory_Only", "Practical_Only", "Theory_and_Practical"};
        typeCombo = new JComboBox<>(types); 
        typeCombo.setFont(fieldFont);
        typeCombo.setBackground(Color.WHITE);
        
        String[] sems = {"1", "2"};
        semCombo = new JComboBox<>(sems); 
        semCombo.setFont(fieldFont);
        semCombo.setBackground(Color.WHITE);
        
        String[] years = {"1", "2", "3", "4"};
        yearCombo = new JComboBox<>(years); 
        yearCombo.setFont(fieldFont);
        yearCombo.setBackground(Color.WHITE);
        
        JLabel l1 = new JLabel("Course Code"); l1.setFont(labelFont); l1.setForeground(labelColor);
        JLabel l2 = new JLabel("Course Name"); l2.setFont(labelFont); l2.setForeground(labelColor);
        JLabel l3 = new JLabel("Credits"); l3.setFont(labelFont); l3.setForeground(labelColor);
        JLabel l4 = new JLabel("Course Type"); l4.setFont(labelFont); l4.setForeground(labelColor);
        JLabel l5 = new JLabel("Semester"); l5.setFont(labelFont); l5.setForeground(labelColor);
        JLabel l6 = new JLabel("Academic Year"); l6.setFont(labelFont); l6.setForeground(labelColor);
        
        grid.add(l1); grid.add(codeField);
        grid.add(l2); grid.add(nameField);
        grid.add(l3); grid.add(creditField);
        grid.add(l4); grid.add(typeCombo);
        grid.add(l5); grid.add(semCombo);
        grid.add(l6); grid.add(yearCombo);
        
        box.add(grid);
        box.add(Box.createVerticalStrut(35));
        
        JButton saveBtn = new JButton("Save Course");
        saveBtn.setBackground(new Color(41, 128, 185)); // Elegant blue
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setPreferredSize(new Dimension(150, 40));
        saveBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        saveBtn.addActionListener(e -> saveCourse());
        
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
        
        formPanel.add(headerPanel, BorderLayout.NORTH);
        formPanel.add(sp, BorderLayout.CENTER);
    }
    
    private void clearForm() {
        codeField.setText("");
        nameField.setText("");
        creditField.setText("");
        typeCombo.setSelectedIndex(0);
        semCombo.setSelectedIndex(0);
        yearCombo.setSelectedIndex(0);
    }
    
    private void saveCourse() {
        try {
            String code = codeField.getText();
            String name = nameField.getText();
            int credit = Integer.parseInt(creditField.getText());
            String type = (String) typeCombo.getSelectedItem();
            int sem = Integer.parseInt((String) semCombo.getSelectedItem());
            int year = Integer.parseInt((String) yearCombo.getSelectedItem());
            
            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields.");
                return;
            }
            
            if (CourseDAO.addCourse(1, code, name, credit, type, sem, year, null)) {
                JOptionPane.showMessageDialog(this, "Course added successfully!");
                refreshTableData();
                cardLayout.show(this, "List");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add course.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Check credits format.");
        }
    }
    
    private void refreshTableData() {
        if(table == null) return;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        List<Course> list = CourseDAO.getAllCourses();
        for (Course c : list) {
            String idStr = String.format("C%03d", c.getCourseId());
            String yearSem = "Y" + c.getAcademicYear() + " S" + c.getSemester();
            model.addRow(new Object[]{ idStr, c.getCourseCode(), c.getCourseName(), c.getCredit(), c.getCourseType(), yearSem, "" });
        }
        if (showingLabel != null) showingLabel.setText("Showing 1 - " + list.size() + " of " + list.size() + " courses");
    }
    
    class TypeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String valStr = value != null ? value.toString() : "";
            Color bgColor = new Color(240, 240, 240);
            Color fgColor = new Color(100, 100, 100);
            
            if (valStr.equals("Theory_Only")) { bgColor = new Color(230, 244, 255); fgColor = new Color(0, 102, 204); }
            else if (valStr.equals("Practical_Only")) { bgColor = new Color(255, 235, 238); fgColor = new Color(204, 0, 51); }
            else if (valStr.equals("Theory_and_Practical")) { bgColor = new Color(230, 248, 234); fgColor = new Color(34, 139, 34); }
            
            final Color fBgColor = bgColor;
            JLabel label = new JLabel(valStr.replace("_", " ")) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(fBgColor);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(fgColor);
            label.setBorder(new EmptyBorder(4, 12, 4, 12));
            panel.add(label);
            return panel;
        }
    }
    
    class ActionsCellRenderer extends DefaultTableCellRenderer {
        private ImageIcon deleteIconImg;
        public ActionsCellRenderer() {
            try {
                ImageIcon icon = new ImageIcon("src/main/resources/images/admin-icons/action_delete.png");
                deleteIconImg = new ImageIcon(icon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
            } catch (Exception e) {}
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            if (deleteIconImg != null) {
                JLabel del = new JLabel(deleteIconImg);
                del.setCursor(new Cursor(Cursor.HAND_CURSOR));
                panel.add(del);
            }
            return panel;
        }
    }
    
    class ActionsCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private JPanel panel;
        private int currentRow;
        public ActionsCellEditor(JTable table) {
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
            panel.setBackground(table.getSelectionBackground());
            try {
                ImageIcon icon = new ImageIcon("src/main/resources/images/admin-icons/action_delete.png");
                ImageIcon delIcon = new ImageIcon(icon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
                JLabel del = new JLabel(delIcon);
                del.setCursor(new Cursor(Cursor.HAND_CURSOR));
                del.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        fireEditingStopped();
                        String idStr = (String) table.getValueAt(currentRow, 0);
                        int id = Integer.parseInt(idStr.substring(1));
                        int confirm = JOptionPane.showConfirmDialog(panel, "Delete course " + idStr + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            if (CourseDAO.deleteCourse(id)) {
                                ((DefaultTableModel)table.getModel()).removeRow(currentRow);
                            } else {
                                JOptionPane.showMessageDialog(panel, "Failed to delete.");
                            }
                        }
                    }
                });
                panel.add(del);
            } catch(Exception e){}
        }
        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean isSelected, int row, int column) {
            this.currentRow = row;
            return panel;
        }
        @Override
        public Object getCellEditorValue() { return ""; }
    }
}
