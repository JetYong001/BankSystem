package views.staffPage.FRONTLINE;

import DAO.AccountDAO;
import views.customerPage.Themeable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

public class Open_Account_Page extends JPanel implements Themeable {
    private final Color ACCENT = new Color(255, 204, 0);
    private final Color ERROR_RED = new Color(231, 76, 60);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);

    private Color inputBg;
    private Color textPrimary;
    private Color borderColor;

    private final JTextField nameField, icField, addressField, phoneField, gmailField;
    private final JPasswordField pinField;
    private final JPanel formContainer;
    private final JLabel title;
    private final ArrayList<JLabel> sectionLabels = new ArrayList<>();

    public Open_Account_Page(boolean isDark) {
        setLayout(new GridBagLayout());

        formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(new EmptyBorder(40, 60, 40, 60));
        formContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        title = new JLabel("Open Current Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(title);
        formContainer.add(Box.createVerticalStrut(30));

        nameField = createStyledField();
        icField = createStyledField();
        addressField = createStyledField();
        phoneField = createStyledField();
        gmailField = createStyledField();
        pinField = createStyledPinField();

        addSection(formContainer, "FULL NAME", nameField);
        addSection(formContainer, "IC NUMBER", icField);
        addSection(formContainer, "ADDRESS", addressField);
        addSection(formContainer, "PHONE NUMBER", phoneField);
        addSection(formContainer, "EMAIL", gmailField);
        addSection(formContainer, "6-DIGIT TRANSACTION PIN", pinField);

        formContainer.add(Box.createVerticalStrut(20));

        JButton btn = createRoundedButton();
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(btn);

        add(formContainer);

        updateTheme(isDark);
    }

    @Override
    public void updateTheme(boolean isDark) {
        Color bgMain;
        Color cardBg;
        Color textSecondary;
        if (isDark) {
            bgMain = new Color(13, 13, 15);
            cardBg = new Color(22, 22, 26);
            inputBg = new Color(30, 30, 35);
            textPrimary = new Color(255, 255, 255);
            textSecondary = new Color(150, 150, 160);
            borderColor = new Color(45, 45, 50);
        } else {
            bgMain = new Color(245, 246, 250);
            cardBg = Color.WHITE;
            inputBg = new Color(248, 249, 252);
            textPrimary = new Color(33, 37, 41);
            textSecondary = new Color(108, 117, 125);
            borderColor = new Color(220, 225, 230);
        }

        setBackground(bgMain);
        formContainer.setBackground(cardBg);
        title.setForeground(textPrimary);

        for (JLabel l : sectionLabels) {
            l.setForeground(textSecondary);
        }

        updateFieldTheme(nameField);
        updateFieldTheme(icField);
        updateFieldTheme(addressField);
        updateFieldTheme(phoneField);
        updateFieldTheme(gmailField);
        updateFieldTheme(pinField);

        repaint();
    }

    private void updateFieldTheme(JTextField f) {
        f.setBackground(inputBg);
        f.setForeground(textPrimary);
    }

    private void handleOpenAccount() {
        String name = nameField.getText().trim();
        String ic = icField.getText().trim();
        String addr = addressField.getText().trim();
        String ph = phoneField.getText().trim();
        String gm = gmailField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();

        if (name.isEmpty() || ic.isEmpty() || pin.isEmpty()) {
            showToast("Required fields are empty", false);
            return;
        }

        if (pin.length() != 6) {
            showToast("PIN must be exactly 6 digits", false);
            return;
        }

        String result = AccountDAO.openCustomerAccount(name, ic, pin, "DEBIT", "CURRENT", addr, ph, gm);

        if (result.contains("|")) {
            showToast("Current Account Created!", true);
            nameField.setText(""); icField.setText(""); addressField.setText("");
            phoneField.setText(""); gmailField.setText(""); pinField.setText("");
        } else {
            showToast(result, false);
        }
    }

    private void addSection(JPanel p, String text, JComponent f) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionLabels.add(l);
        p.add(l);
        p.add(Box.createVerticalStrut(6));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(f);
        p.add(Box.createVerticalStrut(15));
    }

    private JTextField createStyledField() {
        JTextField f = new JTextField() {
            private boolean isFocused = false;
            {
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) { isFocused = true; repaint(); }
                    public void focusLost(FocusEvent e) { isFocused = false; repaint(); }
                });
            }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocused ? ACCENT : borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.dispose();
            }
        };
        f.setOpaque(false);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(500, 42));
        f.setMaximumSize(new Dimension(500, 42));
        f.setBorder(new EmptyBorder(5, 15, 5, 15));
        return f;
    }

    private JPasswordField createStyledPinField() {
        JPasswordField f = new JPasswordField() {
            private boolean isFocused = false;
            {
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) { isFocused = true; repaint(); }
                    public void focusLost(FocusEvent e) { isFocused = false; repaint(); }
                });
            }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocused ? ACCENT : borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.dispose();
            }
        };
        f.setOpaque(false);
        f.setCaretColor(ACCENT);
        f.setPreferredSize(new Dimension(500, 42));
        f.setMaximumSize(new Dimension(500, 42));
        f.setBorder(new EmptyBorder(5, 15, 5, 15));
        return f;
    }

    private JButton createRoundedButton() {
        JButton btn = new JButton("Open Account") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.BLACK);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 3;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(500, 50));
        btn.setMaximumSize(new Dimension(500, 50));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.addActionListener(e -> handleOpenAccount());
        return btn;
    }

    private void showToast(String m, boolean s) {
        new AnimatedToast(m, s, this).display();
    }

    class AnimatedToast extends JWindow {
        private float opacity = 0f;
        private final boolean isSuccess;
        private final String message;
        private final JPanel targetPanel;

        public AnimatedToast(String m, boolean s, JPanel t) {
            this.message = m;
            this.isSuccess = s;
            this.targetPanel = t;
            setSize(400, 50);
            setBackground(new Color(0, 0, 0, 0));
        }

        public void display() {
            try {
                Point loc = targetPanel.getLocationOnScreen();
                setLocation(loc.x + (targetPanel.getWidth() - getWidth()) / 2, loc.y + targetPanel.getHeight() - 100);
            } catch (Exception ignored) {}

            JPanel content = new JPanel(new BorderLayout()) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                    g2.setColor(isSuccess ? SUCCESS_GREEN : ERROR_RED);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                    g2.dispose();
                }
            };
            content.setOpaque(false);
            JLabel lbl = new JLabel(message, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(Color.WHITE);
            content.add(lbl);
            add(content);
            setVisible(true);

            Timer timer = new Timer(15, null);
            final long start = System.currentTimeMillis();
            timer.addActionListener(e -> {
                long el = System.currentTimeMillis() - start;
                if (el < 300) opacity = el / 300f;
                else if (el > 2000) {
                    opacity = Math.max(0, 1 - (el - 2000) / 300f);
                    if (opacity <= 0) {
                        timer.stop();
                        dispose();
                    }
                }
                repaint();
            });
            timer.start();
        }
    }
}