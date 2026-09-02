package views.staffPage.BACKOFFICE;

import DAO.UserDAO;
import models.User.Staff;
import views.customerPage.Themeable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class Staff_List_Page extends JPanel implements Themeable {
    private final JTable table;
    private final DefaultTableModel model;
    private final JTextField searchField;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JLabel title, searchLabel;
    private final JScrollPane scrollPane;
    private final JPanel header;
    private final JPanel mainContainer;

    private Color bgMain, cardBg, textPrimary, textSecondary, borderColor, headerBg;
    private final Color ACCENT = new Color(255, 204, 0);
    private boolean isDark;

    public Staff_List_Page(boolean isDark) {
        this.isDark = isDark;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 50, 40, 50));

        mainContainer = new JPanel(new BorderLayout(0, 30));
        mainContainer.setOpaque(false);

        header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        title = new JLabel("Staff Directory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        header.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        searchPanel.setOpaque(false);
        searchLabel = new JLabel("Quick Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        searchField = new JTextField() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        searchField.setPreferredSize(new Dimension(280, 45));
        searchField.setBorder(new EmptyBorder(0, 15, 0, 15));
        searchField.setOpaque(false);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        header.add(searchPanel, BorderLayout.EAST);

        mainContainer.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Position", "IC Number", "Join Date"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setRowHeight(65);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) showEditStaffDialog(row);
                }
            }
        });

        setupTableAppearance();

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        mainContainer.add(scrollPane, BorderLayout.CENTER);
        add(mainContainer, BorderLayout.CENTER);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String t = searchField.getText();
                sorter.setRowFilter(t.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + t));
            }
        });

        updateTheme(isDark);
        refreshData();
    }

    private void showEditStaffDialog(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        String id = (String) model.getValueAt(modelRow, 0);
        Staff staff = UserDAO.getAllStaff().stream().filter(s -> s.getStaffID().equals(id)).findFirst().orElse(null);
        if(staff == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Staff Profile", true);
        dialog.setUndecorated(true);
        dialog.setSize(500, 680);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cardBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
                g2.dispose();
            }
        };
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel h = new JLabel("Modify Profile");
        h.setFont(new Font("Segoe UI", Font.BOLD, 28));
        h.setForeground(textPrimary);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(h);
        content.add(Box.createVerticalStrut(35));

        JTextField nameF = createEditField(staff.getFull_name(), false);
        JTextField icF = createEditField(staff.getIcNumber(), false);
        JTextField userF = createEditField(staff.getUsername(), true);
        JTextField phoneF = createEditField(staff.getPhoneNumber(), true);
        JTextField emailF = createEditField(staff.getGmail(), true);

        String[] options = {"Bank Teller", "Branch Manager", "System Administrator", "System Configuration"};
        JComboBox<String> roleC = new JComboBox<>(options);
        roleC.setSelectedItem(staff.getPosition());
        roleC.setPreferredSize(new Dimension(400, 48));
        roleC.setMaximumSize(new Dimension(400, 48));

        addEditSection(content, "FULL NAME (READ-ONLY)", nameF);
        addEditSection(content, "IC NUMBER (READ-ONLY)", icF);
        addEditSection(content, "USERNAME", userF);
        addEditSection(content, "PHONE CONTACT", phoneF);
        addEditSection(content, "EMAIL ADDRESS", emailF);
        addEditSection(content, "ASSIGNED ROLE", roleC);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(400, 55));
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancel = createDialogBtn("Cancel", false);
        cancel.addActionListener(e -> dialog.dispose());

        JButton save = createDialogBtn("Save Changes", true);
        save.addActionListener(e -> {
            String u = userF.getText().trim();
            String p = phoneF.getText().trim();
            String g = emailF.getText().trim();

            if (isValid(u) && isValid(p) && isValid(g)) {
                if(UserDAO.updateFullStaffInfo(id, staff.getFull_name(), staff.getIcNumber(), u, p, g, (String)roleC.getSelectedItem())) {
                    dialog.dispose();
                    refreshData();
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Invalid characters detected or empty fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancel);
        btnPanel.add(save);
        content.add(Box.createVerticalStrut(20));
        content.add(btnPanel);

        dialog.add(content);
        dialog.setBackground(new Color(0,0,0,0));
        dialog.setVisible(true);
    }

    private boolean isValid(String s) {
        if(s.isEmpty()) return false;
        return s.matches("^[a-zA-Z0-9\\s@\\.\\-\\+]+$");
    }

    private JButton createDialogBtn(String t, boolean primary) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(180, 50));
        b.setBackground(primary ? ACCENT : headerBg);
        b.setForeground(primary ? Color.BLACK : textPrimary);
        b.setBorder(BorderFactory.createEmptyBorder());
        return b;
    }

    private JTextField createEditField(String val, boolean editable) {
        JTextField f = new JTextField(val);
        f.setEditable(editable);
        f.setPreferredSize(new Dimension(400, 48));
        f.setMaximumSize(new Dimension(400, 48));
        f.setFont(new Font("Segoe UI", editable ? Font.PLAIN : Font.ITALIC, 15));
        f.setBackground(editable ? headerBg : (isDark ? new Color(40, 40, 48) : new Color(235, 237, 240)));
        f.setForeground(editable ? textPrimary : textSecondary);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return f;
    }

    private void addEditSection(JPanel p, String lab, JComponent c) {
        JLabel l = new JLabel(lab);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(textSecondary);
        p.add(l);
        p.add(Box.createVerticalStrut(8));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(c);
        p.add(Box.createVerticalStrut(20));
    }

    private void setupTableAppearance() {
        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 14));
        h.setPreferredSize(new Dimension(0, 55));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, borderColor));

        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 20, 0, 20));
                c.setBackground(isSelected ? new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 50) : (row % 2 == 0 ? cardBg : bgMain));
                c.setForeground(textPrimary);
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(r);
    }

    @Override
    public void updateTheme(boolean isDark) {
        this.isDark = isDark;
        if (isDark) {
            bgMain = new Color(13, 13, 15); cardBg = new Color(22, 22, 26);
            textPrimary = new Color(255, 255, 255); textSecondary = new Color(160, 160, 175);
            borderColor = new Color(45, 45, 52); headerBg = new Color(32, 32, 38);
        } else {
            bgMain = new Color(245, 246, 252); cardBg = Color.WHITE;
            textPrimary = new Color(33, 37, 41); textSecondary = new Color(108, 117, 125);
            borderColor = new Color(220, 225, 235); headerBg = new Color(242, 244, 248);
        }
        setBackground(bgMain);
        title.setForeground(textPrimary);
        searchLabel.setForeground(textSecondary);
        searchField.setBackground(cardBg);
        searchField.setForeground(textPrimary);
        table.setBackground(cardBg);
        JTableHeader th = table.getTableHeader();
        th.setBackground(headerBg);
        th.setForeground(textSecondary);
        scrollPane.getViewport().setBackground(bgMain);
        repaint();
    }

    public void refreshData() {
        model.setRowCount(0);
        for (Staff s : UserDAO.getAllStaff()) {
            model.addRow(new Object[]{s.getStaffID(), s.getFull_name(), s.getPosition(), s.getIcNumber(), s.getCreatedTime().toString().split("T")[0]});
        }
    }
}