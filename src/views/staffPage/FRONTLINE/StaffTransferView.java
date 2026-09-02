package views.staffPage.FRONTLINE;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.text.SimpleDateFormat;
import models.User.Staff;
import DAO.TransferDAO;
import views.customerPage.Themeable;
import views.customerPage.Transfer.ReceiptView;

public class StaffTransferView extends JPanel implements Themeable {
    private final JTextField senderAccField = new JTextField();
    private final JTextField accField = new JTextField();
    private final JTextField amtField = new JTextField();
    private final JTextField noteField = new JTextField();
    private final JTextField dateField = new JTextField();
    private final JComboBox<String> bankBox = new JComboBox<>(new String[]{"Nexus Bank", "Maybank", "CIMB", "Public Bank", "RHB", "Hong Leong"});
    private final JButton nextBtn = new JButton("EXECUTE TRANSFER");
    private final JPanel formContainer = new JPanel(new GridBagLayout());

    private final Color ACCENT_GOLD = new Color(255, 215, 0);

    public StaffTransferView(Staff staff, boolean isDark) {
        setLayout(new BorderLayout(0, 30));
        setBorder(new EmptyBorder(50, 80, 50, 80));

        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        dateField.setEditable(false);

        nextBtn.addActionListener(e -> handleNext());
        setupLayout();
        updateTheme(isDark);
    }

    private void setupLayout() {
        formContainer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        addFormField("SENDER ACCOUNT NUMBER", senderAccField, gbc, 0);
        addFormField("RECIPIENT BANK", bankBox, gbc, 2);
        addFormField("RECIPIENT ACCOUNT NUMBER", accField, gbc, 4);
        addFormField("TRANSFER AMOUNT (RM)", amtField, gbc, 6);
        addFormField("TRANSACTION DATE", dateField, gbc, 8);
        addFormField("INTERNAL STAFF NOTE", noteField, gbc, 10);

        add(formContainer, BorderLayout.CENTER);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bp.setOpaque(false);
        nextBtn.setPreferredSize(new Dimension(400, 55));
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bp.add(nextBtn);
        add(bp, BorderLayout.SOUTH);
    }

    private void addFormField(String txt, JComponent c, GridBagConstraints gbc, int y) {
        gbc.gridy = y; gbc.insets = new Insets(12, 0, 5, 0);
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formContainer.add(l, gbc);

        gbc.gridy = y + 1; gbc.insets = new Insets(0, 0, 15, 0);
        c.setPreferredSize(new Dimension(400, 45));
        styleComponent(c);
        formContainer.add(c, gbc);
    }

    private void styleComponent(JComponent c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        if (c instanceof JTextField) {
            JTextField t = (JTextField) c;
            t.setCaretColor(ACCENT_GOLD);
            t.setMargin(new Insets(0, 10, 0, 10));
        }
    }

    private void handleNext() {
        Frame p = (Frame) SwingUtilities.getWindowAncestor(this);
        String sAcc = senderAccField.getText().trim();
        String rAcc = accField.getText().trim();
        String bank = (String) bankBox.getSelectedItem();
        String ref = "STF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

        if (!sAcc.matches("\\d{12}")) {
            showToast(p, "Invalid Sender Account Number (12 digits required)", true);
            return;
        }

        try {
            double amt = Double.parseDouble(amtField.getText());
            if (amt <= 0) throw new Exception();

            if (TransferDAO.getAccountBalance(sAcc) < amt) {
                showToast(p, "Transaction Denied: Insufficient Funds", true);
                return;
            }

            if ("Nexus Bank".equals(bank)) {
                if (!rAcc.matches("\\d{12}") || TransferDAO.getAccountBalance(rAcc) <= 0) {
                    showToast(p, "Error: Nexus Recipient Account not found", true);
                    return;
                }
            }

            int confirm = JOptionPane.showConfirmDialog(p,
                    "Authorize Transfer of RM " + String.format("%.2f", amt) + " to " + bank + "?",
                    "Staff Authorization", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (TransferDAO.updateBalance(sAcc, -amt)) {
                    TransferDAO.addRecord(sAcc, "TRANSFER-OUT", amt, "To " + bank + ": " + rAcc, ref, time, rAcc);

                    if ("Nexus Bank".equals(bank)) {
                        TransferDAO.updateBalance(rAcc, amt);
                        TransferDAO.addRecord(rAcc, "TRANSFER-IN", amt, "From Nexus: " + sAcc, ref, time, sAcc);
                    }

                    showToast(p, "Transfer Completed Successfully", false);
                    new ReceiptView(p, rAcc, amt, new Date(), noteField.getText(), ref, time).setVisible(true);
                    reset();
                }
            }
        } catch (Exception e) {
            showToast(p, "Error: Please enter a valid numeric amount", true);
        }
    }

    private void reset() {
        senderAccField.setText("");
        accField.setText("");
        amtField.setText("");
        noteField.setText("");
    }

    private void showToast(Frame f, String m, boolean e) {
        new ToastNotification(f, m, e).setVisible(true);
    }

    public void updateTheme(boolean dark) {
        setBackground(dark ? new Color(25, 25, 28) : new Color(245, 246, 250));

        Color textColor = dark ? ACCENT_GOLD : new Color(44, 62, 80);
        Color fieldBg = dark ? new Color(45, 45, 50) : Color.WHITE;
        Color fieldFg = dark ? Color.WHITE : Color.BLACK;

        for (Component c : formContainer.getComponents()) {
            if (c instanceof JLabel) c.setForeground(textColor);
            if (c instanceof JTextField || c instanceof JComboBox) {
                c.setBackground(fieldBg);
                c.setForeground(fieldFg);
                ((JComponent)c).setBorder(BorderFactory.createLineBorder(dark ? new Color(70, 70, 75) : new Color(210, 210, 210)));
            }
        }

        nextBtn.setBackground(ACCENT_GOLD);
        nextBtn.setForeground(Color.BLACK);
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nextBtn.setBorder(BorderFactory.createEmptyBorder());
    }

    static class ToastNotification extends JDialog {
        private float opacity = 0f;

        public ToastNotification(Frame f, String m, boolean isError) {
            super(f);
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0));

            JPanel p = new JPanel(new BorderLayout(15, 0)) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(30, 30, 30, 240));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                    g2.setColor(isError ? new Color(231, 76, 60) : new Color(46, 204, 113));
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));
                    g2.dispose();
                }
            };

            p.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
            JLabel l = new JLabel(m);
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
            p.add(l, BorderLayout.CENTER);

            add(p);
            pack();
            setLocationRelativeTo(f);

            new Thread(() -> {
                try {
                    for (int i = 0; i <= 10; i++) {
                        setOpacity(i * 0.1f);
                        Thread.sleep(20);
                    }
                    Thread.sleep(2500);
                    for (int i = 10; i >= 0; i--) {
                        setOpacity(i * 0.1f);
                        Thread.sleep(20);
                    }
                    dispose();
                } catch (Exception ex) {}
            }).start();
        }
    }
}