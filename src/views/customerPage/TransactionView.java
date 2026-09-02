package views.customerPage;

import models.Account.Account;
import models.User.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionView extends JPanel implements Themeable {
    private static final Color ACCENT = new Color(255, 204, 0);

    private final JTable table;
    private final DefaultTableModel model;
    private final JLabel title;
    private final JLabel subtitle;
    private final RoundedCard tableCard;
    private final JButton statementButton;
    private final Customer customer;

    private Color currentBG;
    private Color currentCardBG;
    private Color currentTextMain;
    private Color currentTextSub;
    private Color currentBorder;
    private Color currentTableBG;

    public TransactionView(Customer customer, boolean isDark) {
        this.customer = customer;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 50, 40, 50));

        title = new JLabel("Transaction History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        subtitle = new JLabel("Review your recent account activity and transfer targets");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        statementButton = new JButton("Generate Monthly Statement");
        statementButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statementButton.setFocusPainted(false);
        statementButton.setBorderPainted(false);
        statementButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        statementButton.addActionListener(e -> generateMonthlyStatement());

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 5));
        headerText.setOpaque(false);
        headerText.add(title);
        headerText.add(subtitle);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(headerText, BorderLayout.WEST);

        JPanel buttonWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 18));
        buttonWrap.setOpaque(false);
        buttonWrap.add(statementButton);
        header.add(buttonWrap, BorderLayout.EAST);

        String[] cols = {"Ref ID", "Date", "Time", "Type", "Related Acc", "Details", "Amount (RM)"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        tableCard = new RoundedCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        loadData(customer);
        updateTheme(isDark);
    }

    private void setupCustomRenderers() {
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String type = table.getValueAt(row, 3).toString().toUpperCase();
                boolean isIncome = type.contains("DEPOSIT") ||
                        type.contains("IN") ||
                        type.contains("RECEIVED") ||
                        type.contains("REFUND");

                if (isIncome) {
                    l.setText("+ " + value);
                    l.setForeground(new Color(46, 204, 113));
                } else {
                    l.setText("- " + value);
                    l.setForeground(new Color(231, 76, 60));
                }

                l.setFont(new Font("Segoe UI", Font.BOLD, 14));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBackground(isSelected ? table.getSelectionBackground() : currentTableBG);
                return l;
            }
        });

        DefaultTableCellRenderer center = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBackground(isSelected ? table.getSelectionBackground() : currentTableBG);
                l.setForeground(currentTextMain);
                return l;
            }
        };

        for (int i = 0; i < 6; i++) {
            if (i == 5) continue;
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

    public void loadData(Customer customer) {
        model.setRowCount(0);
        try {
            File file = new File("data/transactions.json");
            if (!file.exists() || customer == null) return;

            String content = Files.readString(file.toPath());

            Pattern p = Pattern.compile("\\{[^}]+\\}");
            Matcher m = p.matcher(content);

            while (m.find()) {
                String entry = m.group();
                String accNum = getValue(entry, "acc_num");

                boolean isMine = false;
                for (Account acc : customer.getAccounts()) {
                    if (acc.getAccountNum().equals(accNum)) {
                        isMine = true;
                        break;
                    }
                }

                if (isMine) {
                    String refID = getValue(entry, "reference_id");
                    if (refID.isEmpty()) continue;

                    model.addRow(new Object[]{
                            refID,
                            getValue(entry, "date"),
                            getValue(entry, "timestamp"),
                            getValue(entry, "type").toUpperCase(),
                            getValue(entry, "related_acc").isEmpty() ? "N/A" : getValue(entry, "related_acc"),
                            getValue(entry, "details").isEmpty() ? "-" : getValue(entry, "details"),
                            String.format("%.2f", Double.parseDouble(getValue(entry, "amount")))
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getValue(String entry, String key) {
        String searchKey = "\"" + key + "\"";
        int keyPos = entry.indexOf(searchKey);
        if (keyPos == -1) return "";

        int colon = entry.indexOf(":", keyPos);
        int startQuote = entry.indexOf("\"", colon);

        if (startQuote != -1 && startQuote < entry.indexOf(",", colon) && startQuote < entry.indexOf("}", colon)) {
            int endQuote = entry.indexOf("\"", startQuote + 1);
            if (endQuote != -1) return entry.substring(startQuote + 1, endQuote);
        }

        int comma = entry.indexOf(",", colon);
        int brace = entry.indexOf("}", colon);
        int end = (comma != -1 && (brace == -1 || comma < brace)) ? comma : brace;
        if (end == -1) end = entry.length();

        return entry.substring(colon + 1, end).trim().replaceAll("[\"\\s]", "");
    }

    private void generateMonthlyStatement() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No records found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String accNum = customer.getAccounts().get(0).getAccountNum();
        StatementDialog dialog = new StatementDialog((Frame) SwingUtilities.getWindowAncestor(this), accNum, this.model);
        dialog.setVisible(true);
    }

    @Override
    public void updateTheme(boolean isDarkMode) {
        if (isDarkMode) {
            currentBG = new Color(18, 18, 20);
            currentCardBG = new Color(28, 28, 32);
            currentTextMain = Color.WHITE;
            currentTextSub = new Color(150, 150, 150);
            currentBorder = new Color(255, 255, 255, 15);
            currentTableBG = new Color(32, 32, 35);
        } else {
            currentBG = new Color(245, 246, 250);
            currentCardBG = Color.WHITE;
            currentTextMain = new Color(40, 40, 40);
            currentTextSub = new Color(110, 110, 110);
            currentBorder = new Color(220, 220, 220);
            currentTableBG = Color.WHITE;
        }

        setBackground(currentBG);
        title.setForeground(currentTextMain);
        subtitle.setForeground(currentTextSub);
        table.setBackground(currentTableBG);
        table.setForeground(currentTextMain);
        table.setSelectionBackground(new Color(255, 215, 0, 80));
        table.getTableHeader().setBackground(isDarkMode ? new Color(45, 45, 48) : new Color(235, 235, 240));
        table.getTableHeader().setForeground(isDarkMode ? ACCENT : Color.BLACK);
        statementButton.setBackground(ACCENT);
        statementButton.setForeground(Color.BLACK);

        setupCustomRenderers();
        repaint();
    }

    private class RoundedCard extends JPanel {
        private RoundedCard() { setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentCardBG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
            g2.setColor(currentBorder);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 30, 30));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}