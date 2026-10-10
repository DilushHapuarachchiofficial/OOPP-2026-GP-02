package main.java.ui.admin;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.StyledEditorKit;

import main.java.model.admin.Notice;
import main.java.dao.admin.NoticeDAO;

public class NoticeManagementPanel extends JPanel {
    private CardLayout cardLayout;
    private JLabel showingLabel;
    private JTable table;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    // Form components
    private JTextField titleField;
    private JComboBox<String> audienceCombo;
    private JTextPane descArea;
    private JTextField attachmentField;

    public NoticeManagementPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setBackground(new Color(245, 247, 250));
        
        JPanel listPanel = createListPanel();
        JPanel addPanel = createAddPanel();
        
        add(listPanel, "List");
        add(addPanel, "Add");
        
        cardLayout.show(this, "List");
    }
    
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        
        // --- Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Notice Board");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 40, 50));
        
        JLabel subtitleLabel = new JLabel("Publish and manage faculty notices");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 130, 140));
        
        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(subtitleLabel);
        
        JButton addNoticeBtn = new JButton("  + Publish Notice  ");
        addNoticeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addNoticeBtn.setForeground(Color.WHITE);
        addNoticeBtn.setBackground(new Color(30, 100, 220));
        addNoticeBtn.setFocusPainted(false);
        addNoticeBtn.setBorderPainted(false);
        addNoticeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addNoticeBtn.setPreferredSize(new Dimension(150, 36));
        addNoticeBtn.addActionListener(e -> {
            clearForm();
            cardLayout.show(this, "Add");
        });
        
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(addNoticeBtn, BorderLayout.EAST);
        
        // --- Content Section ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 25, 25, 25));
        
        // Table Wrapper
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true));
        
        // Table
        String[] columns = {"ID", "Title", "Target Audience", "Published Date", "Actions"};
        DefaultTableModel model = new DefaultTableModel(null, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 4; }
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
        table.getColumnModel().getColumn(2).setCellRenderer(new AudienceCellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionsCellRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionsCellEditor(table));
        
        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(350);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        
        // Pagination Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        showingLabel = new JLabel("Showing notices");
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
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        refreshTableData(model);
        return panel;
    }
    
    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        
        // Form Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Publish New Notice");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 40, 50));
        
        JLabel subtitleLabel = new JLabel("Enter notice details and publish");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 130, 140));
        
        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(subtitleLabel);
        
        JButton backBtn = new JButton("  Back to Notices  ");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(new Color(100, 110, 120));
        backBtn.setBackground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> cardLayout.show(this, "List"));
        
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);
        
        // Form Content
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(0, 25, 25, 25));
        
        JPanel box = new JPanel(new BorderLayout(0, 20));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 230), 1, true),
            new EmptyBorder(25, 30, 25, 30)
        ));
        
        Font labelFont = new Font("Segoe UI Semibold", Font.PLAIN, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 15);
        
        // Top Section: Title, Audience, Attachment
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        
        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 15));
        grid.setOpaque(false);
        
        JLabel l1 = new JLabel("Notice Title"); l1.setFont(labelFont);
        JLabel l2 = new JLabel("Target Audience"); l2.setFont(labelFont);
        
        titleField = new JTextField();
        titleField.setFont(fieldFont);
        String[] audiences = {"All", "Students", "Lecturers", "Technical_Officers"};
        audienceCombo = new JComboBox<>(audiences);
        audienceCombo.setFont(fieldFont);
        audienceCombo.setBackground(Color.WHITE);
        
        grid.add(l1); grid.add(titleField);
        grid.add(l2); grid.add(audienceCombo);
        
        JPanel attachmentPanel = new JPanel(new BorderLayout(10, 0));
        attachmentPanel.setOpaque(false);
        
        JLabel l3 = new JLabel("Attachment (Optional)"); l3.setFont(labelFont);
        
        attachmentField = new JTextField();
        attachmentField.setEditable(false);
        attachmentField.setFont(fieldFont);
        
        JButton browseBtn = new JButton("Browse...");
        browseBtn.setBackground(new Color(230, 230, 230));
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                attachmentField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        attachmentPanel.add(attachmentField, BorderLayout.CENTER);
        attachmentPanel.add(browseBtn, BorderLayout.EAST);
        
        JPanel attWrap = new JPanel(new BorderLayout(20, 0));
        attWrap.setOpaque(false);
        l3.setPreferredSize(l1.getPreferredSize());
        attWrap.add(l3, BorderLayout.WEST);
        attWrap.add(attachmentPanel, BorderLayout.CENTER);
        
        topPanel.add(grid);
        topPanel.add(Box.createVerticalStrut(20));
        topPanel.add(attWrap);
        
        // Description with Rich Text Editor
        JPanel descWrap = new JPanel(new BorderLayout(5, 5));
        descWrap.setOpaque(false);
        JLabel descLbl = new JLabel("Notice Content:");
        descLbl.setFont(labelFont);
        descWrap.add(descLbl, BorderLayout.NORTH);
        
        descArea = new JTextPane();
        descArea.setContentType("text/html");
        
        JScrollPane descScroll = new JScrollPane(descArea);
        
        // Toolbar for Rich Text Editor
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        Action boldAction = new StyledEditorKit.BoldAction();
        boldAction.putValue(Action.NAME, "Bold");
        Action italicAction = new StyledEditorKit.ItalicAction();
        italicAction.putValue(Action.NAME, "Italic");
        Action underlineAction = new StyledEditorKit.UnderlineAction();
        underlineAction.putValue(Action.NAME, "Underline");
        
        toolBar.add(boldAction);
        toolBar.add(italicAction);
        toolBar.add(underlineAction);
        
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(toolBar, BorderLayout.NORTH);
        editorPanel.add(descScroll, BorderLayout.CENTER);
        
        descWrap.add(editorPanel, BorderLayout.CENTER);
        
        // Save Button
        JButton saveBtn = new JButton("Publish Notice");
        saveBtn.setBackground(new Color(41, 128, 185));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        saveBtn.setPreferredSize(new Dimension(180, 42));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveNotice());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(saveBtn);
        
        box.add(topPanel, BorderLayout.NORTH);
        box.add(descWrap, BorderLayout.CENTER);
        box.add(btnPanel, BorderLayout.SOUTH);
        
        formContainer.add(box, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(formContainer, BorderLayout.CENTER);
        return panel;
    }
    
    private void clearForm() {
        titleField.setText("");
        audienceCombo.setSelectedIndex(0);
        attachmentField.setText("");
        descArea.setText("");
    }
    
    private void saveNotice() {
        String title = titleField.getText();
        String desc = descArea.getText();
        String audience = (String) audienceCombo.getSelectedItem();
        String attachment = attachmentField.getText();
        
        if (title.isEmpty() || desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
            return;
        }
        
        if (attachment.isEmpty()) {
            attachment = null;
        }
        
        // Assume publishedBy = 1 for admin session placeholder
        if (NoticeDAO.addNotice(title, desc, audience, 1, attachment)) {
            JOptionPane.showMessageDialog(this, "Notice published successfully!");
            refreshTableData((DefaultTableModel) table.getModel());
            cardLayout.show(this, "List");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to publish notice.");
        }
    }
    
    private void refreshTableData(DefaultTableModel model) {
        if(table == null) return;
        model.setRowCount(0);
        List<Notice> list = NoticeDAO.getAllNotices();
        for (Notice n : list) {
            String idStr = String.format("N%03d", n.getNoticeId());
            String dateStr = n.getPublishedDate() != null ? sdf.format(n.getPublishedDate()) : "";
            model.addRow(new Object[]{ idStr, n.getTitle(), n.getTargetAudience(), dateStr, "" });
        }
        if (showingLabel != null) showingLabel.setText("Showing 1 - " + list.size() + " of " + list.size() + " notices");
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
    
    class AudienceCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String valStr = value != null ? value.toString() : "";
            Color bgColor = new Color(240, 240, 240);
            Color fgColor = new Color(100, 100, 100);
            
            if (valStr.equals("All")) { bgColor = new Color(225, 238, 255); fgColor = new Color(0, 102, 204); }
            else if (valStr.equals("Students")) { bgColor = new Color(254, 239, 227); fgColor = new Color(204, 102, 0); }
            else if (valStr.equals("Lecturers")) { bgColor = new Color(246, 234, 255); fgColor = new Color(138, 43, 226); }
            else if (valStr.equals("Technical_Officers")) { bgColor = new Color(230, 248, 234); fgColor = new Color(34, 139, 34); }
            
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
                        int id = Integer.parseInt(idStr.substring(1));
                        int confirm = JOptionPane.showConfirmDialog(panel, "Delete notice " + idStr + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            if (NoticeDAO.deleteNotice(id)) {
                                ((DefaultTableModel)table.getModel()).removeRow(currentRow);
                            } else {
                                JOptionPane.showMessageDialog(panel, "Failed to delete notice.");
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
