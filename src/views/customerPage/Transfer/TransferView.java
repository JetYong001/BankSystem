package views.customerPage.Transfer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.text.SimpleDateFormat;
import models.User.Customer;
import models.Account.Account;
import DAO.TransferDAO;
import DAO.AccountDAO;
import DAO.UserDAO;
import views.customerPage.Themeable;

public class TransferView extends JPanel implements Themeable {
    private final JTextField fromAccField = new JTextField();
    private final JTextField accField = new JTextField();
    private final JTextField amtField = new JTextField();
    private final JTextField noteField = new JTextField();
    private final JTextField dateField = new JTextField();
    private final JComboBox<String> bankBox = new JComboBox<>(new String[]{"Nexus Bank", "Maybank", "CIMB", "Public Bank", "RHB", "Hong Leong"});
    private final JButton nextBtn = new JButton("INITIATE TRANSFER");
    private final JPanel formContainer = new JPanel(new GridBagLayout());

    private Customer customer;
    private boolean isDark;

    private final Color ACCENT_GOLD = new Color(255, 215, 0);
    private final Color DARK_BG = new Color(18, 18, 20);
    private final Color DARK_COMP = new Color(28, 28, 32);

    public TransferView(Customer customer, boolean isDark) {
        this.customer = customer;
        this.isDark = isDark;

        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(40, 60, 40, 60));

        fromAccField.setEditable(false);
        fromAccField.setFocusable(false);

        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        dateField.setEditable(false);
        dateField.setFocusable(false);

        nextBtn.addActionListener(e -> handleNext());

        setupLayout();
        updateTheme(isDark);
        loadCurrentAccount();
    }

    private void loadCurrentAccount() {
        fromAccField.setText("No Current Account Found");
        for (Account acc : customer.getAccounts()) {

            if (AccountDAO.isCurrentAccount(acc.getAccountNum())) {
                fromAccField.setText(acc.getAccountNum() + " (Current Account)");
                break;
            }
        }
    }

    private void setupLayout() {
        formContainer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 0);

        addFormField("From Account:", fromAccField, gbc, 0);
        addFormField("Recipient Bank:", bankBox, gbc, 2);
        addFormField("Account Number (12-digits):", accField, gbc, 4);
        addFormField("Transfer Amount (RM):", amtField, gbc, 6);
        addFormField("Transaction Date:", dateField, gbc, 8);
        addFormField("Reference Note:", noteField, gbc, 10);

        gbc.gridy = 12;
        gbc.insets = new Insets(30, 0, 0, 0);
        nextBtn.setPreferredSize(new Dimension(500, 55));
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formContainer.add(nextBtn, gbc);

        add(formContainer);
    }

    private void addFormField(String labelStr, JComponent field, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        JLabel l = new JLabel(labelStr);
        l.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        formContainer.add(l, gbc);

        gbc.gridy = y + 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        field.setPreferredSize(new Dimension(500, 45));
        formContainer.add(field, gbc);
        gbc.insets = new Insets(5, 0, 5, 0);
    }

    private void handleNext() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        String refId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

        String targetAcc = accField.getText();
        String selectedBank = (String) bankBox.getSelectedItem();

        if (!targetAcc.matches("\\d{12}")) {
            ToastNotification.show(parent, "Account must be 12 digits!", true);
            return;
        }

        if ("Nexus Bank".equals(selectedBank) && !AccountDAO.isAccountExists(targetAcc)) {
            ToastNotification.show(parent, "Nexus Bank account not found!", true);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtField.getText());
            if (amount <= 0) throw new Exception();
        } catch (Exception e) {
            ToastNotification.show(parent, "Invalid amount!", true);
            return;
        }

        Account myAcc = null;
        for (Account acc : customer.getAccounts()) {
            if (AccountDAO.isCurrentAccount(acc.getAccountNum())) {
                myAcc = acc;
                break;
            }
        }

        if (myAcc == null || amount > myAcc.getBalance() || AccountDAO.isAccountFrozen(myAcc.getAccountNum())) {
            String msg = (myAcc == null) ? "No current account!" :
                    (AccountDAO.isAccountFrozen(myAcc.getAccountNum()) ? "Account is frozen!" : "Insufficient balance!");
            ToastNotification.show(parent, msg, true);
            return;
        }

        PinInputDialog pinDialog = new PinInputDialog(parent);
        if (pinDialog.showDialog()) {
            String inputPin = pinDialog.getPin();

            if (AccountDAO.verifyAccountPin(myAcc.getAccountNum(), inputPin)) {

                String result = AccountDAO.processJsonTransaction(myAcc.getAccountNum(), String.valueOf(amount), "Transfer");

                if ("SUCCESS".equals(result)) {
                    if ("Nexus Bank".equals(selectedBank)) {
                        TransferDAO.updateBalance(targetAcc, amount);
                    }

                    myAcc.setBalance(myAcc.getBalance() - amount);
                    TransferDAO.addRecord(myAcc.getAccountNum(), "Transfer", amount, noteField.getText(), refId, timestamp, targetAcc);

                    ToastNotification.show(parent, "Transfer Successful!", false);
                    new ReceiptView(parent, targetAcc, amount, new Date(), noteField.getText(), refId, timestamp).setVisible(true);
                    resetForm();
                    triggerGlobalRefresh();
                } else {
                    ToastNotification.show(parent, result, true);
                }
            } else {
                ToastNotification.show(parent, "Invalid Transaction PIN", true);
            }
        }
    }

    private void triggerGlobalRefresh() {
        Customer freshCustomer = UserDAO.getCustomerByUsername(customer.getUsername());
        if (freshCustomer != null) this.customer = freshCustomer;
        refresh();
    }

    private void resetForm() {
        accField.setText("");
        amtField.setText("");
        noteField.setText("");
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        bankBox.setSelectedIndex(0);
    }

    public void updateTheme(boolean isDarkMode) {
        this.isDark = isDarkMode;
        setBackground(isDarkMode ? DARK_BG : new Color(250, 250, 252));

        Color labelColor = isDarkMode ? ACCENT_GOLD : new Color(50, 50, 50);
        for (Component c : formContainer.getComponents()) {
            if (c instanceof JLabel) c.setForeground(labelColor);
        }

        styleComponent(bankBox);
        styleComponent(fromAccField);
        styleComponent(accField);
        styleComponent(amtField);
        styleComponent(noteField);
        styleComponent(dateField);

        fromAccField.setBackground(isDarkMode ? new Color(25, 25, 28) : new Color(245, 245, 245));
        dateField.setBackground(isDarkMode ? new Color(25, 25, 28) : new Color(245, 245, 245));

        nextBtn.setBackground(ACCENT_GOLD);
        nextBtn.setForeground(Color.BLACK);
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nextBtn.setBorder(BorderFactory.createEmptyBorder());
    }

    private void styleComponent(JComponent c) {
        c.setBackground(isDark ? DARK_COMP : Color.WHITE);
        c.setForeground(isDark ? Color.WHITE : Color.BLACK);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isDark ? new Color(60, 60, 60) : new Color(210, 210, 210), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    static class ToastNotification extends JDialog {
        public ToastNotification(Frame owner, String message, boolean isError) {
            super(owner);
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0));
            Color bgColor = isError ? new Color(255, 118, 117) : new Color(0, 184, 148);

            JPanel panel = new JPanel(new BorderLayout(15, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bgColor);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.dispose();
                }
            };
            panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            JLabel label = new JLabel(message);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            panel.add(label, BorderLayout.CENTER);
            add(panel);
            pack();
            if (owner != null) {
                setLocationRelativeTo(owner);
            }
            new Thread(() -> {
                try {
                    Thread.sleep(1800);
                    for (float i = 1.0f; i > 0; i -= 0.05f) { setOpacity(i); Thread.sleep(20); }
                    dispose();
                } catch (Exception ignored) {}
            }).start();
        }
        public static void show(Frame owner, String message, boolean isError) {
            new ToastNotification(owner, message, isError).setVisible(true);
        }
    }
    public void refresh() {
        loadCurrentAccount();
        revalidate();
        repaint();
    }
}