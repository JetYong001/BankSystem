package views.staffPage.BACKOFFICE;

import DAO.AccountDAO;
import views.customerPage.Themeable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Account_Frozen_Page extends JPanel implements Themeable {
    private final Color GOLD = new Color(255, 204, 0);
    private final Color ERROR_RED = new Color(231, 76, 60);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);

    private Color textPrimary;
    private Color textSecondary;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;
    private final JButton actionBtn;
    private final JLabel titleLabel;
    private final JScrollPane scroll;

    public Account_Frozen_Page(boolean isDark) {
        setLayout(new BorderLayout(0, 30));
        setBorder(new EmptyBorder(40, 60, 40, 60));

        JPanel header = new JPanel(new BorderLayout(0, 20));
        header.setOpaque(false);

        titleLabel = new JLabel("ACCOUNT SECURITY CENTER");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchBar.setOpaque(false);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 42));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton searchBtn = createBtn("SEARCH", GOLD, new Color(28, 32, 40));
        searchBtn.addActionListener(e -> loadData(searchField.getText().trim()));

        actionBtn = createBtn("TOGGLE STATUS", ERROR_RED, Color.WHITE);
        actionBtn.setEnabled(false);
        actionBtn.addActionListener(e -> handleToggle());

        searchBar.add(searchField);
        searchBar.add(Box.createHorizontalStrut(12));
        searchBar.add(searchBtn);
        searchBar.add(Box.createHorizontalStrut(10));
        searchBar.add(actionBtn);

        header.add(titleLabel, BorderLayout.NORTH);
        header.add(searchBar, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new String[]{"ACC NO", "CUST ID", "TYPE", "BALANCE", "STATUS"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(55);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getSelectionModel().addListSelectionListener(e -> actionBtn.setEnabled(table.getSelectedRow() != -1));

        scroll = new JScrollPane(table);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        updateTheme(isDark);
        loadData("");
    }

    private void showToast(String m, boolean isError) {
        new AnimatedToast(m, !isError, this).display();
    }

    class AnimatedToast extends JWindow {
        private float opacity = 0f;
        private int yMove = 20;
        private final boolean isSuccess;
        private final String message;
        private final JPanel target;

        public AnimatedToast(String m, boolean s, JPanel t) {
            this.message = m;
            this.isSuccess = s;
            this.target = t;
            setSize(320, 60);
            setBackground(new Color(0, 0, 0, 0));
            setAlwaysOnTop(true);
        }

        public void display() {
            try {
                Point p = target.getLocationOnScreen();
                int x = p.x + (target.getWidth() - getWidth()) / 2;
                int y = p.y + (target.getHeight() - getHeight()) / 2;
                setLocation(x, y + 100);
            } catch (Exception e) { setLocationRelativeTo(null); }

            JPanel content = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                    g2.setColor(isSuccess ? SUCCESS_GREEN : ERROR_RED);
                    g2.fillRoundRect(0, yMove, getWidth(), getHeight() - 10, 20, 20);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(message)) / 2;
                    int ty = yMove + ((getHeight() - 10 + fm.getAscent()) / 2) - 2;
                    g2.drawString(message, tx, ty);
                    g2.dispose();
                }
            };
            content.setOpaque(false);
            add(content);
            setVisible(true);

            Timer timer = new Timer(15, null);
            final long start = System.currentTimeMillis();
            timer.addActionListener(e -> {
                long el = System.currentTimeMillis() - start;
                if (el < 400) {
                    opacity = el / 400f;
                    yMove = (int) (20 * (1 - opacity));
                } else if (el > 1800) {
                    opacity = Math.max(0, 1 - (el - 1800) / 400f);
                    if (opacity <= 0) {
                        timer.stop();
                        dispose();
                    }
                }
                content.repaint();
            });
            timer.start();
        }
    }

    private void loadData(String query) {
        tableModel.setRowCount(0);
        String json = AccountDAO.readJsonFile();
        Pattern blockPattern = Pattern.compile("(?s)\\{[^{]*?\"account_number\"\\s*:\\s*\"([^\"]+)\"[^}]*?}");
        Matcher m = blockPattern.matcher(json);

        while (m.find()) {
            String block = m.group(0);
            String acc = m.group(1);
            String cid = extract(block, "customer_id");
            String type = extract(block, "account_type");
            String bal = extract(block, "balance");
            String stat = extract(block, "status");
            if (stat.isEmpty()) stat = "ACTIVE";
            if (query.isEmpty() || acc.contains(query) || cid.contains(query)) {
                tableModel.addRow(new Object[]{acc, cid, type, "RM " + bal, stat});
            }
        }
    }

    private String extract(String block, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"?([^,\"}]+)\"?");
        Matcher m = p.matcher(block);
        return m.find() ? m.group(1).trim() : "";
    }

    private void handleToggle() {
        int r = table.getSelectedRow();
        String acc = (String) tableModel.getValueAt(r, 0);
        String current = (String) tableModel.getValueAt(r, 4);
        String next = current.equals("ACTIVE") ? "FROZEN" : "ACTIVE";

        if (AccountDAO.updateAccountStatus(acc, next)) {
            loadData(searchField.getText().trim());
            showToast("Account " + acc + " set to " + next, false);
        } else {
            showToast("Failed to update status", true);
        }
    }

    @Override
    public void updateTheme(boolean isDark) {
        Color bgMain = isDark ? new Color(15, 15, 18) : new Color(245, 246, 252);
        Color cardBg = isDark ? new Color(25, 25, 30) : Color.WHITE;
        Color inputBg = isDark ? new Color(35, 35, 42) : new Color(250, 250, 252);
        Color borderColor = isDark ? new Color(50, 50, 60) : new Color(225, 230, 238);
        textPrimary = isDark ? Color.WHITE : new Color(28, 32, 40);
        textSecondary = isDark ? new Color(150, 150, 165) : new Color(110, 117, 125);

        setBackground(bgMain);
        titleLabel.setForeground(textPrimary);
        searchField.setBackground(inputBg);
        searchField.setForeground(textPrimary);
        searchField.setCaretColor(GOLD);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)));

        table.setBackground(cardBg);
        table.setForeground(textPrimary);
        table.setSelectionBackground(isDark ? new Color(45, 45, 52) : new Color(242, 243, 248));
        table.setSelectionForeground(textPrimary);

        table.getTableHeader().setBackground(isDark ? new Color(30, 30, 35) : new Color(245, 246, 250));
        table.getTableHeader().setForeground(textSecondary);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));

        scroll.setBorder(BorderFactory.createLineBorder(borderColor));
        scroll.setBackground(cardBg);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                setBorder(new EmptyBorder(0, 20, 0, 20));
                if (c == 4) {
                    setForeground(v.equals("FROZEN") ? ERROR_RED : SUCCESS_GREEN);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setForeground(isS ? textPrimary : textSecondary);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(renderer);
    }

    private JButton createBtn(String t, Color bg, Color fg) {
        JButton b = new JButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setPreferredSize(new Dimension(140, 42));
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}