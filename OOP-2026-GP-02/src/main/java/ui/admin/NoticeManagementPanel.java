package main.java.ui.admin;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private SimpleDateFormat dateOnlySdf = new SimpleDateFormat("yyyy-MM-dd");
    
    // Form components
    private JTextField titleField;
    private JComboBox<String> audienceCombo;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JTextPane descArea;
    private JTextField attachmentField;

    // Filter components
    private JTextField searchFilterField;
    private JComboBox<String> audienceFilterCombo;
    private JTextField fromDateFilterField;
    private JTextField toDateFilterField;

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
        headerPanel.setBorder(new EmptyBorder(25, 25, 15, 25));
        
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Notice Board");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 40, 50));
        
        JLabel subtitleLabel = new JLabel("Publish, schedule and manage faculty notices");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 130, 140));
        
        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(subtitleLabel);
        
        JButton addNoticeBtn = new JButton("  + Publish Notice  ");
        addNoticeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addNoticeBtn.setForeground(Color.WHITE);
        addNoticeBtn.setBackground(new Color(21, 101, 192));
        addNoticeBtn.setFocusPainted(false);
        addNoticeBtn.setBorderPainted(false);
        addNoticeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addNoticeBtn.setPreferredSize(new Dimension(150, 36));
        addNoticeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { addNoticeBtn.setBackground(new Color(13, 71, 161)); }
            public void mouseExited(java.awt.event.MouseEvent e) { addNoticeBtn.setBackground(new Color(21, 101, 192)); }
        });
        addNoticeBtn.addActionListener(e -> {
            clearForm();
            cardLayout.show(this, "Add");
        });
        
        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(addNoticeBtn, BorderLayout.EAST);
        
        // --- Content Section ---
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 25, 25, 25));

        // --- Filter Bar (Search + Audience + From/To Filters) ---
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
            new EmptyBorder(2, 10, 2, 10)
        ));

        JLabel sLbl = new JLabel("Search:");
        sLbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        searchFilterField = new JTextField(12);
        searchFilterField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel audLbl = new JLabel("Audience:");
        audLbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        String[] audOptions = {"All Audiences", "All", "Students", "Lecturers", "Technical_Officers"};
        audienceFilterCombo = new JComboBox<>(audOptions);
        audienceFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        audienceFilterCombo.setBackground(Color.WHITE);

        JLabel fLbl = new JLabel("From:");
        fLbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        fromDateFilterField = new JTextField(8);
        fromDateFilterField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fromDateFilterField.setEditable(false);
        fromDateFilterField.setBackground(Color.WHITE);
        fromDateFilterField.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fromDateFilterField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openDatePicker(fromDateFilterField, true);
            }
        });

        JLabel tLbl = new JLabel("To:");
        tLbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        toDateFilterField = new JTextField(8);
        toDateFilterField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toDateFilterField.setEditable(false);
        toDateFilterField.setBackground(Color.WHITE);
        toDateFilterField.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toDateFilterField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openDatePicker(toDateFilterField, true);
            }
        });

        JButton applyFilterBtn = new JButton("Filter");
        applyFilterBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        applyFilterBtn.setBackground(new Color(21, 101, 192));
        applyFilterBtn.setForeground(Color.WHITE);
        applyFilterBtn.setFocusPainted(false);
        applyFilterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyFilterBtn.addActionListener(e -> applyFilters());

        JButton resetFilterBtn = new JButton("Reset");
        resetFilterBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resetFilterBtn.setBackground(Color.WHITE);
        resetFilterBtn.setForeground(new Color(80, 90, 100));
        resetFilterBtn.setFocusPainted(false);
        resetFilterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetFilterBtn.addActionListener(e -> {
            searchFilterField.setText("");
            audienceFilterCombo.setSelectedIndex(0);
            fromDateFilterField.setText("");
            toDateFilterField.setText("");
            applyFilters();
        });

        filterCard.add(sLbl);
        filterCard.add(searchFilterField);
        filterCard.add(audLbl);
        filterCard.add(audienceFilterCombo);
        filterCard.add(fLbl);
        filterCard.add(fromDateFilterField);
        filterCard.add(tLbl);
        filterCard.add(toDateFilterField);
        filterCard.add(applyFilterBtn);
        filterCard.add(resetFilterBtn);
        
        // Table Wrapper
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true));
        
        // Table
        String[] columns = {"ID", "Title", "Target Audience", "Valid From", "Valid To", "Status", "Actions"};
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
        
        // Custom Renderers
        table.getColumnModel().getColumn(2).setCellRenderer(new AudienceCellRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionsCellRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionsCellEditor(table));
        
        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(95);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);
        table.setFillsViewportHeight(true);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        
        // Footer info
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 5, 0, 5));
        
        showingLabel = new JLabel("Showing notices");
        showingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        showingLabel.setForeground(new Color(120, 130, 140));
        footerPanel.add(showingLabel, BorderLayout.WEST);
        
        contentPanel.add(filterCard, BorderLayout.NORTH);
        contentPanel.add(tableWrapper, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        applyFilters();
        return panel;
    }
    
    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        
        // Form Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(25, 25, 20, 25));
        
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
        
        // Form Content Container
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
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        
        // Top Section: Title, Audience, Date Pickers, Attachment
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        
        JPanel grid = new JPanel(new GridLayout(4, 2, 20, 15));
        grid.setOpaque(false);
        
        JLabel l1 = new JLabel("Notice Title"); l1.setFont(labelFont);
        JLabel l2 = new JLabel("Target Audience"); l2.setFont(labelFont);
        JLabel lFrom = new JLabel("Display From (Date)"); lFrom.setFont(labelFont);
        JLabel lTo = new JLabel("Display To (Date)"); lTo.setFont(labelFont);
        
        titleField = new JTextField();
        titleField.setFont(fieldFont);
        String[] audiences = {"All", "Students", "Lecturers", "Technical_Officers"};
        audienceCombo = new JComboBox<>(audiences);
        audienceCombo.setFont(fieldFont);
        audienceCombo.setBackground(Color.WHITE);

        // 1. From Date - Non-editable textfield with auto-current date + Calendar Button
        JPanel fromDateWrap = new JPanel(new BorderLayout(8, 0));
        fromDateWrap.setOpaque(false);
        fromDateField = new JTextField();
        fromDateField.setFont(fieldFont);
        fromDateField.setEditable(false);
        fromDateField.setBackground(Color.WHITE);
        fromDateField.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fromDateField.setText(dateOnlySdf.format(new Date())); // Auto current date
        fromDateField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openDatePicker(fromDateField, false);
            }
        });
        
        JButton fromPickBtn = new JButton("📅 Select Date");
        fromPickBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fromPickBtn.setBackground(new Color(21, 101, 192));
        fromPickBtn.setForeground(Color.WHITE);
        fromPickBtn.setFocusPainted(false);
        fromPickBtn.setBorderPainted(false);
        fromPickBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fromPickBtn.addActionListener(e -> openDatePicker(fromDateField, false));
        fromDateWrap.add(fromDateField, BorderLayout.CENTER);
        fromDateWrap.add(fromPickBtn, BorderLayout.EAST);

        // 2. To Date - Non-editable textfield + Calendar Button + Clear Button
        JPanel toDateWrap = new JPanel(new BorderLayout(8, 0));
        toDateWrap.setOpaque(false);
        toDateField = new JTextField();
        toDateField.setFont(fieldFont);
        toDateField.setEditable(false);
        toDateField.setBackground(Color.WHITE);
        toDateField.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toDateField.setText(""); // Empty for indefinite
        toDateField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openDatePicker(toDateField, true);
            }
        });

        JPanel toBtnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        toBtnGroup.setOpaque(false);

        JButton toPickBtn = new JButton("📅 Select Date");
        toPickBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toPickBtn.setBackground(new Color(21, 101, 192));
        toPickBtn.setForeground(Color.WHITE);
        toPickBtn.setFocusPainted(false);
        toPickBtn.setBorderPainted(false);
        toPickBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toPickBtn.addActionListener(e -> openDatePicker(toDateField, true));

        JButton clearToBtn = new JButton("✕ Clear");
        clearToBtn.setToolTipText("Clear to make notice permanent");
        clearToBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearToBtn.setBackground(Color.WHITE);
        clearToBtn.setForeground(new Color(100, 116, 139));
        clearToBtn.setFocusPainted(false);
        clearToBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearToBtn.addActionListener(e -> toDateField.setText(""));

        toBtnGroup.add(toPickBtn);
        toBtnGroup.add(clearToBtn);

        toDateWrap.add(toDateField, BorderLayout.CENTER);
        toDateWrap.add(toBtnGroup, BorderLayout.EAST);
        
        grid.add(l1); grid.add(titleField);
        grid.add(l2); grid.add(audienceCombo);
        grid.add(lFrom); grid.add(fromDateWrap);
        grid.add(lTo); grid.add(toDateWrap);
        
        JPanel attachmentPanel = new JPanel(new BorderLayout(10, 0));
        attachmentPanel.setOpaque(false);
        
        JLabel l3 = new JLabel("Attachment (Optional)"); l3.setFont(labelFont);
        
        attachmentField = new JTextField();
        attachmentField.setEditable(false);
        attachmentField.setFont(fieldFont);
        attachmentField.setBackground(Color.WHITE);
        
        JButton browseBtn = new JButton("Browse...");
        browseBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        browseBtn.setBackground(new Color(21, 101, 192));
        browseBtn.setForeground(Color.WHITE);
        browseBtn.setFocusPainted(false);
        browseBtn.setBorderPainted(false);
        browseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        topPanel.add(Box.createVerticalStrut(15));
        topPanel.add(attWrap);
        
        // Description with Rich Text Editor
        JPanel descWrap = new JPanel(new BorderLayout(5, 5));
        descWrap.setOpaque(false);
        JLabel descLbl = new JLabel("Notice Content:");
        descLbl.setFont(labelFont);
        descWrap.add(descLbl, BorderLayout.NORTH);
        
        descArea = new JTextPane();
        descArea.setContentType("text/plain");
        
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(0, 180));
        descScroll.setMinimumSize(new Dimension(0, 150));
        
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
        saveBtn.setBackground(new Color(21, 101, 192));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        saveBtn.setPreferredSize(new Dimension(180, 42));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { saveBtn.setBackground(new Color(13, 71, 161)); }
            public void mouseExited(java.awt.event.MouseEvent e) { saveBtn.setBackground(new Color(21, 101, 192)); }
        });
        saveBtn.addActionListener(e -> saveNotice());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        btnPanel.setOpaque(false);
        btnPanel.add(saveBtn);
        
        box.add(topPanel, BorderLayout.NORTH);
        box.add(descWrap, BorderLayout.CENTER);
        box.add(btnPanel, BorderLayout.SOUTH);
        
        formContainer.add(box, BorderLayout.CENTER);

        // Vertically scrollable page wrapper to prevent content truncation
        JScrollPane pageScrollPane = new JScrollPane(formContainer);
        pageScrollPane.setBorder(BorderFactory.createEmptyBorder());
        pageScrollPane.getViewport().setBackground(new Color(245, 247, 250));
        pageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pageScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        pageScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(pageScrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Interactive calendar popup dialog for selecting dates
     */
    private void openDatePicker(JTextField targetField, boolean allowClear) {
        Window window = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(window, "Select Date", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(320, 340);
        dialog.setLocationRelativeTo(targetField);
        dialog.setResizable(false);
        dialog.setLayout(new BorderLayout());

        java.util.Calendar cal = java.util.Calendar.getInstance();
        if (!targetField.getText().trim().isEmpty()) {
            try {
                Date existing = dateOnlySdf.parse(targetField.getText().trim());
                cal.setTime(existing);
            } catch (Exception ignored) {}
        }

        int[] currentYear = { cal.get(java.util.Calendar.YEAR) };
        int[] currentMonth = { cal.get(java.util.Calendar.MONTH) };

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(21, 101, 192));
        header.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        monthLabel.setForeground(Color.WHITE);

        JButton prevBtn = new JButton("<");
        prevBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        prevBtn.setBackground(new Color(13, 71, 161));
        prevBtn.setForeground(Color.WHITE);
        prevBtn.setFocusPainted(false);
        prevBtn.setBorderPainted(false);
        prevBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton nextBtn = new JButton(">");
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nextBtn.setBackground(new Color(13, 71, 161));
        nextBtn.setForeground(Color.WHITE);
        nextBtn.setFocusPainted(false);
        nextBtn.setBorderPainted(false);
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        header.add(prevBtn, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);

        JPanel daysGrid = new JPanel(new GridLayout(7, 7, 3, 3));
        daysGrid.setBackground(Color.WHITE);
        daysGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                               "July", "August", "September", "October", "November", "December"};

        Runnable updateCalendar = new Runnable() {
            @Override
            public void run() {
                monthLabel.setText(monthNames[currentMonth[0]] + " " + currentYear[0]);
                daysGrid.removeAll();

                String[] daysOfWeek = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
                for (String d : daysOfWeek) {
                    JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    lbl.setForeground(new Color(100, 116, 139));
                    daysGrid.add(lbl);
                }

                java.util.Calendar temp = java.util.Calendar.getInstance();
                temp.set(currentYear[0], currentMonth[0], 1);
                int firstDayOfWeek = temp.get(java.util.Calendar.DAY_OF_WEEK) - 1;
                int daysInMonth = temp.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

                for (int i = 0; i < firstDayOfWeek; i++) {
                    daysGrid.add(new JLabel(""));
                }

                java.util.Calendar todayCal = java.util.Calendar.getInstance();
                boolean isThisMonth = (todayCal.get(java.util.Calendar.YEAR) == currentYear[0] &&
                                       todayCal.get(java.util.Calendar.MONTH) == currentMonth[0]);
                int todayDate = todayCal.get(java.util.Calendar.DAY_OF_MONTH);

                for (int day = 1; day <= daysInMonth; day++) {
                    final int selectedDay = day;
                    JButton dayBtn = new JButton(String.valueOf(day));
                    dayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    dayBtn.setMargin(new Insets(2, 2, 2, 2));
                    dayBtn.setFocusPainted(false);
                    dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    if (isThisMonth && day == todayDate) {
                        dayBtn.setBackground(new Color(21, 101, 192));
                        dayBtn.setForeground(Color.WHITE);
                    } else {
                        dayBtn.setBackground(new Color(248, 250, 252));
                        dayBtn.setForeground(new Color(30, 41, 59));
                    }

                    dayBtn.addActionListener(e -> {
                        String formatted = String.format("%04d-%02d-%02d", currentYear[0], currentMonth[0] + 1, selectedDay);
                        targetField.setText(formatted);
                        dialog.dispose();
                    });
                    daysGrid.add(dayBtn);
                }

                int totalCells = firstDayOfWeek + daysInMonth;
                int remaining = (totalCells % 7 == 0) ? 0 : 7 - (totalCells % 7);
                for (int i = 0; i < remaining; i++) {
                    daysGrid.add(new JLabel(""));
                }

                daysGrid.revalidate();
                daysGrid.repaint();
            }
        };

        prevBtn.addActionListener(e -> {
            if (currentMonth[0] == 0) {
                currentMonth[0] = 11;
                currentYear[0]--;
            } else {
                currentMonth[0]--;
            }
            updateCalendar.run();
        });

        nextBtn.addActionListener(e -> {
            if (currentMonth[0] == 11) {
                currentMonth[0] = 0;
                currentYear[0]++;
            } else {
                currentMonth[0]++;
            }
            updateCalendar.run();
        });

        updateCalendar.run();

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footer.setBackground(new Color(245, 247, 250));

        if (allowClear) {
            JButton clearBtn = new JButton("Clear / Permanent");
            clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            clearBtn.setBackground(Color.WHITE);
            clearBtn.setForeground(new Color(100, 116, 139));
            clearBtn.setFocusPainted(false);
            clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            clearBtn.addActionListener(e -> {
                targetField.setText("");
                dialog.dispose();
            });
            footer.add(clearBtn);
        }

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setForeground(new Color(100, 116, 139));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());
        footer.add(cancelBtn);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(daysGrid, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void clearForm() {
        titleField.setText("");
        audienceCombo.setSelectedIndex(0);
        fromDateField.setText(dateOnlySdf.format(new Date())); // Auto current date
        toDateField.setText("");
        attachmentField.setText("");
        descArea.setText("");
    }
    
    private void saveNotice() {
        String title = titleField.getText().trim();
        String desc = ""; try { desc = descArea.getDocument().getText(0, descArea.getDocument().getLength()).trim(); } catch(Exception ignored){ desc = descArea.getText().trim(); } if (desc.isEmpty() && !descArea.getText().trim().isEmpty()) { desc = descArea.getText().trim(); } desc = desc.replaceAll("(?s)<head.*?>.*?</head>", "").replaceAll("(?s)<style.*?>.*?</style>", "").replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").trim();
        String audience = (String) audienceCombo.getSelectedItem();
        String attachment = attachmentField.getText().trim();
        String fromStr = fromDateField.getText().trim();
        String toStr = toDateField.getText().trim();
        
        if (title.isEmpty() || desc.isEmpty()) {
            ModernDialog.showMessageDialog(this, "Please fill in all required fields (Notice Title and Content).", "Validation Error", ModernDialog.WARNING_MESSAGE);
            return;
        }
        
        if (attachment.isEmpty()) {
            attachment = null;
        }

        Timestamp fromTimestamp = null;
        Timestamp toTimestamp = null;

        SimpleDateFormat[] formats = {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm"),
            new SimpleDateFormat("yyyy-MM-dd")
        };

        // 1. From Date Logic: If selected from calendar, parse it; if blank, defaults to current time
        if (!fromStr.isEmpty()) {
            Date parsedFrom = parseDateFlexible(fromStr, formats);
            if (parsedFrom != null) {
                fromTimestamp = new Timestamp(parsedFrom.getTime());
            } else {
                fromTimestamp = new Timestamp(System.currentTimeMillis());
            }
        } else {
            fromTimestamp = new Timestamp(System.currentTimeMillis());
        }

        // 2. To Date Logic: If selected, parse it with end-of-day 23:59:59; if blank, remains null (permanent)
        if (!toStr.isEmpty()) {
            Date parsedTo = parseDateFlexible(toStr, formats);
            if (parsedTo != null) {
                if (toStr.length() == 10) {
                    parsedTo = new Date(parsedTo.getTime() + (24L * 60 * 60 * 1000 - 1000));
                }
                toTimestamp = new Timestamp(parsedTo.getTime());
            }
        } else {
            toTimestamp = null;
        }

        if (fromTimestamp != null && toTimestamp != null && toTimestamp.before(fromTimestamp)) {
            ModernDialog.showMessageDialog(this, "'Display To' date cannot be earlier than 'Display From' date.", "Validation Error", ModernDialog.WARNING_MESSAGE);
            return;
        }
        
        int adminId = 1;
        main.java.model.UserSession session = main.java.model.UserSession.getCurrentSession();
        if (session != null && session.getUserId() > 0) {
            adminId = session.getUserId();
        }

        if (NoticeDAO.addNotice(title, desc, audience, adminId, attachment, fromTimestamp, toTimestamp)) {
            ModernDialog.showMessageDialog(this, "Notice published successfully!", "Success", ModernDialog.INFORMATION_MESSAGE);
            clearForm();
            applyFilters();
            cardLayout.show(this, "List");
        } else {
            ModernDialog.showMessageDialog(this, "Failed to publish notice.", "Error", ModernDialog.ERROR_MESSAGE);
        }
    }
    
    private Date parseDateFlexible(String text, SimpleDateFormat[] formats) {
        for (SimpleDateFormat fmt : formats) {
            try {
                fmt.setLenient(false);
                return fmt.parse(text);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void applyFilters() {
        if (table == null) return;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        String search = (searchFilterField != null) ? searchFilterField.getText().trim().toLowerCase() : "";
        String aud = (audienceFilterCombo != null) ? (String) audienceFilterCombo.getSelectedItem() : "All Audiences";
        String filterFromStr = (fromDateFilterField != null) ? fromDateFilterField.getText().trim() : "";
        String filterToStr = (toDateFilterField != null) ? toDateFilterField.getText().trim() : "";

        SimpleDateFormat[] formats = {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm"),
            new SimpleDateFormat("yyyy-MM-dd")
        };

        Date filterFromDate = !filterFromStr.isEmpty() ? parseDateFlexible(filterFromStr, formats) : null;
        Date filterToDate = !filterToStr.isEmpty() ? parseDateFlexible(filterToStr, formats) : null;
        if (filterToDate != null && filterToStr.length() == 10) {
            filterToDate = new Date(filterToDate.getTime() + (24L * 60 * 60 * 1000 - 1000));
        }

        List<Notice> list = NoticeDAO.getAllNotices();
        int count = 0;
        for (Notice n : list) {
            // Filter Search Keyword
            if (!search.isEmpty()) {
                boolean matchTitle = n.getTitle() != null && n.getTitle().toLowerCase().contains(search);
                boolean matchDesc = n.getDescription() != null && n.getDescription().toLowerCase().contains(search);
                if (!matchTitle && !matchDesc) continue;
            }
            // Filter Target Audience
            if (aud != null && !aud.equals("All Audiences") && !aud.equalsIgnoreCase("All")) {
                if (!aud.equalsIgnoreCase(n.getTargetAudience()) && !"All".equalsIgnoreCase(n.getTargetAudience())) {
                    continue;
                }
            }
            // Filter From Date
            if (filterFromDate != null) {
                Date nFrom = n.getFromDate() != null ? n.getFromDate() : n.getPublishedDate();
                if (nFrom != null && nFrom.before(filterFromDate)) {
                    continue;
                }
            }
            // Filter To Date
            if (filterToDate != null) {
                Date nTo = n.getToDate();
                if (nTo != null && nTo.after(filterToDate)) {
                    continue;
                }
            }

            String idStr = String.format("N%03d", n.getNoticeId());
            String fromDateStr = n.getFromDate() != null ? dateOnlySdf.format(n.getFromDate()) : (n.getPublishedDate() != null ? dateOnlySdf.format(n.getPublishedDate()) : "-");
            String toDateStr = n.getToDate() != null ? dateOnlySdf.format(n.getToDate()) : "Permanent";
            String status = n.getStatus();

            model.addRow(new Object[]{ idStr, n.getTitle(), n.getTargetAudience(), fromDateStr, toDateStr, status, "" });
            count++;
        }

        if (showingLabel != null) {
            showingLabel.setText("Showing " + count + " " + (count == 1 ? "notice" : "notices"));
        }
    }
    
    /**
     * Modern sleek scrollbar UI matching application design
     */
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(203, 213, 225);
            this.trackColor = new Color(245, 247, 250);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(185, 198, 214));
            g2.fill(new RoundRectangle2D.Float(thumbBounds.x + 2, thumbBounds.y + 1, thumbBounds.width - 4, thumbBounds.height - 2, 6, 6));
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(245, 247, 250));
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
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

    class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            String status = value != null ? value.toString() : "Active";
            Color bgColor;
            Color fgColor;
            
            if ("Active".equalsIgnoreCase(status)) {
                bgColor = new Color(230, 248, 234);
                fgColor = new Color(22, 163, 74);
            } else if ("Scheduled".equalsIgnoreCase(status)) {
                bgColor = new Color(225, 238, 255);
                fgColor = new Color(37, 99, 235);
            } else {
                bgColor = new Color(254, 237, 237);
                fgColor = new Color(220, 38, 38);
            }
            
            final Color fBgColor = bgColor;
            JLabel label = new JLabel(status) {
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
                        int confirm = ModernDialog.showConfirmDialog(panel, "Delete notice " + idStr + "?", "Confirm Delete", ModernDialog.YES_NO_OPTION);
                        if (confirm == ModernDialog.YES_OPTION) {
                            if (NoticeDAO.deleteNotice(id)) {
                                applyFilters();
                            } else {
                                ModernDialog.showMessageDialog(panel, "Failed to delete notice.", "Error", ModernDialog.ERROR_MESSAGE);
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
