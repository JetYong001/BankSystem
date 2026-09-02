package views;

import DAO.AccountDAO;
import DAO.UserDAO;
import PageManager.switchPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RegisterView extends JPanel {

    private final Image bgImage;
    private final JTextField cardField;
    private final JPasswordField pinField;
    private final JToggleButton agreeCheck;

    private final JTextField userField;
    private final JPasswordField passField;

    private final JComboBox<String> questionBox;
    private final JTextField answerField;

    private final JLabel formTitle, formSubTitle, label1, label2;
    private final JButton continueBtn;

    private final String PLACEHOLDER1 = "Ex. 1111222233334444";
    private final String PLACEHOLDER2 = "Enter 6-digit PIN number";
    private final String PLACEHOLDER_USER = "Create your username";
    private final String PLACEHOLDER_PASS = "Create your password";

    private int currentStep = 1;

    public RegisterView() {
        setLayout(null);
        setBackground(Color.WHITE);
        bgImage = new ImageIcon("src/image1/registerBackground.jpg").getImage();

        int sw = Toolkit.getDefaultToolkit().getScreenSize().width;
        int sh = Toolkit.getDefaultToolkit().getScreenSize().height;

        add(createLeftPanel(sw, sh));

        JButton backBtn = new JButton(new ImageIcon(new ImageIcon("src/image1/back.png").getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH)));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setBounds(sw - 70, 20, 45, 45);
        add(backBtn);

        backBtn.addActionListener(e -> handleBack());

        int formWidth = 480;
        int formX = sw / 2 + (sw / 2 - formWidth) / 2;
        int formY = sh / 2 - 220;

        formTitle = new JLabel("Ready to create an account?");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        formTitle.setBounds(formX, formY, formWidth, 45);
        add(formTitle);

        formSubTitle = new JLabel("Before we create your account, let us verify your access number and pin.");
        formSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formSubTitle.setForeground(new Color(120, 120, 120));
        formSubTitle.setBounds(formX, formY + 55, formWidth, 20);
        add(formSubTitle);

        label1 = createLabel("Card / Access Number", formX, formY + 110);
        add(label1);
        cardField = createRoundTextField(formX, formY + 140, formWidth, PLACEHOLDER1);
        add(cardField);

        label2 = createLabel("PIN", formX, formY + 210);
        add(label2);
        pinField = createRoundPasswordField(formX, formY + 240, formWidth, PLACEHOLDER2);
        add(pinField);

        userField = createRoundTextField(formX, formY + 140, formWidth, PLACEHOLDER_USER);
        userField.setVisible(false);
        add(userField);

        passField = createRoundPasswordField(formX, formY + 240, formWidth, PLACEHOLDER_PASS);
        passField.setVisible(false);
        add(passField);

        String[] questions = {"What is your pet's name?", "What is your mother's maiden name?", "What was your first school?", "In what city were you born?"};
        questionBox = new JComboBox<>(questions);
        questionBox.setBounds(formX, formY + 140, formWidth, 50);
        questionBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        questionBox.setBackground(Color.WHITE);
        questionBox.setVisible(false);
        add(questionBox);

        answerField = createRoundTextField(formX, formY + 240, formWidth, "Enter your answer");
        answerField.setVisible(false);
        add(answerField);

        agreeCheck = new JToggleButton("<html>I agree with the <font color='#0070C0'>TERMS & CONDITIONS</font>...</html>") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 18;
                int y = (getHeight() - size) / 2;
                int x = 90;
                g2.setStroke(new BasicStroke(2));
                g2.setColor(isSelected() ? new Color(39, 158, 107) : new Color(180, 180, 180));
                g2.drawOval(x, y, size, size);
                if (isSelected()) {
                    g2.drawLine(x + 4, y + 10, x + 8, y + 14);
                    g2.drawLine(x + 8, y + 14, x + 16, y + 5);
                }
                g2.dispose();
            }
        };
        agreeCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        agreeCheck.setBounds(formX - 90, formY + 310, formWidth, 30);
        agreeCheck.setFocusPainted(false);
        agreeCheck.setBorderPainted(false);
        agreeCheck.setContentAreaFilled(false);
        add(agreeCheck);

        continueBtn = new JButton("CONTINUE") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(39, 158, 107));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        continueBtn.setContentAreaFilled(false);
        continueBtn.setBorderPainted(false);
        continueBtn.setForeground(Color.WHITE);
        continueBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        continueBtn.setBounds(formX, formY + 360, 200, 50);
        continueBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(continueBtn);

        continueBtn.addActionListener(e -> handleContinue());
    }

    private void handleContinue() {
        if (currentStep == 1) {
            String card = cardField.getText().trim();
            String pin = new String(pinField.getPassword()).trim();

            if (card.equals(PLACEHOLDER1) || pin.equals(PLACEHOLDER2) || card.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all details", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!agreeCheck.isSelected()) {
                JOptionPane.showMessageDialog(this, "Please agree to the Terms & Conditions", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int result = AccountDAO.checkCardForRegistration(card, pin);
            if (result == 2) {
                showStep2();
            } else if (result == 3) {
                JOptionPane.showMessageDialog(this, "This card is already registered to a user.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Card Number or PIN. No record found in accounts.json", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (currentStep == 2) {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword()).trim();

            if (user.isEmpty() || user.equals(PLACEHOLDER_USER) || pass.isEmpty() || pass.equals(PLACEHOLDER_PASS)) {
                JOptionPane.showMessageDialog(this, "Please set your username and password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (UserDAO.isUsernameTaken(user, false)) {
                JOptionPane.showMessageDialog(this, "Username is already taken", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            showStep3();
        } else if (currentStep == 3) {
            String card = cardField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            String q = (String) questionBox.getSelectedItem();
            String a = answerField.getText().trim();

            if (a.isEmpty() || a.equals("Enter your answer")) {
                JOptionPane.showMessageDialog(this, "Please provide an answer", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (UserDAO.completeRegistration(card, user, pass, q, a)) {
                JOptionPane.showMessageDialog(this, "Registration Successful!");
                resetFields();
                switchPage.to("LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, "System error during registration", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showStep2() {
        currentStep = 2;
        formTitle.setText("Setup Login Details");
        formSubTitle.setText("Choose a username and password for your online banking.");
        label1.setText("Username");
        label2.setText("Password");
        cardField.setVisible(false);
        pinField.setVisible(false);
        agreeCheck.setVisible(false);
        userField.setVisible(true);
        passField.setVisible(true);
        continueBtn.setText("NEXT");
        repaint();
    }

    private void showStep3() {
        currentStep = 3;
        formTitle.setText("Secure Your Account");
        formSubTitle.setText("Set a security question for password recovery.");
        label1.setText("Choose a Security Question");
        label2.setText("Your Answer");
        userField.setVisible(false);
        passField.setVisible(false);
        questionBox.setVisible(true);
        answerField.setVisible(true);
        continueBtn.setText("FINISH");
        repaint();
    }

    private void handleBack() {
        if (currentStep == 3) showStep2();
        else if (currentStep == 2) resetToFirstStep();
        else {
            resetFields();
            switchPage.to("LOGIN");
        }
    }

    private void resetToFirstStep() {
        currentStep = 1;
        formTitle.setText("Ready to create an account?");
        formSubTitle.setText("Before we create your account, let us verify your access number and pin.");
        label1.setText("Card / Access Number");
        label2.setText("PIN");
        cardField.setVisible(true);
        pinField.setVisible(true);
        agreeCheck.setVisible(true);
        userField.setVisible(false);
        passField.setVisible(false);
        questionBox.setVisible(false);
        answerField.setVisible(false);
        continueBtn.setText("CONTINUE");
        repaint();
    }

    private void resetFields() {
        resetToFirstStep();
        cardField.setText(PLACEHOLDER1);
        cardField.setForeground(Color.GRAY);
        pinField.setText(PLACEHOLDER2);
        pinField.setForeground(Color.GRAY);
        pinField.setEchoChar((char) 0);
        userField.setText(PLACEHOLDER_USER);
        passField.setText(PLACEHOLDER_PASS);
        answerField.setText("Enter your answer");
        answerField.setForeground(Color.GRAY);
        agreeCheck.setSelected(false);
    }

    private JPanel createLeftPanel(int sw, int sh) {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
                g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 230), getWidth(), 0, new Color(0, 0, 0, 80)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                g2.setColor(Color.WHITE);
                g2.drawString("Nexus Bank | Digital", 40, 60);
                g2.dispose();
            }
        };
        panel.setBounds(0, 0, sw / 2, sh);
        return panel;
    }

    private JLabel createLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(Color.BLACK);
        label.setBounds(x, y, 400, 20);
        return label;
    }

    private JTextField createRoundTextField(int x, int y, int width, String placeholder) {
        JTextField field = new JTextField(placeholder) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(210, 210, 210));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setBounds(x, y, width, 50);
        field.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
        return field;
    }

    private JPasswordField createRoundPasswordField(int x, int y, int width, String placeholder) {
        JPasswordField field = new JPasswordField(placeholder) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(210, 210, 210));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(Color.GRAY);
        field.setBounds(x, y, width, 50);
        field.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        field.setEchoChar((char) 0);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setEchoChar('•');
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (String.valueOf(field.getPassword()).isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                    field.setEchoChar((char) 0);
                }
            }
        });
        return field;
    }
}