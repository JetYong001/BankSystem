package views.staffPage.BACKOFFICE;

import DAO.UserDAO;
import models.User.Staff;
import org.mindrot.jbcrypt.BCrypt;
import views.customerPage.Themeable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.time.LocalDateTime;

public class Add_Staff_Page extends JPanel implements Themeable {

    private Color COLOR_BG, COLOR_CARD, COLOR_TEXT_MAIN, COLOR_TEXT_SUB, COLOR_BORDER;
    private final Color COLOR_GOLD_ACCENT = new Color(255, 204, 0);
    private final Color COLOR_GOLD_HOVER = new Color(230, 184, 0);
    private final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private final Color ERROR_RED = new Color(220, 53, 69);

    private final JPanel mainFormContainer;
    private final JLabel titleLabel, subtitleLabel;
    private final JTextField fullNameField, icNumberField, addressField, phoneField, gmailField, usernameField;
    private final JPasswordField passwordField;
    private final JComboBox<String> roleSelectionBox;

    public Add_Staff_Page(boolean isDark) {
        setLayout(new GridBagLayout());

        mainFormContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD);
                g2.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 25, 25);
                g2.dispose();
            }
        };
        mainFormContainer.setLayout(new BoxLayout(mainFormContainer, BoxLayout.Y_AXIS));
        mainFormContainer.setOpaque(false);
        mainFormContainer.setBorder(new EmptyBorder(45, 70, 45, 70));

        titleLabel = new JLabel("Staff Onboarding");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setAlignmentX(0);

        subtitleLabel = new JLabel("Create internal employee credentials with secure roles");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setAlignmentX(0);

        mainFormContainer.add(titleLabel);
        mainFormContainer.add(Box.createVerticalStrut(8));
        mainFormContainer.add(subtitleLabel);
        mainFormContainer.add(Box.createVerticalStrut(30));

        fullNameField = createModernInputField();
        icNumberField = createModernInputField();
        addressField = createModernInputField();
        phoneField = createModernInputField();
        gmailField = createModernInputField();
        usernameField = createModernInputField();
        passwordField = createModernPasswordField();

        String[] roleOptions = { "Bank Teller", "Branch Manager", "System Administrator", "System Configuration" };
        roleSelectionBox = new JComboBox<>(roleOptions);
        configureRoleSelectionDropdown(roleSelectionBox);

        addLabeledField(mainFormContainer, "FULL NAME", fullNameField);
        addLabeledField(mainFormContainer, "IC / PASSPORT NUMBER", icNumberField);
        addLabeledField(mainFormContainer, "ADDRESS", addressField);
        addLabeledField(mainFormContainer, "PHONE NUMBER", phoneField);
        addLabeledField(mainFormContainer, "EMAIL ADDRESS", gmailField);
        addLabeledField(mainFormContainer, "SYSTEM USERNAME", usernameField);
        addLabeledField(mainFormContainer, "INITIAL PASSWORD", passwordField);
        addLabeledField(mainFormContainer, "ASSIGNED ROLE", roleSelectionBox);

        mainFormContainer.add(Box.createVerticalStrut(20));
        JButton registerButton = createModernSubmitButton();
        registerButton.addActionListener(e -> handleStaffRegistrationProcess());
        mainFormContainer.add(registerButton);

        updateTheme(isDark);
        add(mainFormContainer);
    }

    private void showToast(String m, boolean isError) {
        new AnimatedToast(m, !isError, this).display();
    }

    class AnimatedToast extends JWindow {
        private float opacity = 0f;
        private int yMove = 15;
        private final boolean isSuccess;
        private final String message;
        private final JPanel target;

        public AnimatedToast(String m, boolean s, JPanel t) {
            this.message = m;
            this.isSuccess = s;
            this.target = t;
            setSize(350, 55);
            setBackground(new Color(0, 0, 0, 0));
            setAlwaysOnTop(true);
        }

        public void display() {
            try {
                Point p = target.getLocationOnScreen();
                int x = p.x + (target.getWidth() - getWidth()) / 2;
                int y = p.y + (target.getHeight() - getHeight()) / 2;
                setLocation(x, y);
            } catch (Exception e) {
                setLocationRelativeTo(null);
            }

            JPanel content = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                    g2.setColor(isSuccess ? SUCCESS_GREEN : ERROR_RED);
                    g2.fillRoundRect(0, yMove, getWidth(), getHeight() - 8, 15, 15);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(message)) / 2;
                    int ty = yMove + ((getHeight() - 8 + fm.getAscent()) / 2) - 2;
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
                if (el < 350) {
                    opacity = el / 350f;
                    yMove = (int) (15 * (1 - opacity));
                } else if (el > 1600) {
                    opacity = Math.max(0, 1 - (el - 1600) / 350f);
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

    private void handleStaffRegistrationProcess() {
        String fullName = fullNameField.getText().trim();
        String icNumber = icNumberField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String gmail = gmailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleSelectionBox.getSelectedItem();

        if (fullName.isEmpty() || icNumber.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showToast("Required fields are empty!", true);
            return;
        }

        if(!icNumber.matches("^\\d{12}$")) {
            showToast("Invalid IC number!", true);
            return;
        }

        if (!phone.matches("^[0-9+ -]{8,15}$")) {
            showToast("Invalid phone number!", true);
            return;
        }
        if (!gmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showToast("Invalid email address!", true);
            return;
        }

        String staffID = generateSmartStaffID(role);
        String hashedPass = BCrypt.hashpw(password, BCrypt.gensalt());
        Staff staff = new Staff(icNumber, fullName, LocalDateTime.now(), address, phone, gmail, hashedPass, username, staffID, role);

        if (UserDAO.saveStaff(staff)) {
            showToast("Successfully onboarded: " + staffID, false);
            clearRegistrationForm();
        } else {
            showToast("Critical database error!", true);
        }
    }

    @Override
    public void updateTheme(boolean isDark) {
        if (isDark) {
            COLOR_BG = new Color(14, 14, 17);
            COLOR_CARD = new Color(24, 24, 29);
            COLOR_TEXT_MAIN = new Color(240, 240, 245);
            COLOR_TEXT_SUB = new Color(150, 150, 165);
            COLOR_BORDER = new Color(48, 48, 58);
        } else {
            COLOR_BG = new Color(245, 246, 252);
            COLOR_CARD = Color.WHITE;
            COLOR_TEXT_MAIN = new Color(30, 35, 42);
            COLOR_TEXT_SUB = new Color(110, 117, 125);
            COLOR_BORDER = new Color(225, 230, 238);
        }

        setBackground(COLOR_BG);
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        subtitleLabel.setForeground(COLOR_TEXT_SUB);

        Component[] components = {fullNameField, icNumberField, addressField, phoneField, gmailField, usernameField, passwordField, roleSelectionBox};
        for (Component c : components) {
            c.setBackground(isDark ? new Color(32, 32, 38) : new Color(248, 249, 251));
            c.setForeground(COLOR_TEXT_MAIN);
            if (c instanceof JTextField) ((JTextField) c).setCaretColor(COLOR_GOLD_ACCENT);
        }

        for (Component c : mainFormContainer.getComponents()) {
            if (c instanceof JLabel && c != titleLabel && c != subtitleLabel) {
                c.setForeground(COLOR_TEXT_SUB);
            }
        }
        mainFormContainer.repaint();
    }

    private void addLabeledField(JPanel container, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setAlignmentX(0);
        container.add(label);
        container.add(Box.createVerticalStrut(5));
        component.setAlignmentX(0);
        container.add(component);
        container.add(Box.createVerticalStrut(15));
    }

    private JTextField createModernInputField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(450, 40));
        field.setMaximumSize(new Dimension(450, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(new ModernComponentBorder(), new EmptyBorder(0, 15, 0, 15)));
        return field;
    }

    private JPasswordField createModernPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(450, 40));
        field.setMaximumSize(new Dimension(450, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(new ModernComponentBorder(), new EmptyBorder(0, 15, 0, 15)));
        return field;
    }

    private void configureRoleSelectionDropdown(JComboBox<String> comboBox) {
        comboBox.setPreferredSize(new Dimension(450, 40));
        comboBox.setMaximumSize(new Dimension(450, 40));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(COLOR_TEXT_SUB);
                        g2.setStroke(new BasicStroke(2.5f));
                        int w = getWidth(), h = getHeight();
                        g2.drawLine(w / 2 - 5, h / 2 - 2, w / 2, h / 2 + 3);
                        g2.drawLine(w / 2, h / 2 + 3, w / 2 + 5, h / 2 - 2);
                        g2.dispose();
                    }
                };
                button.setBorder(null);
                button.setContentAreaFilled(false);
                return button;
            }
        });
        comboBox.setBorder(new ModernComponentBorder());
    }

    private JButton createModernSubmitButton() {
        JButton button = new JButton("COMPLETE REGISTRATION") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? COLOR_GOLD_HOVER : COLOR_GOLD_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(new Color(33, 37, 41));
        button.setPreferredSize(new Dimension(450, 48));
        button.setMaximumSize(new Dimension(450, 48));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setAlignmentX(0);
        return button;
    }

    private void clearRegistrationForm() {
        fullNameField.setText(""); icNumberField.setText(""); addressField.setText("");
        phoneField.setText(""); gmailField.setText(""); usernameField.setText("");
        passwordField.setText(""); roleSelectionBox.setSelectedIndex(0);
    }

    private String generateSmartStaffID(String role) {
        String prefix = switch (role) {
            case "Bank Teller" -> "BT";
            case "Branch Manager" -> "BM";
            case "System Administrator" -> "SA";
            case "System Configuration" -> "SC";
            default -> "STF";
        };
        return prefix + "-" + ((int) (Math.random() * 9000) + 1000);
    }

    private class ModernComponentBorder extends javax.swing.border.AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.hasFocus() ? COLOR_GOLD_ACCENT : COLOR_BORDER);
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 12, 12);
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(6, 12, 6, 12); }
    }
}