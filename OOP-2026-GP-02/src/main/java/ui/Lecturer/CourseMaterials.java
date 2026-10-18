package main.java.ui.Lecturer;

import main.java.model.Lecturer.CourseMaterialsLogic;
import main.java.model.Lecturer.CourseMaterialsLogic.CourseItem;
import main.java.model.Lecturer.CourseMaterialsLogic.CourseMaterialItem;
import main.java.model.Lecturer.CourseMaterialsLogic.MaterialsSummary;
import main.java.model.Lecturer.CourseMaterialsLogic.ValidationResult;
import main.java.model.UserSession;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CourseMaterials extends JPanel {

    // =========================================================================
    // COLOR PALETTE (TecFAMS Academic Portal Standard)
    // =========================================================================
    public static final Color COLOR_DEEP_NAVY      = new Color(8, 47, 90);    // #082F5A - Headings & brand
    public static final Color COLOR_PRO_BLUE       = new Color(11, 79, 156);  // #0B4F9C - Primary accents & navbar
    public static final Color COLOR_ACCENT_BLUE    = new Color(40, 120, 208); // #2878D0 - Highlights & links
    public static final Color COLOR_WHITE          = Color.WHITE;
    public static final Color COLOR_PAGE_BG        = new Color(244, 247, 251);// #F4F7FB - Canvas background
    public static final Color COLOR_CARD_BG        = Color.WHITE;
    public static final Color COLOR_BORDER         = new Color(220, 228, 238);// #DCE4EE - Subtle borders
    public static final Color COLOR_TEXT_PRIMARY   = new Color(15, 23, 42);   // #0F172A - High-contrast text
    public static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139);// #64748B - Subtitles & muted labels
    public static final Color COLOR_TEXT_MUTED     = new Color(148, 163, 184);// #94A3B8 - Hints
    public static final Color COLOR_ORANGE         = new Color(255, 107, 0);  // #FF6B00 - Highlights
    public static final Color COLOR_DANGER         = new Color(220, 38, 38);  // #DC2626 - Delete/Error red
    public static final Color COLOR_DANGER_BG      = new Color(254, 242, 242);// #FEF2F2 - Error soft background
    public static final Color COLOR_SUCCESS        = new Color(16, 185, 129); // #10B981 - Success green
    public static final Color COLOR_SUCCESS_BG     = new Color(236, 253, 245);
    public static final Color COLOR_ROW_ALT        = new Color(249, 251, 254);
    public static final Color COLOR_ROW_SELECTED   = new Color(235, 243, 254);

    // =========================================================================
    // TYPOGRAPHY (Windows Segoe UI Hierarchy)
    // =========================================================================
    public static final Font FONT_PAGE_TITLE       = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_PAGE_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_CARD_COUNT       = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_CARD_LABEL       = new Font("Segoe UI Semibold", Font.PLAIN, 12);
    public static final Font FONT_SECTION_TITLE    = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_TABLE_HEADER     = new Font("Segoe UI Semibold", Font.BOLD, 12);
    public static final Font FONT_TABLE_CELL       = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON           = new Font("Segoe UI Semibold", Font.BOLD, 13);
    public static final Font FONT_INPUT            = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BADGE            = new Font("Segoe UI Semibold", Font.BOLD, 11);

    // =========================================================================
    // VIEW STATE CONSTANTS
    // =========================================================================
    private static final String VIEW_LOADING         = "VIEW_LOADING";
    private static final String VIEW_SUCCESS         = "VIEW_SUCCESS";
    private static final String VIEW_ERROR           = "VIEW_ERROR";
    private static final String VIEW_UNAUTHENTICATED = "VIEW_UNAUTHENTICATED";

    // Layout Containers
    private CardLayout cardLayout;
    private JPanel stateContainer;

    // Authenticated Lecturer
    private int lecturerId = -1;

    // Summary Card Value Labels
    private JLabel totalCountLabel;
    private JLabel pdfCountLabel;
    private JLabel wordCountLabel;
    private JLabel pptCountLabel;

    // Filter Controls
    private JTextField searchField;
    private JComboBox<Object> courseFilterCombo;
    private JComboBox<String> typeFilterCombo;

    // Table Components
    private JTable materialsTable;
    private DefaultTableModel tableModel;
    private JLabel tableStatusLabel;
    private JPanel emptyStatePanel;
    private JScrollPane tableScrollPane;

    // Cached Data
    private List<CourseMaterialItem> currentMaterials = new ArrayList<>();
    private List<CourseItem> assignedCourses = new ArrayList<>();

    // Context Action Buttons
    private JButton viewDetailsBtn;
    private JButton openFileBtn;
    private JButton downloadFileBtn;
    private JButton editMaterialBtn;
    private JButton deleteMaterialBtn;

    // Error View Components
    private JLabel errorTitleLabel;
    private JTextArea errorDetailsArea;

    // Optional Navigation Wiring
    private Navbar parentNavbar;
    private CardLayout parentCardLayout;
    private JPanel parentContainer;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /**
     * Default constructor: Resolves lecturer ID from UserSession.
     */
    public CourseMaterials() {
        resolveLecturerSession();
        initializeUI();
        refreshMaterials();
    }

    /**
     * Constructor for explicit lecturer ID.
     */
    public CourseMaterials(int lecturerId) {
        this.lecturerId = lecturerId;
        initializeUI();
        refreshMaterials();
    }

    /**
     * Constructor for embedded navigation within a parent container.
     */
    public CourseMaterials(Navbar navbar, CardLayout layout, JPanel container) {
        this.parentNavbar = navbar;
        this.parentCardLayout = layout;
        this.parentContainer = container;
        resolveLecturerSession();
        initializeUI();
        refreshMaterials();
    }

    private void resolveLecturerSession() {
        UserSession session = UserSession.getCurrentSession();
        if (session != null && "Lecturer".equalsIgnoreCase(session.getRole())) {
            this.lecturerId = session.getUserId();
        } else if (this.lecturerId <= 0 && session == null) {
            // Default fallback for standalone execution / direct IDE testing
            this.lecturerId = 4;
        }
    }

    // =========================================================================
    // UI INITIALIZATION
    // =========================================================================
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_PAGE_BG);

        cardLayout = new CardLayout();
        stateContainer = new JPanel(cardLayout);
        stateContainer.setOpaque(false);

        // 1. Loading View
        stateContainer.add(createLoadingView(), VIEW_LOADING);

        // 2. Success View
        stateContainer.add(createSuccessView(), VIEW_SUCCESS);

        // 3. Error View
        stateContainer.add(createErrorView(), VIEW_ERROR);

        // 4. Unauthenticated View
        stateContainer.add(createUnauthenticatedView(), VIEW_UNAUTHENTICATED);

        add(stateContainer, BorderLayout.CENTER);
    }

    // =========================================================================
    // VIEW BUILDERS
    // =========================================================================

    /**
     * Builds the main success view containing header, summary cards, filter bar, and materials table.
     */
    private JPanel createSuccessView() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        // Central content panel with vertical scrolling
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(24, 32, 32, 32));

        // A. Page Header
        contentPanel.add(createHeaderPanel());
        contentPanel.add(Box.createVerticalStrut(20));

        // B. Summary Cards
        contentPanel.add(createSummaryCardsPanel());
        contentPanel.add(Box.createVerticalStrut(22));

        // C. Filters and Search
        contentPanel.add(createFilterPanel());
        contentPanel.add(Box.createVerticalStrut(16));

        // D. Materials Table Container Card
        contentPanel.add(createTableCard());

        // Wrap in clean vertical JScrollPane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    /**
     * Top Page Header with Title, Subtitle, and Primary "+ Upload Material" button.
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setOpaque(false);

        // Left Titles
        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel titleLabel = new JLabel("Course Materials");
        titleLabel.setFont(FONT_PAGE_TITLE);
        titleLabel.setForeground(COLOR_DEEP_NAVY);

        JLabel subtitleLabel = new JLabel("Manage and share learning resources for your assigned courses.");
        subtitleLabel.setFont(FONT_PAGE_SUBTITLE);
        subtitleLabel.setForeground(COLOR_TEXT_SECONDARY);

        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitleLabel);

        // Right Upload Button
        JButton uploadBtn = createStyledButton("+ Upload Material", COLOR_PRO_BLUE, COLOR_WHITE);
        uploadBtn.setPreferredSize(new Dimension(160, 40));
        uploadBtn.addActionListener(e -> showUploadDialog());

        panel.add(titles, BorderLayout.WEST);
        panel.add(uploadBtn, BorderLayout.EAST);

        return panel;
    }

    /**
     * 4 Summary Cards: Total Materials, PDF Documents, Word Documents, PowerPoint Presentations.
     */
    private JPanel createSummaryCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 18, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        totalCountLabel = new JLabel("0");
        pdfCountLabel = new JLabel("0");
        wordCountLabel = new JLabel("0");
        pptCountLabel = new JLabel("0");

        panel.add(createSummaryCard("Total Materials", totalCountLabel, "All active documents", COLOR_PRO_BLUE));
        panel.add(createSummaryCard("PDF Documents", pdfCountLabel, "Lecture notes & guides", new Color(220, 38, 38)));
        panel.add(createSummaryCard("Word Documents", wordCountLabel, "Tutorials & lab sheets", new Color(21, 101, 192)));
        panel.add(createSummaryCard("PowerPoint Slides", pptCountLabel, "Presentation slides", new Color(234, 88, 12)));

        return panel;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // White card background
                g2.setColor(COLOR_CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

                // 1px subtle border
                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 12, 12));

                // Top Accent Stripe
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, 4, 12, 12));

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_CARD_LABEL);
        titleLbl.setForeground(COLOR_TEXT_SECONDARY);

        valueLabel.setFont(FONT_CARD_COUNT);
        valueLabel.setForeground(COLOR_DEEP_NAVY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(COLOR_TEXT_MUTED);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(subLbl);

        return card;
    }

    /**
     * Search and Filter Bar.
     */
    private JPanel createFilterPanel() {
        JPanel card = createWhiteCardPanel();
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        // Left Controls: Search Field, Course Filter, Type Filter
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftControls.setOpaque(false);

        // Search Input
        searchField = new JTextField(18);
        searchField.setFont(FONT_INPUT);
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.putClientProperty("JTextField.placeholderText", "Search title or file name...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        // Course Dropdown
        courseFilterCombo = new JComboBox<>();
        courseFilterCombo.setFont(FONT_INPUT);
        courseFilterCombo.setPreferredSize(new Dimension(220, 36));
        courseFilterCombo.addItem("All Assigned Courses");
        courseFilterCombo.addActionListener(e -> applyFilters());

        // File Type Dropdown
        typeFilterCombo = new JComboBox<>(new String[]{"All Types", "PDF", "Word", "PowerPoint"});
        typeFilterCombo.setFont(FONT_INPUT);
        typeFilterCombo.setPreferredSize(new Dimension(140, 36));
        typeFilterCombo.addActionListener(e -> applyFilters());

        leftControls.add(new JLabel("Search:"));
        leftControls.add(searchField);
        leftControls.add(new JLabel("Course:"));
        leftControls.add(courseFilterCombo);
        leftControls.add(new JLabel("Format:"));
        leftControls.add(typeFilterCombo);

        // Right Controls: Reset & Refresh
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        JButton resetBtn = createStyledButton("Reset", new Color(241, 245, 249), COLOR_TEXT_PRIMARY);
        resetBtn.setPreferredSize(new Dimension(80, 36));
        resetBtn.addActionListener(e -> resetFilters());

        JButton refreshBtn = createStyledButton("Refresh", COLOR_ACCENT_BLUE, COLOR_WHITE);
        refreshBtn.setPreferredSize(new Dimension(90, 36));
        refreshBtn.addActionListener(e -> refreshMaterials());

        rightControls.add(resetBtn);
        rightControls.add(refreshBtn);

        card.add(leftControls, BorderLayout.CENTER);
        card.add(rightControls, BorderLayout.EAST);

        return card;
    }

    /**
     * Materials Table Card with custom table, action toolbar, and empty state.
     */
    private JPanel createTableCard() {
        JPanel card = createWhiteCardPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Top bar of card: Table Status
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel heading = new JLabel("Uploaded Course Materials");
        heading.setFont(FONT_SECTION_TITLE);
        heading.setForeground(COLOR_DEEP_NAVY);

        tableStatusLabel = new JLabel("Loading materials...");
        tableStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableStatusLabel.setForeground(COLOR_TEXT_SECONDARY);

        topBar.add(heading, BorderLayout.WEST);
        topBar.add(tableStatusLabel, BorderLayout.EAST);

        // Table Model Setup
        String[] columnNames = {"#", "Material Title", "Course", "File Name", "Format", "Upload Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Non-editable directly in cells
            }
        };

        materialsTable = new JTable(tableModel);
        materialsTable.setFont(FONT_TABLE_CELL);
        materialsTable.setRowHeight(38);
        materialsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        materialsTable.setShowVerticalLines(false);
        materialsTable.setShowHorizontalLines(true);
        materialsTable.setGridColor(new Color(241, 245, 249));
        materialsTable.setSelectionBackground(COLOR_ROW_SELECTED);
        materialsTable.setSelectionForeground(COLOR_DEEP_NAVY);

        // Column widths
        materialsTable.getColumnModel().getColumn(0).setPreferredWidth(45);  // #
        materialsTable.getColumnModel().getColumn(1).setPreferredWidth(260); // Title
        materialsTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Course
        materialsTable.getColumnModel().getColumn(3).setPreferredWidth(170); // File Name
        materialsTable.getColumnModel().getColumn(4).setPreferredWidth(90);  // Format
        materialsTable.getColumnModel().getColumn(5).setPreferredWidth(130); // Date

        // Table Header Styling
        JTableHeader header = materialsTable.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(COLOR_DEEP_NAVY);
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        // Format Badge Renderer
        materialsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(FONT_BADGE);

                String format = value != null ? value.toString() : "";
                if ("PDF".equalsIgnoreCase(format)) {
                    lbl.setForeground(new Color(220, 38, 38));
                    lbl.setText("  PDF  ");
                } else if ("Word".equalsIgnoreCase(format)) {
                    lbl.setForeground(new Color(21, 101, 192));
                    lbl.setText("  WORD  ");
                } else if ("PowerPoint".equalsIgnoreCase(format)) {
                    lbl.setForeground(new Color(234, 88, 12));
                    lbl.setText("  PPT  ");
                } else {
                    lbl.setForeground(COLOR_TEXT_SECONDARY);
                    lbl.setText("  " + format + "  ");
                }
                return lbl;
            }
        });

        // Alternating row colors for regular cells
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? COLOR_WHITE : COLOR_ROW_ALT);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        for (int i = 0; i < materialsTable.getColumnCount(); i++) {
            if (i != 4) {
                materialsTable.getColumnModel().getColumn(i).setCellRenderer(defaultRenderer);
            }
        }

        // Selection Listener to enable/disable row actions
        materialsTable.getSelectionModel().addListSelectionListener(e -> updateActionButtonsState());

        // Double-click row opens Details Dialog
        materialsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && materialsTable.getSelectedRow() != -1) {
                    showViewDetailsDialog();
                }
            }
        });

        tableScrollPane = new JScrollPane(materialsTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        tableScrollPane.setPreferredSize(new Dimension(800, 320));

        // Empty state panel (shown when 0 records match)
        emptyStatePanel = createEmptyStatePanel();
        emptyStatePanel.setVisible(false);

        // Center Stack containing Table and Empty State
        JPanel tableStack = new JPanel(new CardLayout());
        tableStack.setOpaque(false);
        tableStack.add(tableScrollPane, "TABLE");
        tableStack.add(emptyStatePanel, "EMPTY");

        // Action Buttons Toolbar below table
        JPanel actionToolbar = createActionToolbar();

        card.add(topBar, BorderLayout.NORTH);
        card.add(tableStack, BorderLayout.CENTER);
        card.add(actionToolbar, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Context Action Toolbar for table rows: View Details, Open, Save Copy, Edit, Delete.
     */
    private JPanel createActionToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setOpaque(false);

        viewDetailsBtn = createStyledButton("View Details", new Color(241, 245, 249), COLOR_TEXT_PRIMARY);
        viewDetailsBtn.addActionListener(e -> showViewDetailsDialog());

        openFileBtn = createStyledButton("Open Document", COLOR_PRO_BLUE, COLOR_WHITE);
        openFileBtn.addActionListener(e -> openSelectedFile());

        downloadFileBtn = createStyledButton("Save Copy As...", new Color(241, 245, 249), COLOR_TEXT_PRIMARY);
        downloadFileBtn.addActionListener(e -> downloadSelectedFile());

        editMaterialBtn = createStyledButton("Edit", new Color(241, 245, 249), COLOR_TEXT_PRIMARY);
        editMaterialBtn.addActionListener(e -> showEditDialog());

        deleteMaterialBtn = createStyledButton("Delete", COLOR_DANGER_BG, COLOR_DANGER);
        deleteMaterialBtn.addActionListener(e -> deleteSelectedMaterial());

        bar.add(viewDetailsBtn);
        bar.add(openFileBtn);
        bar.add(downloadFileBtn);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(editMaterialBtn);
        bar.add(deleteMaterialBtn);

        updateActionButtonsState();
        return bar;
    }

    private void updateActionButtonsState() {
        boolean hasSelection = materialsTable != null && materialsTable.getSelectedRow() != -1;
        if (viewDetailsBtn != null) viewDetailsBtn.setEnabled(hasSelection);
        if (openFileBtn != null) openFileBtn.setEnabled(hasSelection);
        if (downloadFileBtn != null) downloadFileBtn.setEnabled(hasSelection);
        if (editMaterialBtn != null) editMaterialBtn.setEnabled(hasSelection);
        if (deleteMaterialBtn != null) deleteMaterialBtn.setEnabled(hasSelection);
    }

    /**
     * Empty state panel when no materials match or zero uploaded.
     */
    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(800, 240));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel iconLabel = new JLabel("📁");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel("No Course Materials Found");
        msgLabel.setFont(FONT_SECTION_TITLE);
        msgLabel.setForeground(COLOR_DEEP_NAVY);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hintLabel = new JLabel("Click '+ Upload Material' above to share documents with your students.");
        hintLabel.setFont(FONT_PAGE_SUBTITLE);
        hintLabel.setForeground(COLOR_TEXT_SECONDARY);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(iconLabel);
        inner.add(Box.createVerticalStrut(8));
        inner.add(msgLabel);
        inner.add(Box.createVerticalStrut(4));
        inner.add(hintLabel);

        panel.add(inner);
        return panel;
    }

    // =========================================================================
    // STATE VIEWS (Loading, Error, Unauthenticated)
    // =========================================================================

    private JPanel createLoadingView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PAGE_BG);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JProgressBar pbar = new JProgressBar();
        pbar.setIndeterminate(true);
        pbar.setPreferredSize(new Dimension(220, 8));
        pbar.setForeground(COLOR_PRO_BLUE);
        pbar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel text = new JLabel("Connecting to TecFAMS Database...");
        text.setFont(FONT_PAGE_SUBTITLE);
        text.setForeground(COLOR_DEEP_NAVY);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(text);
        box.add(Box.createVerticalStrut(14));
        box.add(pbar);

        panel.add(box);
        return panel;
    }

    private JPanel createErrorView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PAGE_BG);

        JPanel card = createWhiteCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 40, 32, 40));
        card.setPreferredSize(new Dimension(520, 260));

        errorTitleLabel = new JLabel("Database Connection Error");
        errorTitleLabel.setFont(FONT_SECTION_TITLE);
        errorTitleLabel.setForeground(COLOR_DANGER);
        errorTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorDetailsArea = new JTextArea("Could not connect to MySQL server at localhost:3306.");
        errorDetailsArea.setWrapStyleWord(true);
        errorDetailsArea.setLineWrap(true);
        errorDetailsArea.setEditable(false);
        errorDetailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorDetailsArea.setForeground(COLOR_TEXT_SECONDARY);
        errorDetailsArea.setBackground(COLOR_CARD_BG);
        errorDetailsArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton retryBtn = createStyledButton("Retry Connection", COLOR_PRO_BLUE, COLOR_WHITE);
        retryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryBtn.addActionListener(e -> refreshMaterials());

        card.add(errorTitleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(errorDetailsArea);
        card.add(Box.createVerticalStrut(20));
        card.add(retryBtn);

        panel.add(card);
        return panel;
    }

    private JPanel createUnauthenticatedView() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_PAGE_BG);

        JPanel card = createWhiteCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 40, 32, 40));
        card.setPreferredSize(new Dimension(500, 220));

        JLabel title = new JLabel("Authentication Required");
        title.setFont(FONT_SECTION_TITLE);
        title.setForeground(COLOR_DEEP_NAVY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Please log in with an authenticated Lecturer account to manage materials.");
        desc.setFont(FONT_PAGE_SUBTITLE);
        desc.setForeground(COLOR_TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginBtn = createStyledButton("Go to Login", COLOR_PRO_BLUE, COLOR_WHITE);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win != null) win.dispose();
            try {
                Class<?> loginCls = Class.forName("main.java.ui.LoginUI");
                Object inst = loginCls.getDeclaredConstructor().newInstance();
                if (inst instanceof JFrame) ((JFrame) inst).setVisible(true);
            } catch (Exception ignored) {}
        });

        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(desc);
        card.add(Box.createVerticalStrut(22));
        card.add(loginBtn);

        panel.add(card);
        return panel;
    }

    // =========================================================================
    // ASYNCHRONOUS DATA LOADING (SwingWorker)
    // =========================================================================

    /**
     * Refreshes all materials and summary cards from the MySQL database using a non-blocking SwingWorker.
     */
    public void refreshMaterials() {
        resolveLecturerSession();

        if (lecturerId <= 0) {
            cardLayout.show(stateContainer, VIEW_UNAUTHENTICATED);
            return;
        }

        cardLayout.show(stateContainer, VIEW_LOADING);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private MaterialsSummary summary;
            private List<CourseItem> courses;
            private List<CourseMaterialItem> materials;
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    // Fetch genuine database data
                    summary = CourseMaterialsLogic.getSummary(lecturerId);
                    courses = CourseMaterialsLogic.getAssignedCourses(lecturerId);
                    materials = CourseMaterialsLogic.getMaterials(lecturerId, null, null, null);
                } catch (Exception ex) {
                    this.error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                if (error != null) {
                    errorTitleLabel.setText("Database Operation Failed");
                    errorDetailsArea.setText(error.getMessage() != null ? error.getMessage() : "An unexpected database error occurred.");
                    cardLayout.show(stateContainer, VIEW_ERROR);
                    return;
                }

                // Update summary cards
                if (summary != null) {
                    totalCountLabel.setText(String.valueOf(summary.getTotalCount()));
                    pdfCountLabel.setText(String.valueOf(summary.getPdfCount()));
                    wordCountLabel.setText(String.valueOf(summary.getWordCount()));
                    pptCountLabel.setText(String.valueOf(summary.getPptCount()));
                }

                // Populate courses filter
                if (courses != null) {
                    assignedCourses = courses;
                    populateCourseFilter(courses);
                }

                // Update materials table
                if (materials != null) {
                    currentMaterials = materials;
                    populateTable(materials);
                }

                cardLayout.show(stateContainer, VIEW_SUCCESS);
            }
        };

        worker.execute();
    }

    private void populateCourseFilter(List<CourseItem> courses) {
        courseFilterCombo.removeAllItems();
        courseFilterCombo.addItem("All Assigned Courses");
        for (CourseItem c : courses) {
            courseFilterCombo.addItem(c);
        }
    }

    private void populateTable(List<CourseMaterialItem> materials) {
        tableModel.setRowCount(0);

        for (int i = 0; i < materials.size(); i++) {
            CourseMaterialItem item = materials.get(i);
            tableModel.addRow(new Object[]{
                    (i + 1),
                    item.getTitle(),
                    item.getCourseCode() + " - " + item.getCourseName(),
                    item.getFileName(),
                    item.getFileType(),
                    formatDisplayDate(item.getUploadedDate())
            });
        }

        int count = materials.size();
        if (count == 0) {
            tableStatusLabel.setText("0 materials found");
            tableScrollPane.setVisible(false);
            emptyStatePanel.setVisible(true);
        } else {
            tableStatusLabel.setText("Showing " + count + " material" + (count > 1 ? "s" : ""));
            tableScrollPane.setVisible(true);
            emptyStatePanel.setVisible(false);
        }

        updateActionButtonsState();
    }

    /**
     * Filters cached materials based on the search query, course selection, and file type.
     */
    private void applyFilters() {
        if (currentMaterials == null) return;

        String query = searchField.getText().trim().toLowerCase();
        Object selectedCourse = courseFilterCombo.getSelectedItem();
        String selectedType = (String) typeFilterCombo.getSelectedItem();

        Integer filterCourseId = null;
        if (selectedCourse instanceof CourseItem) {
            filterCourseId = ((CourseItem) selectedCourse).getCourseId();
        }

        List<CourseMaterialItem> filtered = new ArrayList<>();
        for (CourseMaterialItem item : currentMaterials) {
            // Course match
            if (filterCourseId != null && item.getCourseId() != filterCourseId) {
                continue;
            }

            // Type match
            if (selectedType != null && !selectedType.equals("All Types")) {
                if (!selectedType.equalsIgnoreCase(item.getFileType())) {
                    continue;
                }
            }

            // Text search match
            if (!query.isEmpty()) {
                boolean matchTitle = item.getTitle().toLowerCase().contains(query);
                boolean matchFile = item.getFileName().toLowerCase().contains(query);
                boolean matchCourse = item.getCourseCode().toLowerCase().contains(query) ||
                                      item.getCourseName().toLowerCase().contains(query);
                if (!matchTitle && !matchFile && !matchCourse) {
                    continue;
                }
            }

            filtered.add(item);
        }

        populateTable(filtered);
    }

    private void resetFilters() {
        searchField.setText("");
        courseFilterCombo.setSelectedIndex(0);
        typeFilterCombo.setSelectedIndex(0);
        populateTable(currentMaterials);
    }

    private CourseMaterialItem getSelectedMaterial() {
        int row = materialsTable.getSelectedRow();
        if (row >= 0 && row < currentMaterials.size()) {
            return currentMaterials.get(row);
        }
        return null;
    }

    // =========================================================================
    // ACTION DIALOGS & FILE OPERATIONS
    // =========================================================================

    /**
     * Displays the Upload Material Dialog.
     */
    private void showUploadDialog() {
        if (lecturerId <= 0) {
            JOptionPane.showMessageDialog(this, "You must be authenticated as a lecturer to upload.",
                    "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (assignedCourses == null || assignedCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "You do not have any courses assigned to your account.\n" +
                    "Lecturers can only upload materials for their assigned courses.\n" +
                    "Please contact the administration.",
                    "No Assigned Courses", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Upload Course Material", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(COLOR_WHITE);
        form.setBorder(new EmptyBorder(24, 28, 24, 28));

        // 1. Course Selection
        JLabel courseLbl = new JLabel("Assigned Course * (Only your assigned courses)");
        courseLbl.setFont(FONT_CARD_LABEL);
        courseLbl.setForeground(COLOR_DEEP_NAVY);

        JComboBox<CourseItem> courseCombo = new JComboBox<>();
        courseCombo.setFont(FONT_INPUT);
        for (CourseItem c : assignedCourses) {
            courseCombo.addItem(c);
        }
        if (assignedCourses.size() == 1) {
            courseCombo.setSelectedIndex(0);
        }


        // 2. Material Title
        JLabel titleLbl = new JLabel("Material Title *");
        titleLbl.setFont(FONT_CARD_LABEL);
        titleLbl.setForeground(COLOR_DEEP_NAVY);

        JTextField titleField = new JTextField();
        titleField.setFont(FONT_INPUT);

        // 3. Description
        JLabel descLbl = new JLabel("Description (Optional)");
        descLbl.setFont(FONT_CARD_LABEL);
        descLbl.setForeground(COLOR_DEEP_NAVY);

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setFont(FONT_INPUT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);

        // 4. File Selection
        JLabel fileLbl = new JLabel("Document File * (PDF, Word, or PowerPoint up to 50 MB)");
        fileLbl.setFont(FONT_CARD_LABEL);
        fileLbl.setForeground(COLOR_DEEP_NAVY);

        final File[] chosenFile = new File[1];
        JLabel fileStatusLabel = new JLabel("No file selected.");
        fileStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        fileStatusLabel.setForeground(COLOR_TEXT_SECONDARY);

        JButton browseBtn = createStyledButton("Choose File...", new Color(241, 245, 249), COLOR_TEXT_PRIMARY);
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Course Material File");
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Permitted Documents (*.pdf, *.doc, *.docx, *.ppt, *.pptx)",
                    "pdf", "doc", "docx", "ppt", "pptx"
            ));
            int res = chooser.showOpenDialog(dialog);
            if (res == JFileChooser.APPROVE_OPTION) {
                File sel = chooser.getSelectedFile();
                ValidationResult val = CourseMaterialsLogic.validateFile(sel);
                if (val.isValid()) {
                    chosenFile[0] = sel;
                    fileStatusLabel.setText(sel.getName() + " (" + CourseMaterialsLogic.formatFileSize(sel.length()) + ")");
                    fileStatusLabel.setForeground(COLOR_SUCCESS);

                    // Pre-fill title if empty
                    if (titleField.getText().trim().isEmpty()) {
                        titleField.setText(CourseMaterialsLogic.getBaseName(sel.getName()).replace('_', ' '));
                    }
                } else {
                    chosenFile[0] = null;
                    fileStatusLabel.setText(val.getErrorMessage());
                    fileStatusLabel.setForeground(COLOR_DANGER);
                }
            }
        });

        JPanel fileRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fileRow.setOpaque(false);
        fileRow.add(browseBtn);
        fileRow.add(fileStatusLabel);

        // Form Assembly
        form.add(courseLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(courseCombo);
        form.add(Box.createVerticalStrut(14));

        form.add(titleLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(titleField);
        form.add(Box.createVerticalStrut(14));

        form.add(descLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(descScroll);
        form.add(Box.createVerticalStrut(14));

        form.add(fileLbl);
        form.add(Box.createVerticalStrut(6));
        form.add(fileRow);

        // Bottom Actions Bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        actions.setBackground(new Color(248, 250, 252));
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton cancelBtn = createStyledButton("Cancel", COLOR_WHITE, COLOR_TEXT_PRIMARY);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton submitBtn = createStyledButton("Upload Material", COLOR_PRO_BLUE, COLOR_WHITE);
        submitBtn.addActionListener(e -> {
            CourseItem selectedCourse = (CourseItem) courseCombo.getSelectedItem();
            if (selectedCourse == null) {
                JOptionPane.showMessageDialog(dialog, "Please select an assigned course.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a material title.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (chosenFile[0] == null) {
                JOptionPane.showMessageDialog(dialog, "Please choose a valid document file.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Disable button during background upload
            submitBtn.setEnabled(false);
            submitBtn.setText("Uploading...");

            SwingWorker<Integer, Void> uploadWorker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return CourseMaterialsLogic.uploadMaterial(
                            lecturerId,
                            selectedCourse.getCourseId(),
                            title,
                            descArea.getText().trim(),
                            chosenFile[0]
                    );
                }

                @Override
                protected void done() {
                    try {
                        get();
                        dialog.dispose();
                        JOptionPane.showMessageDialog(CourseMaterials.this,
                                "Course material uploaded successfully!",
                                "Upload Complete", JOptionPane.INFORMATION_MESSAGE);
                        refreshMaterials();
                    } catch (Exception ex) {
                        submitBtn.setEnabled(true);
                        submitBtn.setText("Upload Material");
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        JOptionPane.showMessageDialog(dialog, "Upload failed: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            uploadWorker.execute();
        });

        actions.add(cancelBtn);
        actions.add(submitBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Displays the View Details Dialog with full metadata and download/open actions.
     */
    private void showViewDetailsDialog() {
        CourseMaterialItem item = getSelectedMaterial();
        if (item == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Course Material Details", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_WHITE);
        content.setBorder(new EmptyBorder(22, 26, 22, 26));

        addDetailRow(content, "Material Title", item.getTitle(), FONT_SECTION_TITLE);
        addDetailRow(content, "Course", item.getCourseCode() + " - " + item.getCourseName(), FONT_INPUT);
        addDetailRow(content, "File Name", item.getFileName(), FONT_INPUT);
        addDetailRow(content, "Format & Size", item.getFileType() + " • " + item.getFileSizeFormatted(), FONT_INPUT);
        addDetailRow(content, "Upload Date", formatDisplayDate(item.getUploadedDate()), FONT_INPUT);

        // Description
        JLabel descHeader = new JLabel("Description:");
        descHeader.setFont(FONT_CARD_LABEL);
        descHeader.setForeground(COLOR_TEXT_SECONDARY);
        content.add(descHeader);
        content.add(Box.createVerticalStrut(4));

        JTextArea descArea = new JTextArea(item.getDescription().isEmpty() ? "No description provided." : item.getDescription());
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setFont(FONT_INPUT);
        descArea.setBackground(COLOR_ROW_ALT);
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
        content.add(descArea);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        actions.setBackground(new Color(248, 250, 252));
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton openBtn = createStyledButton("Open Document", COLOR_PRO_BLUE, COLOR_WHITE);
        openBtn.addActionListener(e -> {
            dialog.dispose();
            openSelectedFile();
        });

        JButton saveBtn = createStyledButton("Save Copy As...", new Color(241, 245, 249), COLOR_TEXT_PRIMARY);
        saveBtn.addActionListener(e -> {
            dialog.dispose();
            downloadSelectedFile();
        });

        JButton closeBtn = createStyledButton("Close", COLOR_WHITE, COLOR_TEXT_PRIMARY);
        closeBtn.addActionListener(e -> dialog.dispose());

        actions.add(closeBtn);
        actions.add(saveBtn);
        actions.add(openBtn);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel container, String label, String value, Font valueFont) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(FONT_CARD_LABEL);
        lbl.setForeground(COLOR_TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(valueFont);
        val.setForeground(COLOR_DEEP_NAVY);

        container.add(lbl);
        container.add(Box.createVerticalStrut(2));
        container.add(val);
        container.add(Box.createVerticalStrut(10));
    }

    /**
     * Displays the Edit Material Dialog.
     */
    private void showEditDialog() {
        CourseMaterialItem item = getSelectedMaterial();
        if (item == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Course Material", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(COLOR_WHITE);
        form.setBorder(new EmptyBorder(22, 26, 22, 26));

        JLabel courseLbl = new JLabel("Assigned Course *");
        courseLbl.setFont(FONT_CARD_LABEL);
        courseLbl.setForeground(COLOR_DEEP_NAVY);

        JComboBox<CourseItem> courseCombo = new JComboBox<>();
        courseCombo.setFont(FONT_INPUT);
        CourseItem currentSelection = null;
        for (CourseItem c : assignedCourses) {
            courseCombo.addItem(c);
            if (c.getCourseId() == item.getCourseId()) {
                currentSelection = c;
            }
        }
        if (currentSelection != null) courseCombo.setSelectedItem(currentSelection);

        JLabel titleLbl = new JLabel("Material Title *");
        titleLbl.setFont(FONT_CARD_LABEL);
        titleLbl.setForeground(COLOR_DEEP_NAVY);

        JTextField titleField = new JTextField(item.getTitle());
        titleField.setFont(FONT_INPUT);

        JLabel descLbl = new JLabel("Description (Optional)");
        descLbl.setFont(FONT_CARD_LABEL);
        descLbl.setForeground(COLOR_DEEP_NAVY);

        JTextArea descArea = new JTextArea(item.getDescription(), 3, 20);
        descArea.setFont(FONT_INPUT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);

        form.add(courseLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(courseCombo);
        form.add(Box.createVerticalStrut(12));

        form.add(titleLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(titleField);
        form.add(Box.createVerticalStrut(12));

        form.add(descLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(descScroll);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        actions.setBackground(new Color(248, 250, 252));
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton cancelBtn = createStyledButton("Cancel", COLOR_WHITE, COLOR_TEXT_PRIMARY);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = createStyledButton("Save Changes", COLOR_PRO_BLUE, COLOR_WHITE);
        saveBtn.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            if (newTitle.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CourseItem destCourse = (CourseItem) courseCombo.getSelectedItem();
            if (destCourse == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a valid course.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                CourseMaterialsLogic.updateMaterial(
                        lecturerId,
                        item.getMaterialId(),
                        destCourse.getCourseId(),
                        newTitle,
                        descArea.getText().trim()
                );
                dialog.dispose();
                JOptionPane.showMessageDialog(CourseMaterials.this, "Material updated successfully!", "Updated", JOptionPane.INFORMATION_MESSAGE);
                refreshMaterials();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to update: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actions.add(cancelBtn);
        actions.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Opens the selected document using system default application.
     */
    private void openSelectedFile() {
        CourseMaterialItem item = getSelectedMaterial();
        if (item == null) return;

        try {
            CourseMaterialsLogic.openMaterial(item.getMaterialId());
        } catch (FileNotFoundException fnf) {
            JOptionPane.showMessageDialog(this,
                    "The requested material binary was not found in the database.\n" +
                    "Material: " + item.getTitle() + "\nFile: " + item.getFileName(),
                    "Material Not Found in Database", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not open document: " + ex.getMessage(),
                    "Open Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Downloads/saves a copy of the selected material file.
     */
    private void downloadSelectedFile() {
        CourseMaterialItem item = getSelectedMaterial();
        if (item == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Copy of Material");
        chooser.setSelectedFile(new File(item.getFileName()));

        int choice = chooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File dest = chooser.getSelectedFile();
            if (dest.exists()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "File '" + dest.getName() + "' already exists. Overwrite?",
                        "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try {
                CourseMaterialsLogic.downloadMaterial(item.getMaterialId(), dest);
                JOptionPane.showMessageDialog(this,
                        "File saved successfully to:\n" + dest.getAbsolutePath(),
                        "Download Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to save file: " + ex.getMessage(),
                        "Download Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    /**
     * Confirms and deletes the selected material from database and disk.
     */
    private void deleteSelectedMaterial() {
        CourseMaterialItem item = getSelectedMaterial();
        if (item == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this course material?\n\n" +
                "Title: " + item.getTitle() + "\n" +
                "File: " + item.getFileName() + "\n\n" +
                "This operation cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                CourseMaterialsLogic.deleteMaterial(lecturerId, item.getMaterialId());
                JOptionPane.showMessageDialog(this, "Course material deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshMaterials();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // UI HELPERS & COMPONENT FACTORIES
    // =========================================================================

    private JPanel createWhiteCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                g2.setColor(COLOR_CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));

                g2.setColor(COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 12, 12));

                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                Color paintBg = isEnabled() ? (hovered ? bg.darker() : bg) : new Color(226, 232, 240);
                g2.setColor(paintBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 8, 8));

                // 1px Border if light background
                if (bg.getRed() > 220 && bg.getGreen() > 220 && bg.getBlue() > 220) {
                    g2.setColor(COLOR_BORDER);
                    g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 8, 8));
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(FONT_BUTTON);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        return btn;
    }

    private static String formatDisplayDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "N/A";
        // Format ISO timestamp into clean readable string
        if (rawDate.length() >= 10) {
            return rawDate.substring(0, 10);
        }
        return rawDate;
    }

    // =========================================================================
    // STANDALONE LAUNCHER & TESTING
    // =========================================================================

    /**
     * Standalone main method allowing direct execution from IntelliJ IDEA.
     * Simulates session for Dr. Chinthaka Premachandra (Lecturer ID: 4).
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Set test session
        UserSession.setCurrentSession(new UserSession(
                4,
                "lec_chinthaka",
                "Lecturer",
                "Dr. Chinthaka Premachandra",
                "Senior Lecturer Gr. I",
                "Department of Information and Communication Technology",
                "chinthaka@fot.ruh.ac.lk"
        ));

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TecFAMS - Lecturer Course Materials");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 750);
            frame.setLayout(new BorderLayout());

            Navbar navbar = new Navbar(Navbar.PAGE_MATERIALS);
            CourseMaterials materials = new CourseMaterials();

            navbar.addNavigationListener(pageName -> {
                if (Navbar.PAGE_MATERIALS.equals(pageName)) {
                    materials.refreshMaterials();
                }
            });

            frame.add(navbar, BorderLayout.NORTH);
            frame.add(materials, BorderLayout.CENTER);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
