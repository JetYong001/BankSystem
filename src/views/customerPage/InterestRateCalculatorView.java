/* Derived from Open_Account_Page.java */

package views.customerPage;

import models.InterestRate.InterestRateCalculator;
import models.User.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class InterestRateCalculatorView extends JPanel implements Themeable {
    private final Color ACCENT = new Color(255, 204, 0);
    private final Color ERROR_RED = new Color(231, 76, 60);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);

    private Color bgMain, cardBg, inputBg, textPrimary, textSecondary, borderColor;

    private final JTextField P_field, t_field, r_field;
    private final JPanel formContainer;
    private final JLabel title;
    //private final JLabel subtitle;
    private final ArrayList<JLabel> sectionLabels = new ArrayList<>();
    private final ArrayList<JLabel> descLabels = new ArrayList<>();
    private final ArrayList<JLabel> disabledFields = new ArrayList<>();

    private Customer currentUser;

    public InterestRateCalculatorView(Customer currentUser, boolean isDark) {
        this.currentUser = currentUser;

        setLayout(new GridBagLayout());

        formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(new EmptyBorder(30, 50, 30, 50));
        formContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        title = new JLabel("Interest Rate Calculator");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        //subtitle = new JLabel("Facing shortage in money? NexusBank's Loan is here to help!  :D");
        //subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        //subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(title);
        //formContainer.add(subtitle);
        formContainer.add(Box.createVerticalStrut(25));

        P_field = createStyledField();
        t_field = createStyledField();
        r_field = createStyledField();
        r_field.setText("0.25% per annum");
        r_field.setEnabled(false);   // Disables the 'Interest/Profit Rate' field since it is determined automatically based on the minimum amount

        addSection(formContainer, "Savings amount", "The amount you have saved in your savings account.", P_field);

        // Other fields
        addSection(formContainer, "Interest rate", "The interest rate for your savings account (in NexusBank) is 0.25% by default.", r_field);

        addSection(formContainer, "Years", "The duration of calculating the interest rate.", t_field);

        formContainer.add(Box.createVerticalStrut(8));

        JButton btn = createRoundedButton();
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(btn);

        add(formContainer);

        updateTheme(isDark);
    }

    @Override
    public void updateTheme(boolean isDark) {
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
        //subtitle.setForeground(textSecondary);

        for (JLabel l : sectionLabels) {
            l.setForeground(textSecondary);
        }

        for (JLabel descLabel : descLabels) {
            descLabel.setForeground(textSecondary);
        }

        for (JLabel disabledField : disabledFields) {
            disabledField.setForeground(textSecondary);
        }

        updateFieldTheme(P_field);
        updateFieldTheme(t_field);
        updateFieldTheme(r_field);

        repaint();
    }

    private void updateFieldTheme(JTextField f) {
        f.setBackground(inputBg);
        f.setForeground(textPrimary);
    }

    private void handleOpenAccount() {
        String P_input = P_field.getText(),
                t_input = t_field.getText();

        double P = 0.00, t = 0.00;

        if(P_input.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Sorry, but your savings amount cannot be empty.");
            return;
        }
        else if (t_input.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Sorry, but the number of years cannot be empty.");
            return;
        }
        else {
            try {
                P = Double.parseDouble(P_input);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Sorry, but your savings amount is not numerical.");
                return;
            }

            try {
                t = Double.parseDouble(t_input);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Sorry, but the number of years is not numerical.");
                return;
            }

            try {
                // Calculates the interest rate based on the savings amount and years.
                double interestRate = InterestRateCalculator.calcInterestRate(P, t);

                JOptionPane.showMessageDialog(null, String.format("The interest rate for RM%.2f in %.1f years with an interest rate of 0.25pct is RM%.2f.", P, t, interestRate));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                return;
            }
        }
    }

    private void addSection(JPanel p, String text, String desc, JComponent f) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionLabels.add(l);
        p.add(l);
        // Description
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLabels.add(descLabel);
        p.add(descLabel);

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
                super.paintComponent(g);
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
        f.setPreferredSize(new Dimension(500, 40));
        f.setMaximumSize(new Dimension(500, 40));
        f.setBorder(new EmptyBorder(5, 15, 5, 15));
        return f;
    }

    private JButton createRoundedButton() {
        JButton btn = new JButton("Submit now") {
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
        btn.setPreferredSize(new Dimension(500, 48));
        btn.setMaximumSize(new Dimension(500, 48));
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
            setSize(350, 50);
            setBackground(new Color(0, 0, 0, 0));
        }

        public void display() {
            try {
                Point loc = targetPanel.getLocationOnScreen();
                setLocation(loc.x + (targetPanel.getWidth() - getWidth()) / 2, loc.y + targetPanel.getHeight() - 80);
            } catch (Exception ignored) {}

            JPanel content = getJPanel();
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

        private JPanel getJPanel() {
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
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(Color.WHITE);
            content.add(lbl);
            return content;
        }
    }
}