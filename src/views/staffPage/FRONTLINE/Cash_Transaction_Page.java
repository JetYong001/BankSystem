package views.staffPage.FRONTLINE;

import DAO.AccountDAO;
import views.customerPage.Themeable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Cash_Transaction_Page extends JPanel implements Themeable {
    private final Color ACCENT = new Color(255, 204, 0);
    private final Color ERROR_RED = new Color(231, 76, 60);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color HOVER_COLOR = new Color(255, 215, 64);

    private Color bgMain, cardBg, inputBg, textPrimary, textSecondary, borderColor;

    private final JTextField accNumField, amountField, icVerifyField;
    private String selectedTransType = "DEPOSIT";
    private final JPanel depositCard, withdrawCard, formContainer, mainWrapper;
    private final JLabel title, subtitle;
    private final ArrayList<JLabel> sectionLabels = new ArrayList<>();

    public Cash_Transaction_Page(boolean isDark) {
        setLayout(new BorderLayout());
        mainWrapper = new JPanel(new GridBagLayout());
        formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(new EmptyBorder(50, 60, 50, 60));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        title = new JLabel("Cash Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        subtitle = new JLabel("Direct cash deposit or withdrawal via account verification");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitle);

        formContainer.add(headerPanel);
        formContainer.add(Box.createVerticalStrut(45));

        accNumField = createStyledField();
        icVerifyField = createStyledField();
        amountField = createStyledField();

        addSection(formContainer, "CUSTOMER ACCOUNT NUMBER", accNumField);
        addSection(formContainer, "CUSTOMER IC NUMBER (VERIFICATION)", icVerifyField);
        addSection(formContainer, "TRANSACTION AMOUNT (MYR)", amountField);

        JLabel typeLabel = new JLabel("SELECT OPERATION");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionLabels.add(typeLabel);
        formContainer.add(typeLabel);
        formContainer.add(Box.createVerticalStrut(10));

        JPanel cardWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cardWrapper.setOpaque(false);
        cardWrapper.setMaximumSize(new Dimension(540, 95));
        cardWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        depositCard = createTypeCard("DEPOSIT", "Receive cash from customer", "＋");
        withdrawCard = createTypeCard("WITHDRAWAL", "Dispense cash to customer", "－");
        depositCard.setPreferredSize(new Dimension(260, 85));
        withdrawCard.setPreferredSize(new Dimension(260, 85));

        cardWrapper.add(depositCard);
        cardWrapper.add(Box.createHorizontalStrut(20));
        cardWrapper.add(withdrawCard);

        formContainer.add(cardWrapper);
        formContainer.add(Box.createVerticalStrut(40));

        JButton btn = createRoundedButton();
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(btn);

        mainWrapper.add(formContainer);
        add(mainWrapper, BorderLayout.CENTER);

        updateTheme(isDark);
    }

    @Override
    public void updateTheme(boolean isDark) {
        if (isDark) {
            bgMain = new Color(13, 13, 15);
            cardBg = new Color(22, 22, 26);
            inputBg = new Color(30, 30, 35);
            textPrimary = Color.WHITE;
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
        mainWrapper.setBackground(bgMain);
        formContainer.setBackground(cardBg);
        title.setForeground(textPrimary);
        subtitle.setForeground(textSecondary);

        for (JLabel l : sectionLabels) l.setForeground(textSecondary);
        updateFieldTheme(accNumField);
        updateFieldTheme(icVerifyField);
        updateFieldTheme(amountField);
        updateCardSelection();
        repaint();
    }

    private void updateFieldTheme(JTextField f) {
        f.setBackground(inputBg);
        f.setForeground(textPrimary);
    }

    private void handleTransaction() {
        String acc = accNumField.getText().trim();
        String ic = icVerifyField.getText().trim();
        String amt = amountField.getText().trim();

        if (acc.isEmpty() || ic.isEmpty() || amt.isEmpty()) {
            showToast("Missing required fields", false);
            return;
        }

        if (!ic.matches("\\d{12}")) {
            showToast("Invalid IC format (12 digits required)", false);
            return;
        }

        String result = AccountDAO.processJsonTransaction(acc, amt, selectedTransType);

        if (result.equals("SUCCESS")) {
            showToast("Transaction Successful", true);
            accNumField.setText("");
            icVerifyField.setText("");
            amountField.setText("");
        } else {
            showToast(result.replace("_", " "), false);
        }
    }

    private JPanel createTypeCard(String type, String desc, String sym) {
        JPanel card = getJPanel(type);

        JLabel icon = new JLabel(sym, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        icon.setForeground(ACCENT);
        icon.setPreferredSize(new Dimension(50, 85));

        JPanel tp = new JPanel();
        tp.setLayout(new BoxLayout(tp, BoxLayout.Y_AXIS));
        tp.setOpaque(false);
        tp.setBorder(new EmptyBorder(18, 5, 15, 15));

        JLabel nl = new JLabel(type);
        nl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel dl = new JLabel(desc);
        dl.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        tp.add(nl);
        tp.add(dl);

        card.add(icon, BorderLayout.WEST);
        card.add(tp, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                selectedTransType = type;
                updateCardSelection();
            }
        });
        return card;
    }

    private JPanel getJPanel(String type) {
        JPanel card = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(selectedTransType.equals(type) ? ACCENT : borderColor);
                g2.setStroke(new BasicStroke(selectedTransType.equals(type) ? 3f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 15, 15);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return card;
    }

    private void updateCardSelection() {
        Color activeCardBg = (bgMain != null && bgMain.getRed() < 50) ? new Color(40, 40, 48) : new Color(245, 247, 250);
        depositCard.setBackground(selectedTransType.equals("DEPOSIT") ? activeCardBg : cardBg);
        withdrawCard.setBackground(selectedTransType.equals("WITHDRAWAL") ? activeCardBg : cardBg);

        for (JPanel c : new JPanel[]{depositCard, withdrawCard}) {
            JPanel inner = (JPanel) c.getComponent(1);
            inner.getComponent(0).setForeground(textPrimary);
            inner.getComponent(1).setForeground(textSecondary);
        }
        repaint();
    }

    private void addSection(JPanel p, String text, JComponent f) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionLabels.add(l);
        p.add(l);
        p.add(Box.createVerticalStrut(8));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(f);
        p.add(Box.createVerticalStrut(22));
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocused ? ACCENT : borderColor);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 12, 12);
                g2.dispose();
            }
        };
        f.setOpaque(false);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        f.setPreferredSize(new Dimension(540, 48));
        f.setMaximumSize(new Dimension(540, 48));
        f.setBorder(new EmptyBorder(5, 18, 5, 18));
        return f;
    }

    private JButton createRoundedButton() {
        JButton btn = new JButton("Complete Action") {
            private boolean isHovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
                });
            }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isHovered ? HOVER_COLOR : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(20, 20, 20));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 4;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(540, 58));
        btn.setMaximumSize(new Dimension(540, 58));
        btn.addActionListener(e -> handleTransaction());
        return btn;
    }

    private void showToast(String m, boolean s) {
        new AnimatedToast(m, s, this).display();
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
            setSize(320, 50);
            setBackground(new Color(0, 0, 0, 0));
            setAlwaysOnTop(true);
        }

        public void display() {
            try {
                Point p = target.getLocationOnScreen();
                int x = p.x + (target.getWidth() - getWidth()) / 2;
                int y = p.y + (target.getHeight() - getHeight()) / 2;
                setLocation(x, y);
            } catch (Exception e) { setLocationRelativeTo(null); }

            JPanel content = getJPanel();
            add(content);
            setVisible(true);

            Timer timer = new Timer(15, null);
            final long start = System.currentTimeMillis();
            timer.addActionListener(e -> {
                long el = System.currentTimeMillis() - start;
                if (el < 400) {
                    opacity = el / 400f;
                    yMove = (int)(15 * (1 - opacity));
                } else if (el > 1600) {
                    opacity = Math.max(0, 1 - (el - 1600) / 400f);
                    if (opacity <= 0) {
                        timer.stop();
                        dispose();
                    }
                }
                content.repaint();
            });
            timer.start();
        }

        private JPanel getJPanel() {
            JPanel content = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                    g2.setColor(isSuccess ? SUCCESS_GREEN : ERROR_RED);
                    g2.fillRoundRect(0, yMove, getWidth(), getHeight() - 10, 25, 25);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth(message)) / 2;
                    int textY = yMove + ((getHeight() - 10 + fm.getAscent()) / 2) - 2;
                    g2.drawString(message, textX, textY);
                    g2.dispose();
                }
            };
            content.setOpaque(false);
            return content;
        }
    }
}