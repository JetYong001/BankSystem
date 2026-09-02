package views.staffPage.STAFF;

import views.customerPage.Themeable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class FinancialCalculator extends JPanel implements Themeable {

    private Color cardBg, inputBg, textPrimary, textSecondary, borderColor;
    private final Color ACCENT_YELLOW = new Color(255, 204, 0);
    private RoundedPanel mainCard;
    private JLabel title, resLabel, totalLabel;
    private final List<JTextField> fields = new ArrayList<>();
    private final String[] placeholders = {
            "Principal Amount (e.g. 50000)",
            "Annual Interest Rate (e.g. 4.5)",
            "Term (Months, e.g. 24)"
    };

    public FinancialCalculator(boolean isDark) {
        setLayout(new GridBagLayout());
        updateTheme(isDark);
        initComponent();
    }

    private void initComponent() {
        removeAll();
        mainCard = new RoundedPanel(35, cardBg);
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setBorder(new EmptyBorder(50, 50, 50, 50));
        mainCard.setPreferredSize(new Dimension(520, 650));

        title = new JLabel("Financial Calculator");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(textPrimary);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        fields.clear();
        for (String placeholder : placeholders) {
            addInputField(placeholder);
        }

        JButton calcBtn = new JButton("Calculate Repayment") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_YELLOW);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };

        calcBtn.setPreferredSize(new Dimension(420, 55));
        calcBtn.setMaximumSize(new Dimension(420, 55));
        calcBtn.setContentAreaFilled(false);
        calcBtn.setBorderPainted(false);
        calcBtn.setFocusPainted(false);
        calcBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calcBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        calcBtn.addActionListener(e -> calculate());

        resLabel = new JLabel("Monthly: RM 0.00");
        resLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        resLabel.setForeground(ACCENT_YELLOW);
        resLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalLabel = new JLabel("Total Interest Payable: RM 0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        totalLabel.setForeground(textSecondary);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainCard.add(title);
        mainCard.add(Box.createRigidArea(new Dimension(0, 40)));
        for (JTextField f : fields) {
            mainCard.add(f);
            mainCard.add(Box.createRigidArea(new Dimension(0, 25)));
        }
        mainCard.add(Box.createRigidArea(new Dimension(0, 10)));
        mainCard.add(calcBtn);
        mainCard.add(Box.createRigidArea(new Dimension(0, 45)));
        mainCard.add(resLabel);
        mainCard.add(Box.createRigidArea(new Dimension(0, 10)));
        mainCard.add(totalLabel);

        add(mainCard);
    }

    private void addInputField(String hint) {
        JTextField tf = new JTextField(hint);
        tf.setBackground(inputBg);
        tf.setForeground(textSecondary);
        tf.setCaretColor(ACCENT_YELLOW);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(0, 18, 0, 18)
        ));
        tf.setMaximumSize(new Dimension(420, 55));
        tf.setPreferredSize(new Dimension(420, 55));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);

        tf.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_YELLOW, 1),
                        BorderFactory.createEmptyBorder(0, 18, 0, 18)
                ));
                if (tf.getText().equals(hint)) {
                    tf.setText("");
                    tf.setForeground(textPrimary);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 1),
                        BorderFactory.createEmptyBorder(0, 18, 0, 18)
                ));
                if (tf.getText().isEmpty()) {
                    tf.setForeground(textSecondary);
                    tf.setText(hint);
                }
            }
        });
        fields.add(tf);
    }

    @Override
    public void updateTheme(boolean isDark) {
        Color bgMain;
        if (isDark) {
            bgMain = new Color(13, 13, 15);
            cardBg = new Color(22, 22, 26);
            inputBg = new Color(32, 32, 38);
            textPrimary = new Color(255, 255, 255);
            textSecondary = new Color(150, 150, 160);
            borderColor = new Color(45, 45, 50);
        } else {
            bgMain = new Color(245, 246, 250);
            cardBg = Color.WHITE;
            inputBg = new Color(245, 245, 247);
            textPrimary = new Color(33, 37, 41);
            textSecondary = new Color(108, 117, 125);
            borderColor = new Color(220, 225, 230);
        }

        setBackground(bgMain);

        if (mainCard != null) {
            mainCard.setBgColor(cardBg);
            title.setForeground(textPrimary);
            resLabel.setForeground(ACCENT_YELLOW);
            totalLabel.setForeground(textSecondary);

            for (int i = 0; i < fields.size(); i++) {
                JTextField f = fields.get(i);
                f.setBackground(inputBg);
                f.setCaretColor(ACCENT_YELLOW);
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 1),
                        BorderFactory.createEmptyBorder(0, 18, 0, 18)
                ));
                f.setForeground(f.getText().equals(placeholders[i]) ? textSecondary : textPrimary);
            }
        }
        repaint();
        revalidate();
    }

    private void calculate() {
        try {
            double p = getVal(0);
            double annualRate = getVal(1);
            double r = annualRate / 100 / 12;
            int n = (int) getVal(2);

            if (p <= 0 || n <= 0) throw new Exception();

            double pmt = (r == 0) ? p / n : (p * r) / (1 - Math.pow(1 + r, -n));
            resLabel.setText(String.format("Monthly: RM %.2f", pmt));
            totalLabel.setText(String.format("Total Interest Payable: RM %.2f", (pmt * n) - p));
        } catch (Exception e) {
            resLabel.setText("Invalid Input Data");
            totalLabel.setText("Please check your numbers");
        }
    }

    private double getVal(int index) {
        String text = fields.get(index).getText();
        if (text.equals(placeholders[index]) || text.isEmpty()) return 0;
        return Double.parseDouble(text);
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }

        public void setBgColor(Color color) {
            this.bgColor = color;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
        }
    }
}