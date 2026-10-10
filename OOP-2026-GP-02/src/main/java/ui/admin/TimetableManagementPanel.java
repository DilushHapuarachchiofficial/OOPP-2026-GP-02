package ui;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import model.Timetable;
import dao.TimetableDAO;

public class TimetableManagementPanel extends JPanel {
    private JLabel showingLabel;
    private JTable table;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public TimetableManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        
        // --- Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Timetable Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 40, 50));
        
        JLabel subtitleLabel = new JLabel("Create and manage master timetables");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 130, 140));
        
        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(subtitleLabel);
        
        JButton addTimetableBtn = new JButton("  + Create Timetable  ");
        addTimetableBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addTimetableBtn.setForeground(Color.WHITE);
        addTimetableBtn.setBackground(new Color(30, 100, 220));
        addTimetableBtn.setFocusPainted(false);
        addTimetableBtn.setBorderPainted(false);
        addTimetableBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addTimetableBtn.setPreferredSize(new Dimension(170, 36));
        addTimetableBtn.addActionListener(e -> showAddTimetableDialog());
        
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(addTimetableBtn, BorderLayout.EAST);
        
        // --- Content Section ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 25, 25, 25));
        
        // Table Wrapper
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true));
        
        // Table
        String[] columns = {"ID", "Department ID", "Academic Year", "Semester", "Published Date", "Actions"};
        DefaultTableModel model = new DefaultTableModel(null, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 5; }
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
        
        // Custom Renderers
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionsCellRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionsCellEditor(table));
        
        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        
        // Pagination Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        showingLabel = new JLabel("Showing timetables");
        showingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showingLabel.setForeground(new Color(120, 130, 140));
        
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        paginationPanel.setOpaque(false);
        paginationPanel.add(createPageButton("Previous", false));
        paginationPanel.add(createPageButton("1", true));
        paginationPanel.add(createPageButton("Next", false));
        
        footerPanel.add(showingLabel, BorderLayout.WEST);
        footerPanel.add(paginationPanel, BorderLayout.EAST);
        
        contentPanel.add(tableWrapper, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        refreshTableData(model);
    }
    
    private void refreshTableData(DefaultTableModel model) {
        model.setRowCount(0);
        List<Timetable> list = TimetableDAO.getAllTimetables();
        for (Timetable t : list) {
            String idStr = String.format("TT%03d", t.getTimetableId());
            String dateStr = t.getPublishedDate() != null ? sdf.format(t.getPublishedDate()) : "";
            model.addRow(new Object[]{ idStr, t.getDepartmentId(), "Year " + t.getAcademicYear(), "Semester " + t.getSemester(), dateStr, "" });
        }
        if (showingLabel != null) showingLabel.setText("Showing 1 - " + list.size() + " of " + list.size() + " timetables");
    }
    
    private JButton createPageButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(4, 12, 4, 12));
        if (active) {
            btn.setBackground(new Color(30, 100, 220));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 110, 120));
        }
        return btn;
    }
    
    private void showAddTimetableDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Timetable", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] depts = {"1 (ICT)", "2 (ET)", "3 (BST)"};
        JComboBox<String> deptCombo = new JComboBox<>(depts);
        
        String[] years = {"1", "2", "3", "4"};
        JComboBox<String> yearCombo = new JComboBox<>(years);
        
        String[] sems = {"1", "2"};
        JComboBox<String> semCombo = new JComboBox<>(sems);
        
        panel.add(new JLabel("Department:"));
        panel.add(deptCombo);
        panel.add(new JLabel("Academic Year:"));
        panel.add(yearCombo);
        panel.add(new JLabel("Semester:"));
        panel.add(semCombo);
        
        JButton saveBtn = new JButton("Create");
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        saveBtn.addActionListener(e -> {
            int deptId = Integer.parseInt(((String) deptCombo.getSelectedItem()).split(" ")[0]);
            int year = Integer.parseInt((String) yearCombo.getSelectedItem());
            int sem = Integer.parseInt((String) semCombo.getSelectedItem());
            
            if (TimetableDAO.addTimetable(deptId, year, sem)) {
                JOptionPane.showMessageDialog(dialog, "Timetable created successfully!");
                dialog.dispose();
                refreshTableData((DefaultTableModel) table.getModel());
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create timetable.");
            }
        });
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
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
            JLabel del = new JLabel(deleteIconImg);
            del.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.add(del);
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
                        int id = Integer.parseInt(idStr.substring(2)); // TT001 -> 1
                        int confirm = JOptionPane.showConfirmDialog(panel, "Delete timetable " + idStr + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            if (TimetableDAO.deleteTimetable(id)) {
                                ((DefaultTableModel)table.getModel()).removeRow(currentRow);
                            } else {
                                JOptionPane.showMessageDialog(panel, "Failed to delete. It may have associated entries.");
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
