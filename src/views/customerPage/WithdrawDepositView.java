package views.customerPage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import models.User.Customer;
import models.Account.Account;
import DAO.TransferDAO;
import DAO.AccountDAO;
import views.customerPage.Transfer.PinInputDialog;
import views.customerPage.Transfer.ReceiptView;

public class WithdrawDepositView extends JPanel implements Themeable {
    private final JComboBox<AccountItem> accountSelector = new JComboBox<>();
    private final JTextField accField = new JTextField();
    private final JTextField amtField = new JTextField();
    private final JTextField noteField = new JTextField();
    private final JButton nextBtn = new JButton("CONFIRM TRANSACTION");
    private final JButton withdrawTab = new JButton("WITHDRAWAL");
    private final JButton depositTab = new JButton("DEPOSIT");
    private final JPanel formContainer = new JPanel(new GridBagLayout());
    private final JLabel accLabel = new JLabel("Target Savings Account:");
    private final JLabel typeLabel = new JLabel("Select Source Account:");

    private final Customer customer;
    private boolean isDark;
    private String currentMode = "WITHDRAWAL";

    private final Color ACCENT_GOLD = new Color(255, 215, 0);
    private final Color DARK_CARD = new Color(35, 35, 38);

    private static class AccountItem {
        Account account;
        String typeFromJson;
        AccountItem(Account acc) {
            this.account = acc;
            if(AccountDAO.isSavingsAccount(acc.getAccountNum())){
                this.typeFromJson = "SAVINGS";
            }else if(AccountDAO.isCurrentAccount(acc.getAccountNum())){
                this.typeFromJson = "CURRENT";
            }else{
                this.typeFromJson = "FIXED";
            }

        }
        @Override
        public String toString() {
            return "[" + typeFromJson + "] " + account.getAccountNum() + " - RM " + String.format("%.2f", account.getBalance());
        }
    }

    public WithdrawDepositView(Customer customer, boolean isDark) {
        this.customer = customer;
        this.isDark = isDark;
        setLayout(new BorderLayout(0, 40));
        setBorder(new EmptyBorder(40, 60, 40, 60));

        setupNavigation();
        setupLayout();
        refreshAccounts();
        updateMode("WITHDRAWAL");
        updateTheme(isDark);

        nextBtn.addActionListener(e -> handleNext());
    }

    private void refreshAccounts() {
        accountSelector.removeAllItems();
        for (Account acc : customer.getAccounts()) {
            accountSelector.addItem(new AccountItem(acc));
        }
        if (accountSelector.getItemCount() > 0) {
            accountSelector.setSelectedIndex(0);
        }
        accountSelector.revalidate();
        accountSelector.repaint();
    }

    private void setupNavigation() {
        JPanel navWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        navWrapper.setOpaque(false);

        JPanel togglePanel = new JPanel(new GridLayout(1, 2, 0, 0));
        togglePanel.setPreferredSize(new Dimension(500, 50));
        togglePanel.setBackground(new Color(60, 60, 60));

        withdrawTab.setFocusPainted(false);
        depositTab.setFocusPainted(false);
        withdrawTab.setBorder(BorderFactory.createEmptyBorder());
        depositTab.setBorder(BorderFactory.createEmptyBorder());
        withdrawTab.setFont(new Font("Segoe UI", Font.BOLD, 14));
        depositTab.setFont(new Font("Segoe UI", Font.BOLD, 14));

        withdrawTab.addActionListener(e -> updateMode("WITHDRAWAL"));
        depositTab.addActionListener(e -> updateMode("DEPOSIT"));

        togglePanel.add(withdrawTab);
        togglePanel.add(depositTab);
        navWrapper.add(togglePanel);
        add(navWrapper, BorderLayout.NORTH);
    }

    private void updateMode(String mode) {
        this.currentMode = mode;
        boolean isDep = mode.equals("DEPOSIT");
        accLabel.setVisible(isDep);
        accField.setVisible(isDep);
        typeLabel.setText(isDep ? "Transfer From:" : "Withdraw From:");

        withdrawTab.setBackground(isDep ? (isDark ? DARK_CARD : new Color(220, 220, 220)) : ACCENT_GOLD);
        withdrawTab.setForeground(isDep ? (isDark ? Color.WHITE : Color.BLACK) : Color.BLACK);
        depositTab.setBackground(isDep ? ACCENT_GOLD : (isDark ? DARK_CARD : new Color(220, 220, 220)));
        depositTab.setForeground(isDep ? Color.BLACK : (isDark ? Color.WHITE : Color.BLACK));

        revalidate();
        repaint();
    }

    private void setupLayout() {
        formContainer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        addFormField(typeLabel, accountSelector, gbc, 0);
        addFormField(accLabel, accField, gbc, 2);
        addFormField(new JLabel("Amount (RM):"), amtField, gbc, 4);
        addFormField(new JLabel("Note / Description:"), noteField, gbc, 6);

        add(formContainer, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        nextBtn.setPreferredSize(new Dimension(500, 60));
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(nextBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JLabel label, JComponent field, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        formContainer.add(label, gbc);
        gbc.gridy = y + 1;
        gbc.insets = new Insets(0, 0, 25, 0);
        field.setPreferredSize(new Dimension(500, 48));
        formContainer.add(field, gbc);
        gbc.insets = new Insets(10, 0, 5, 0);
    }

    private void handleNext() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        AccountItem selected = (AccountItem) accountSelector.getSelectedItem();
        if (selected == null) return;

        Account src = selected.account;
        double amt;
        try {
            amt = Double.parseDouble(amtField.getText());
            if (amt <= 0) throw new Exception();
        } catch (Exception e) {
            ToastNotification.show(parent, "Please enter a valid amount", true);
            return;
        }

        if (amt > src.getBalance()) {
            ToastNotification.show(parent, "Insufficient balance", true);
            return;
        }

        String target = accField.getText();
        if (currentMode.equals("DEPOSIT")) {
            if (!target.matches("\\d{12}")) {
                ToastNotification.show(parent, "Enter a valid 12-digit account", true);
                return;
            }
            if (!AccountDAO.isSavingsAccount(target)) {
                ToastNotification.show(parent, "Target must be a Savings Account", true);
                return;
            }
        }

        PinInputDialog pinDialog = new PinInputDialog(parent);
        if (pinDialog.showDialog()) {
            String inputPin = pinDialog.getPin();

            String cardNum = "";
            if (src.getCardNumber() != null && src.getCardNumber().length > 0) {
                cardNum = src.getCardNumber()[0].getCardNumber();
            }

            if (AccountDAO.verifyAccountPin(cardNum, inputPin)) {
                String type = currentMode.equals("WITHDRAWAL") ? "Withdrawal" : "Deposit";
                String result = AccountDAO.processJsonTransaction(src.getAccountNum(), String.valueOf(amt), type);

                if ("SUCCESS".equals(result)) {
                    execute(parent, src, amt, accField.getText());
                } else {
                    ToastNotification.show(parent, result, true);
                }
            } else {
                ToastNotification.show(parent, "Invalid PIN", true);
            }
        }
    }

    private void execute(Frame parent, Account src, double amt, String target) {
        String ts = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String refId = (currentMode.equals("WITHDRAWAL") ? "WTH" : "DEP") + (System.currentTimeMillis() / 1000);
        boolean ok = false;

        if (currentMode.equals("WITHDRAWAL")) {
            if (TransferDAO.updateBalance(src.getAccountNum(), -amt)) {
                src.setBalance(src.getBalance() - amt);

                TransferDAO.addRecord(src.getAccountNum(), "WITHDRAWAL", amt, noteField.getText(), refId, ts, "ATM / SELF");
                ToastNotification.show(parent, "Withdrawal Success! Code: " + (new Random().nextInt(9000)+1000), false);
                ok = true;
            }
        } else {
            if (TransferDAO.updateBalance(src.getAccountNum(), -amt) && TransferDAO.updateBalance(target, amt)) {
                src.setBalance(src.getBalance() - amt);
                TransferDAO.addRecord(src.getAccountNum(), "DEPOSIT", amt, "To Savings: " + target, refId, ts, target);
                new ReceiptView(parent, target, amt, new Date(), noteField.getText(), refId, ts).setVisible(true);
                ok = true;
            }
        }

        if (ok) {
            resetForm();
            refreshAccounts();
        } else {
            ToastNotification.show(parent, "Transaction failed to process", true);
        }
    }

    private void resetForm() {
        accField.setText("");
        amtField.setText("");
        noteField.setText("");
    }

    @Override
    public void updateTheme(boolean isDarkMode) {
        this.isDark = isDarkMode;
        setBackground(isDarkMode ? new Color(20, 20, 22) : new Color(250, 250, 252));
        Color lbl = isDarkMode ? ACCENT_GOLD : new Color(50, 50, 50);

        for (Component c : formContainer.getComponents()) {
            if (c instanceof JLabel) c.setForeground(lbl);
        }

        style(accountSelector);
        style(accField);
        style(amtField);
        style(noteField);

        nextBtn.setBackground(ACCENT_GOLD);
        nextBtn.setForeground(Color.BLACK);
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nextBtn.setBorder(BorderFactory.createEmptyBorder());
    }

    private void style(JComponent c) {
        c.setBackground(isDark ? DARK_CARD : Color.WHITE);
        c.setForeground(isDark ? Color.WHITE : Color.BLACK);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isDark ? new Color(60, 60, 60) : new Color(210, 210, 210), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        if(c instanceof JTextField) {
            ((JTextField)c).setCaretColor(ACCENT_GOLD);
        }
    }

    static class ToastNotification extends JDialog {
        public ToastNotification(Frame owner, String message, boolean isError) {
            super(owner);
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0));
            JPanel panel = new JPanel(new BorderLayout(15, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(25, 25, 25, 240));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                    g2.setColor(isError ? new Color(255, 60, 60) : new Color(255, 215, 0));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 15, 15));
                    g2.dispose();
                }
            };
            panel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
            JLabel label = new JLabel(message);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            panel.add(label, BorderLayout.CENTER);
            add(panel);
            pack();
            setLocationRelativeTo(owner);
            new Thread(() -> {
                try {
                    Thread.sleep(2200);
                    for (float i = 1.0f; i > 0; i -= 0.05f) { setOpacity(i); Thread.sleep(15); }
                    dispose();
                } catch (Exception ignored) {}
            }).start();
        }
        public static void show(Frame owner, String message, boolean isError) {
            new ToastNotification(owner, message, isError).setVisible(true);
        }
    }
}