package views.customerPage;

import models.User.Customer;
import org.mindrot.jbcrypt.BCrypt;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardView extends JPanel implements Themeable {

    private final JLabel title;
    private final JLabel subtitle;
    private final JPanel cardsPanel;
    private final Customer customer;
    private boolean isDarkMode;
    private final Color GOLD = new Color(255, 215, 0);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color ERROR_RED = new Color(231, 76, 60);

    public CardView(Customer customer, boolean isDark) {
        this.customer = customer;
        this.isDarkMode = isDark;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
        header.setOpaque(false);
        title = new JLabel("My Cards");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        subtitle = new JLabel("Manage your virtual and physical cards");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        header.add(title);
        header.add(subtitle);

        cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 25));
        cardsPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        loadCustomerCards();

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        updateTheme(isDark);
    }

    private void loadCustomerCards() {
        cardsPanel.removeAll();
        if (customer == null) return;
        String ownerName = (customer.getFull_name() != null) ? customer.getFull_name().toUpperCase() : "HOLDER";
        int count = 0;
        try {
            String content = new String(Files.readAllBytes(Paths.get("data/accounts.json")));
            String sectionRegex = "(?s)\"customer_id\"\\s*:\\s*\"" + customer.getCustomerID() + "\".*?(\"customer_id\"|$)";
            Matcher sectionMatcher = Pattern.compile(sectionRegex).matcher(content);
            if (sectionMatcher.find()) {
                String customerSection = sectionMatcher.group();
                String cardRegex = "(?s)\\{\\s*\"card_number\"\\s*:\\s*\"(\\d+)\".*?\"card_type\"\\s*:\\s*\"([^\"]+)\".*?\"cvv\"\\s*:\\s*\"(\\d+)\".*?\"expiry_date\"\\s*:\\s*\"([^\"]+)\"(.*?)}";
                Matcher cardMatcher = Pattern.compile(cardRegex).matcher(customerSection);
                while (cardMatcher.find()) {
                    String num = cardMatcher.group(1);
                    String role = cardMatcher.group(2);
                    String cvv = cardMatcher.group(3);
                    String expiry = cardMatcher.group(4);
                    String extra = cardMatcher.group(5);
                    String limit = "0.00";
                    if (extra.contains("\"limit\"")) {
                        Matcher limitMatcher = Pattern.compile("\"limit\"\\s*:\\s*([\\d.]+)").matcher(extra);
                        if (limitMatcher.find()) limit = limitMatcher.group(1);
                    }
                    cardsPanel.add(new CreditCardWidget(ownerName, num, expiry, cvv, role, limit));
                    count++;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        int rows = (int) Math.ceil((double) count / 3);
        cardsPanel.setPreferredSize(new Dimension(1200, Math.max(500, rows * 260)));
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void handleSetLimit(String cardNumber, String role, String currentLimit) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Card Management", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDarkMode ? new Color(30, 30, 30) : Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
                g2.setColor(isDarkMode ? new Color(50, 50, 50) : new Color(220, 220, 220));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 30, 30));
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 35, 30, 35));

        JLabel h = new JLabel("Adjust Spending Limit");
        h.setFont(new Font("Segoe UI", Font.BOLD, 20));
        h.setForeground(GOLD);
        h.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel info = new JLabel(role + " : " + formatCardNum(cardNumber));
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.setForeground(isDarkMode ? Color.LIGHT_GRAY : Color.DARK_GRAY);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField input = getJTextField(currentLimit);

        JLabel pLbl = new JLabel("Enter 6-Digit PIN to Verify:");
        pLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pLbl.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        pLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField pinInput = new JPasswordField();
        pinInput.setMaximumSize(new Dimension(280, 40));
        pinInput.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pinInput.setHorizontalAlignment(JTextField.CENTER);
        pinInput.setBackground(input.getBackground());
        pinInput.setForeground(input.getForeground());
        pinInput.setCaretColor(input.getCaretColor());
        pinInput.setBorder(input.getBorder());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        JButton cancel = new JButton("Cancel");
        JButton save = new JButton("Confirm Update");

        styleBtn(cancel, false);
        styleBtn(save, true);

        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            String pin = new String(pinInput.getPassword());
            if (!pin.matches("^\\d{6}$")) {
                showToast("Invalid PIN Format!", ERROR_RED);
                return;
            }

            if (!verifyCardPin(cardNumber, pin)) {
                showToast("Incorrect PIN!", ERROR_RED);
                return;
            }

            try {
                double val = Double.parseDouble(input.getText());
                if (val < 0) {
                    showToast("Limit cannot be negative!", ERROR_RED);
                    return;
                }
                updateCardLimitInJson(cardNumber, val);
                dialog.dispose();
                showToast("Limit Updated Successfully!", SUCCESS_GREEN);
                loadCustomerCards();
            } catch (Exception ex) { showToast("Invalid Amount!", ERROR_RED); }
        });

        btnPanel.add(cancel);
        btnPanel.add(save);

        panel.add(h); panel.add(Box.createVerticalStrut(8));
        panel.add(info); panel.add(Box.createVerticalStrut(25));
        panel.add(input); panel.add(Box.createVerticalStrut(20));
        panel.add(pLbl); panel.add(Box.createVerticalStrut(8));
        panel.add(pinInput); panel.add(Box.createVerticalStrut(30));
        panel.add(btnPanel);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JTextField getJTextField(String currentLimit) {
        JTextField input = new JTextField(currentLimit);
        input.setMaximumSize(new Dimension(280, 50));
        input.setFont(new Font("Monospaced", Font.BOLD, 24));
        input.setHorizontalAlignment(JTextField.CENTER);
        input.setBackground(isDarkMode ? new Color(40, 40, 40) : new Color(240, 240, 240));
        input.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        input.setCaretColor(isDarkMode ? Color.WHITE : Color.BLACK);
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isDarkMode ? new Color(60, 60, 60) : new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return input;
    }

    private boolean verifyCardPin(String cardNumber, String inputPin) {
        try {
            String path = "data/accounts.json";
            String content = new String(Files.readAllBytes(Paths.get(path)));
            String cardRegex = "(?s)\"card_number\"\\s*:\\s*\"" + cardNumber + "\".*?\"pin_hash\"\\s*:\\s*\"([^\"]+)\"";
            Matcher m = Pattern.compile(cardRegex).matcher(content);
            if (m.find()) {
                String hashed = m.group(1);
                return BCrypt.checkpw(inputPin, hashed);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private String formatCardNum(String num) {
        if (num == null) return "";
        StringBuilder sb = new StringBuilder();
        String clean = num.replace(" ", "");
        for (int i = 0; i < clean.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(" ");
            sb.append(clean.charAt(i));
        }
        return sb.toString();
    }

    private void styleBtn(JButton b, boolean primary) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(12, 25, 12, 25));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (primary) {
            b.setBackground(GOLD);
            b.setForeground(Color.BLACK);
        } else {
            b.setBackground(isDarkMode ? new Color(55, 55, 55) : new Color(215, 215, 215));
            b.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        }
    }

    private void showToast(String message, Color color) {
        final JWindow toast = new JWindow();
        toast.setType(Window.Type.UTILITY);
        toast.setBackground(new Color(0, 0, 0, 0));

        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 245));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 35, 35));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(22, 50, 22, 50));

        JLabel lbl = new JLabel(message);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbl);
        toast.add(p);
        toast.pack();

        Window parent = SwingUtilities.getWindowAncestor(this);
        if (parent == null) return;

        int centerX = parent.getX() + (parent.getWidth() - toast.getWidth()) / 2;
        int centerY = parent.getY() + (parent.getHeight() - toast.getHeight()) / 2;

        toast.setLocation(centerX, centerY);
        toast.setAlwaysOnTop(true);
        toast.setOpacity(0f);
        toast.setVisible(true);

        long startTime = System.currentTimeMillis();
        Timer anim = new Timer(15, null);
        anim.addActionListener(e -> {
            long now = System.currentTimeMillis();
            long elapsed = now - startTime;
            if (elapsed < 400) {
                float alpha = elapsed / 400f;
                toast.setOpacity(alpha);
            } else if (elapsed > 1800 && elapsed < 2600) {
                float alpha = 1f - ((elapsed - 1800) / 800f);
                toast.setOpacity(Math.max(0, alpha));
            } else if (elapsed >= 2600) {
                toast.dispose();
                ((Timer) e.getSource()).stop();
            }
        });
        anim.start();
    }

    private void updateCardLimitInJson(String cardNumber, double limit) {
        try {
            String path = "data/accounts.json";
            String content = new String(Files.readAllBytes(Paths.get(path)));
            String cardBlockRegex = "(?s)(\"card_number\"\\s*:\\s*\"" + cardNumber + "\".*?)}";
            Matcher m = Pattern.compile(cardBlockRegex).matcher(content);
            if (m.find()) {
                String updatedBlock = getString(limit, m);
                Files.writeString(Paths.get(path), content.replaceFirst(Pattern.quote(m.group(0)), Matcher.quoteReplacement(updatedBlock + "\n    }")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static String getString(double limit, Matcher m) {
        String block = m.group(1);
        String updatedBlock;
        if (block.contains("\"limit\"")) {
            updatedBlock = block.replaceFirst("(\"limit\"\\s*:\\s*)[\\d.]+", "$1" + String.format("%.2f", limit));
        } else {
            updatedBlock = block.trim();
            if (updatedBlock.endsWith(",")) updatedBlock = updatedBlock.substring(0, updatedBlock.length()-1);
            updatedBlock += ",\n      \"limit\": " + String.format("%.2f", limit);
        }
        return updatedBlock;
    }

    @Override public void updateTheme(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        Color bg = isDarkMode ? new Color(18, 18, 18) : new Color(248, 249, 252);
        setBackground(bg);
        title.setForeground(isDarkMode ? Color.WHITE : new Color(33, 37, 41));
        subtitle.setForeground(isDarkMode ? new Color(160, 160, 160) : new Color(108, 117, 125));
    }

    private class CreditCardWidget extends JPanel {
        private final String owner;
        private final String number;
        private final String expiry;
        private final String cvv;
        private final String role;
        private final String brand;
        private boolean isHovered = false;

        public CreditCardWidget(String owner, String number, String expiry, String cvv, String role, String limit) {
            this.owner = owner; this.number = formatCardNum(number);
            this.expiry = expiry; this.cvv = cvv;
            this.role = role;
            this.brand = number.startsWith("4") ? "VISA" : "MasterCard";
            setPreferredSize(new Dimension(380, 230));
            setOpaque(false);
            if (!role.equals("DEBIT")) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) { handleSetLimit(number.replace(" ", ""), role, limit); }
                    @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                });
            }
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp; Color accent;
            if (role.equals("CREDIT")) {
                gp = new GradientPaint(0, 0, new Color(35, 35, 35), getWidth(), getHeight(), new Color(65, 50, 15));
                accent = GOLD;
            } else if (role.equals("SUBCARD")) {
                gp = new GradientPaint(0, 0, new Color(25, 45, 65), getWidth(), getHeight(), new Color(15, 20, 30));
                accent = new Color(0, 190, 255);
            } else {
                gp = new GradientPaint(0, 0, new Color(42, 42, 48), getWidth(), getHeight(), new Color(22, 22, 28));
                accent = new Color(180, 180, 190);
            }
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
            if (isHovered) {
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
            }
            g2.setPaint(new GradientPaint(35, 65, new Color(255, 225, 120), 85, 100, new Color(170, 135, 45)));
            g2.fill(new RoundRectangle2D.Float(35, 65, 48, 32, 10, 10));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.ITALIC | Font.BOLD, 18));
            g2.drawString(brand, 35, 45);
            g2.setColor(accent);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(role, getWidth() - g2.getFontMetrics().stringWidth(role) - 25, 45);
            g2.setColor(new Color(245, 245, 245));
            g2.setFont(new Font("Monospaced", Font.BOLD, 19));
            g2.drawString(number, 35, 135);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(210, 210, 210, 160));
            g2.drawString("HOLDER", 35, 185);
            g2.drawString("EXPIRES", 210, 185);
            g2.drawString("CVV", 305, 185);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString(owner.length() > 15 ? owner.substring(0, 15) : owner, 35, 210);
            g2.drawString(expiry, 210, 210);
            g2.drawString(cvv, 305, 210);
            g2.dispose();
        }
    }
}