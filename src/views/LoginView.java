package views;

import DAO.UserDAO;
import PageManager.switchPage;
import views.customerPage.Customer_Dashboard;
import views.staffPage.Staff_Dashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;

public class LoginView extends JPanel {

    private final Color GOLD = new Color(255, 204, 0);
    private final boolean isStaffSystem;
    private float rippleSize = 0;
    private final Point rippleCenter = new Point(0, 0);
    private float regRippleSize = 0;
    private boolean isRegHovered = false;

    private String currentLoginUsername;
    private JPasswordField pwdFieldReference;
    private JTextField userField;


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ImageIcon bgIcon = new ImageIcon(isStaffSystem ? "src/image1/test.jpeg" : "src/image1/CusLoginBackground.jpg");
        g.drawImage(bgIcon.getImage(), 0, 0, getWidth(), getHeight(), null);
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.fillRect(0, getHeight(), getWidth(), 110);
    }

    public LoginView(boolean isStaffSystem) {
        this.isStaffSystem = isStaffSystem;
        setLayout(null);

        int sw = Toolkit.getDefaultToolkit().getScreenSize().width;
        int sh = Toolkit.getDefaultToolkit().getScreenSize().height;


        setFocusable(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        String titleText = isStaffSystem ? "NexusBank  |  Staff System" : "NexusBank  |  Digital";
        JLabel logo = new JLabel(titleText);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 35));
        logo.setForeground(Color.WHITE);
        logo.setBounds(40, 30, 500, 50);
        add(logo);

        if (!isStaffSystem) {
            add(createRegisterButton(sw));
        }

        JButton buttonClose = switchPage.createCloseButton();
        add(buttonClose);

        JLabel slogan = new JLabel(isStaffSystem ? "Staff Login" : "Humanising Financial Services", SwingConstants.CENTER);
        slogan.setFont(new Font("Arial", Font.BOLD, 80));
        slogan.setForeground(Color.WHITE);
        slogan.setBounds(0, sh / 2 - 220, sw, 110);
        add(slogan);

        add(createLoginBar(sw, sh));
        add(createForgetPwdLabel(sw, sh));

        if (!isStaffSystem) {
            createBottomNav(this, sw, sh);
        }
    }

    private JPanel createLoginBar(int sw, int sh) {
        JPanel bar = getJPanel(sw, sh);

        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 23));
        userIcon.setForeground(new Color(150, 150, 150));
        userIcon.setBounds(20, 18, 30, 40);
        bar.add(userIcon);

        this.userField = getJTextField();
        bar.add(this.userField);

        this.userField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void resetStyle() {
                bar.setBorder(null);
                userField.setForeground(Color.BLACK);
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                resetStyle();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                resetStyle();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                resetStyle();
            }
        });

        JLabel loginIcon = new JLabel("\uD83D\uDD12");
        loginIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 23));
        loginIcon.setForeground(new Color(150, 150, 150));
        loginIcon.setBounds(375, 18, 30, 40);
        bar.add(loginIcon);

        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setBounds(365, 0, 150, 70);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        final Timer rt = new Timer(15, null);
        rt.addActionListener(a -> {
            rippleSize += 10;
            if (rippleSize > 250) {
                rt.stop();
            }
            bar.repaint();
        });

        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            boolean exists = UserDAO.isUsernameTaken(username,isStaffSystem);

            if (!exists) {
                String errorMsg = isStaffSystem ? "No such staff " : "No such customer";
                showError(bar, userField, errorMsg);
            } else {
                showPasswordPopup(username);
            }
        });
        bar.add(loginBtn);
        return bar;
    }

    private JPanel getJPanel(int sw, int sh) {
        JPanel bar = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Shape fullBar = new RoundRectangle2D.Float(0, 0, w, h, 20, 20);
                g2.setClip(new Rectangle(0, 0, w - 150, h));
                g2.setColor(Color.WHITE);
                g2.fill(fullBar);
                g2.setClip(new Rectangle(w - 150, 0, 150, h));
                g2.setColor(GOLD);
                g2.fill(fullBar);
                if (rippleSize > 0) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, 0.5f - rippleSize / 600f)));
                    g2.setColor(Color.WHITE);
                    g2.fill(new Ellipse2D.Float(rippleCenter.x - rippleSize / 2, rippleCenter.y - rippleSize / 2, rippleSize, rippleSize));
                }
                g2.dispose();
            }
        };
        bar.setBounds(sw / 2 - 250, sh / 2 - 60, 500, 70);
        bar.setOpaque(false);
        return bar;
    }

    private static JTextField getJTextField() {
        String placeholder = "Username";
        JTextField userField = new JTextField(placeholder);
        userField.setBorder(null);
        userField.setOpaque(false);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        userField.setForeground(Color.GRAY);
        userField.setBounds(65, 15, 280, 40);
        userField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (userField.getText().equals(placeholder)) {
                    userField.setText("");
                    userField.setForeground(Color.BLACK);
                } else {
                    SwingUtilities.invokeLater(() -> userField.setCaretPosition(userField.getText().length()));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (userField.getText().trim().isEmpty()) {
                    userField.setText(placeholder);
                    userField.setForeground(Color.GRAY);
                }
            }
        });
        return userField;
    }

    private JLabel createForgetPwdLabel(int sw, int sh) {
        JLabel forgetPwd = new JLabel("Forgot Password?", SwingConstants.CENTER);
        forgetPwd.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        forgetPwd.setForeground(new Color(255, 255, 255, 200));
        forgetPwd.setBounds(sw / 2 - 150, sh / 2 + 30, 300, 30);
        forgetPwd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgetPwd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                forgetPwd.setForeground(GOLD);
                Map<AttributedCharacterIterator.Attribute, Object> attributes = new HashMap<>(forgetPwd.getFont().getAttributes());
                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                forgetPwd.setFont(forgetPwd.getFont().deriveFont(attributes));
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                handleForgetPassword();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgetPwd.setForeground(new Color(255, 255, 255, 200));
                forgetPwd.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            }
        });
        return forgetPwd;
    }

    private JPanel createRegisterButton(int sw) {
        JPanel btn = getJPanelForRegister(sw);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel txt = new JLabel("REGISTER NOW", SwingConstants.CENTER);
        txt.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txt.setForeground(Color.WHITE);
        txt.setBounds(0, 0, 200, 50);
        btn.add(txt);
        final Timer anim = new Timer(15, e -> {
            regRippleSize += 8;
            if (regRippleSize > Math.max(btn.getWidth(), btn.getHeight()) * 2) {
                regRippleSize = 0;
                ((Timer) e.getSource()).stop();
            }
            btn.repaint();
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchPage.to("REGISTER");
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                isRegHovered = true;
                txt.setForeground(Color.BLACK);
                regRippleSize = 0;
                anim.restart();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                isRegHovered = false;
                txt.setForeground(Color.WHITE);
                btn.repaint();
            }
        });
        return btn;
    }

    private JPanel getJPanelForRegister(int sw) {
        JPanel btn = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                if (isRegHovered) {
                    g2.setColor(GOLD);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 20, 20);
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
                if (regRippleSize > 0) {
                    float alpha = Math.max(0, 0.5f - regRippleSize / 400f);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g2.setColor(Color.WHITE);
                    g2.fill(new Ellipse2D.Float((w / 2f) - regRippleSize / 2f, (h / 2f) - regRippleSize / 2f, regRippleSize, regRippleSize));
                }
                g2.dispose();
            }
        };
        btn.setBounds(sw - 300, 30, 200, 50);
        return btn;
    }

    private void createBottomNav(JPanel pane, int sw, int sh) {
        String[] labels = {"Scam Alert", "Accounts", "Cards", "Loans & Financing", "Insurance", "Wealth", "Financial Relief", "Online Trading"};
        String[] icons = {"🛡️", "🏦", "💳", "💰", "🩺", "🌱", "🤝", "📈"};
        int itemW = 120, startX = (sw - (itemW * labels.length)) / 2, baseY = sh - 95;
        for (int i = 0; i < labels.length; i++) {
            JPanel box = new JPanel(null);
            box.setOpaque(false);
            box.setBounds(startX + (i * itemW), baseY, itemW, 95);
            JLabel ico = new JLabel(icons[i], SwingConstants.CENTER);
            ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
            ico.setForeground(Color.WHITE);
            ico.setBounds(0, 10, itemW, 50);
            JLabel txt = new JLabel(labels[i], SwingConstants.CENTER);
            txt.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            txt.setForeground(Color.WHITE);
            txt.setBounds(0, 55, itemW, 20);
            box.add(ico);
            box.add(txt);
            pane.add(box);
            final Timer a = new Timer(5, null);
            MouseAdapter ma = new MouseAdapter() {
                double o = 0;
                @Override
                public void mouseEntered(MouseEvent e) {
                    ico.setForeground(GOLD);
                    txt.setForeground(Color.WHITE);
                    a.stop();
                    for (ActionListener al : a.getActionListeners()) a.removeActionListener(al);
                    a.addActionListener(ev -> {
                        if (o < 10) { o += 1; box.setLocation(box.getX(), (int) (baseY - o)); } else a.stop();
                    });
                    a.start();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    ico.setForeground(Color.WHITE);
                    txt.setForeground(Color.WHITE);
                    a.stop();
                    for (ActionListener al : a.getActionListeners()) a.removeActionListener(al);
                    a.addActionListener(ev -> {
                        if (o > 0) { o -= 1; box.setLocation(box.getX(), (int) (baseY - o)); } else a.stop();
                    });
                    a.start();
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    String target = txt.getText();

                    switch (target) {
                        case "Scam Alert":
                            PageManager.switchPage.to("SCAM_ALERT");
                            break;
                        case "Accounts":
                            PageManager.switchPage.to("ACCOUNTS_INFO");
                            break;
                        case "Cards":
                            PageManager.switchPage.to("CARDS_INFO");
                            break;
                        case "Loans & Financing":
                            PageManager.switchPage.to("LOAN_FINANCING");
                            break;
                        case "Insurance":
                            PageManager.switchPage.to("INSURANCE_INFO");
                            break;
                        case "Wealth":
                            PageManager.switchPage.to("WEALTH_INFO");
                            break;
                        case "Financial Relief":
                            PageManager.switchPage.to("FINANCIAL_RELIEF");
                            break;
                        case "Online Trading":
                            PageManager.switchPage.to("ONLINE_TRADING");
                            break;
                        default:
                            PageManager.switchPage.to("COMING_SOON");
                            break;
                    }
                }
            };
            box.addMouseListener(ma);
        }
    }

    private void showError(JPanel bar, JTextField field, String message) {
        if (bar != null && field != null) {
            bar.setBorder(BorderFactory.createLineBorder(new Color(255, 80, 80), 2));
            field.setForeground(new Color(255, 80, 80));
        }
        showModernErrorDialog(message);
    }

    private void showModernErrorDialog(String message) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setSize(420, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setLayout(null);
        JPanel panel = getErrorJPanel(message, dialog);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JPanel getErrorJPanel(String message, JDialog dialog) {
        JPanel panel = getJPanel();

        JLabel icon = new JLabel("⚠");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        icon.setForeground(new Color(255, 80, 80));
        icon.setBounds(30, 40, 60, 60);
        panel.add(icon);

        JLabel text = new JLabel(message);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        text.setForeground(Color.WHITE);
        text.setBounds(100, 50, 280, 40);
        panel.add(text);

        JButton okBtn = getJButtonOk();
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);
        return panel;
    }

    private JPanel getJPanel() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(25, 25, 30, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
                g2.dispose();
            }
        };
        panel.setBounds(0, 0, 420, 180);
        panel.setOpaque(false);
        return panel;
    }

    private JButton getJButtonOk() {
        JButton okBtn = new JButton("OK") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 80, 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        okBtn.setForeground(Color.WHITE);
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okBtn.setBorderPainted(false);
        okBtn.setFocusPainted(false);
        okBtn.setContentAreaFilled(false);
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.setBounds(150, 110, 120, 35);
        return okBtn;
    }

    private void showPasswordPopup(String username) {
        this.currentLoginUsername = username;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setSize(430, 540);
        dialog.setLocationRelativeTo(this);

        dialog.setShape(new RoundRectangle2D.Double(0, 0, 430, 540, 40, 40));

        JPanel root = getRoot();
        root.setPreferredSize(new Dimension(430, 540));
        dialog.setContentPane(root);

        JPanel avatar = getAvatarPanel();
        avatar.setBounds(155, 90, 130, 130);

        JLabel close = new JLabel("x", SwingConstants.CENTER);
        close.setFont(new Font("Segoe UI", Font.BOLD, 22));
        close.setForeground(new Color(200, 200, 200));
        close.setBounds(385, 10, 35, 35);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dialog.dispose(); }
            public void mouseEntered(MouseEvent e) { close.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { close.setForeground(new Color(200, 200, 200)); }
        });


        JLabel hello = new JLabel("Hello, " + username + "!", SwingConstants.CENTER);
        hello.setFont(new Font("Segoe UI", Font.BOLD, 26));
        hello.setForeground(Color.WHITE);
        hello.setBounds(0, 240, 440, 40);

        JPanel pwdContainer = getPasswordContainer();
        JButton loginBtn = getJButtonForPassword(dialog);

        root.add(close);
        root.add(avatar);
        root.add(hello);
        root.add(pwdContainer);
        root.add(loginBtn);

        dialog.setVisible(true);
    }

    private JPanel getRoot() {
        JPanel root = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Shape round = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setClip(round);

                g2.setColor(new Color(20, 20, 25));
                g2.fill(round);

                Image img = new ImageIcon("src/image1/test.png").getImage();
                g2.drawImage(img, 0, 0, getWidth(), 160, null);

                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, getWidth(), 160);

                GradientPaint gp = new GradientPaint(0, 120, new Color(0,0,0,0), 0, 180, new Color(20,20,25));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 180);

                g2.setClip(null);
                g2.setColor(new Color(255,255,255,40));
                g2.draw(round);

                g2.dispose();
            }
        };
        root.setBounds(0, 0, 440, 540);
        return root;
    }

    private JPanel getAvatarPanel() {
        String userID = UserDAO.getUserIDByUsername(currentLoginUsername, isStaffSystem);
        String avatarPath = UserDAO.getAvatar(userID);
        final Image avatarImg = !avatarPath.isEmpty()
                ? new ImageIcon(avatarPath).getImage()
                : null;
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GOLD);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(30, 30, 35));
                g2.fillOval(3, 3, getWidth() - 6, getHeight() - 6);
                if (avatarImg != null) {
                    g2.setClip(new Ellipse2D.Float(6, 6, getWidth() - 12, getHeight() - 12));
                    g2.drawImage(avatarImg, 6, 6, getWidth() - 12, getHeight() - 12, null);
                } else {
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 40));
                    String initial = currentLoginUsername.substring(0, 1).toUpperCase();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(initial, (getWidth()-fm.stringWidth(initial))/2, getHeight()/2 + 15);
                }
                g2.dispose();
            }
        };
        avatarPanel.setOpaque(false);
        return avatarPanel;
    }

    private JPanel getPasswordContainer() {
        JPanel pwdContainer = getPanel();
        pwdContainer.setBounds(65, 310, 310, 55);

        JLabel lockIcon = new JLabel("🔒");
        lockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lockIcon.setForeground(new Color(180, 180, 180));
        lockIcon.setBounds(20, 0, 30, 55);
        pwdContainer.add(lockIcon);

        this.pwdFieldReference = getJPasswordField();
        pwdContainer.add(this.pwdFieldReference);
        return pwdContainer;
    }

    private JPanel getPanel() {
        JPanel pwdContainer = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 45, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        pwdContainer.setOpaque(false);
        return pwdContainer;
    }

    private JPasswordField getJPasswordField() {
        JPasswordField pwdField = new JPasswordField();
        pwdField.setBounds(55, 0, 240, 55);
        pwdField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pwdField.setBorder(null);
        pwdField.setOpaque(false);
        pwdField.setText("Input your password here");
        pwdField.setForeground(new Color(150, 150, 150));
        pwdField.setCaretColor(GOLD);
        pwdField.setEchoChar((char) 0);
        pwdField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(pwdField.getPassword()).equals("Input your password here")) {
                    pwdField.setText("");
                    pwdField.setEchoChar('●');
                    pwdField.setForeground(Color.WHITE);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (String.valueOf(pwdField.getPassword()).isEmpty()) {
                    pwdField.setText("Input your password here");
                    pwdField.setEchoChar((char) 0);
                    pwdField.setForeground(new Color(150, 150, 150));
                }
            }
        });
        return pwdField;
    }

    private JButton getJButtonForPassword(JDialog dialog) {
        JButton loginBtn = new JButton("LOGIN →") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GOLD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        loginBtn.setContentAreaFilled(false);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setBounds(110, 440, 220, 50);
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        loginBtn.addActionListener(e -> {
            String inputPassword = new String(pwdFieldReference.getPassword());

            if (inputPassword.equals("Input your password here")) {
                inputPassword = "";
            }

            boolean isPasswordCorrect = UserDAO.login(this.currentLoginUsername, inputPassword, this.isStaffSystem);

            if (isPasswordCorrect) {
                String userPlaceholder = isStaffSystem ? "Staff ID" : "Username";
                userField.setText(userPlaceholder);
                userField.setForeground(Color.GRAY);
                dialog.dispose();
                if (isStaffSystem) {
                    String position = UserDAO.getStaffPosition(currentLoginUsername);
                    switchPage.container.add(new Staff_Dashboard(currentLoginUsername, position), "STAFF_DASHBOARD");
                    switchPage.to("STAFF_DASHBOARD");
                } else {
                    switchPage.container.add(new Customer_Dashboard(currentLoginUsername), "CUSTOMER_DASHBOARD");
                    switchPage.to("CUSTOMER_DASHBOARD");
                }
            } else {
                showModernErrorDialog("Incorrect Password");
            }
        });
        return loginBtn;
    }

    private void handleForgetPassword() {
        String username = showModernInputDialog("Reset Password",
                "Enter your " + (isStaffSystem ? "Staff ID:" : "Username:"));

        if (username == null || username.trim().isEmpty()) return;
        username = username.trim();


        boolean exists = isStaffSystem
                ? UserDAO.isStaff(username)
                : UserDAO.isCustomer(username);

        if (!exists) {
            showModernError("User not found.");
            return;
        }


        if (!isStaffSystem) {
            // CUSTOMER
            String question = UserDAO.getSecurityQuestionByUsername(username);
            String answer = showModernInputDialog("Security Question", question);

            if (!UserDAO.verifySecurityAnswer(username, answer)) {
                showModernError("Incorrect security answer");
                return;
            }

        }

        String inputIC = showModernInputDialog("Staff IC Number", "Enter your IC:");

        if (inputIC == null || !inputIC.matches("^\\d{12}$")) {
            showModernError("Invalid IC format");
            return;
        }

        if (!UserDAO.verifyStaffIdentity(username, inputIC,isStaffSystem)) {
            showModernError("IC does not match");
            return;
        }


        String newPassword = showPasswordDialog("New Password", "Enter new password:");
        if (newPassword == null || newPassword.isEmpty()) return;

        String confirmPassword = showPasswordDialog("Confirm Password", "Confirm password:");
        if (!newPassword.equals(confirmPassword)) {
            showModernError("Passwords do not match");
            return;
        }

        boolean success;

        if (!isStaffSystem) {
            success = UserDAO.resetCustomerPassword(username, newPassword);
        } else {
            success = UserDAO.resetStaffPassword(username, newPassword);
        }

        if (success) {
            showModernSuccessDialog();
        } else {
            showModernError("System error updating password");
        }
    }

    private void showModernMessageDialog(String title, String message, boolean isError) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner);
        dialog.setModal(true);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 28));
        Color color = isError ? new Color(255, 80, 80) : new Color(255, 215, 0);
        panel.setBorder(BorderFactory.createLineBorder(color, 1));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);
        JButton closeX = new JButton("x");
        closeX.setForeground(Color.WHITE);
        closeX.setFocusPainted(false);
        closeX.setContentAreaFilled(false);
        closeX.setBorderPainted(false);
        closeX.addActionListener(e -> dialog.dispose());
        topBar.add(closeX);

        JPanel content = new JPanel(new GridLayout(0, 1, 10, 10));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(color);

        JLabel msgLbl = new JLabel("<html><div style='width:250px;'>" + message + "</div></html>");
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        msgLbl.setForeground(Color.WHITE);

        JButton btn = new JButton(isError ? "CLOSE" : "CONTINUE");
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> dialog.dispose());

        content.add(titleLbl);
        content.add(msgLbl);
        content.add(new JLabel(" "));
        content.add(btn);

        panel.add(topBar);
        panel.add(content);
        dialog.add(panel);
        dialog.pack();
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void showModernError(String message) {
        showModernMessageDialog("Error", message, true);
    }

    private void showModernSuccessDialog() {
        showModernMessageDialog("Success", "Password reset successful", false);
    }

    private String showModernInputDialog(String title, String message) {
        final String[] result = {null};
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner);
        dialog.setModal(true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(25, 25, 28));
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45), 1));

        // Top Bar with Close Button
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);
        JButton closeX = new JButton("x");
        closeX.setForeground(Color.WHITE);
        closeX.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        closeX.setContentAreaFilled(false);
        closeX.setBorderPainted(false);
        closeX.setFocusPainted(false);
        closeX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeX.addActionListener(e -> dialog.dispose());
        topBar.add(closeX);

        // Content Panel
        JPanel content = new JPanel();
        content.setLayout(new GridLayout(0, 1, 10, 10));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(new Color(255, 215, 0)); // GOLD

        JLabel msgLbl = new JLabel(message);
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        msgLbl.setForeground(Color.WHITE);

        JTextField inputField = new JTextField();
        inputField.setBackground(new Color(45, 45, 48));
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(new Color(255, 215, 0));
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JButton okBtn = new JButton("OK");
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        okBtn.setBackground(new Color(255, 215, 0));
        okBtn.setForeground(Color.BLACK);
        okBtn.setFocusPainted(false);
        okBtn.setBorderPainted(false);
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.setPreferredSize(new Dimension(0, 45));
        okBtn.addActionListener(e -> {
            result[0] = inputField.getText();
            dialog.dispose();
        });

        content.add(titleLbl);
        content.add(msgLbl);
        content.add(inputField);
        content.add(new JLabel(" ")); // Spacer
        content.add(okBtn);

        mainPanel.add(topBar);
        mainPanel.add(content);
        dialog.add(mainPanel);

        dialog.pack();
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return result[0];
    }

    private String showPasswordDialog(String title, String message) {
        final String[] result = {null};
        Window owner = SwingUtilities.getWindowAncestor(this);

        JDialog dialog = new JDialog(owner);
        dialog.setModal(true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(25, 25, 28));
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45), 1));

        // ===== Top Bar =====
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);

        JButton closeX = new JButton("x");
        closeX.setForeground(Color.WHITE);
        closeX.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        closeX.setContentAreaFilled(false);
        closeX.setBorderPainted(false);
        closeX.setFocusPainted(false);
        closeX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeX.addActionListener(e -> dialog.dispose());

        topBar.add(closeX);

        JPanel content = new JPanel();
        content.setLayout(new GridLayout(0, 1, 10, 10));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(new Color(255, 215, 0)); // GOLD

        JLabel msgLbl = new JLabel(message);
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        msgLbl.setForeground(Color.WHITE);


        JPasswordField passwordField = getPasswordField();

        JButton toggleBtn = new JButton(new ImageIcon("src/icon/view.png"));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.setForeground(Color.WHITE);

        toggleBtn.addActionListener(e -> handlePasswordToggle(passwordField, toggleBtn));

        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setOpaque(false);
        fieldPanel.add(passwordField, BorderLayout.CENTER);
        fieldPanel.add(toggleBtn, BorderLayout.EAST);

        JButton okBtn = new JButton("OK");
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        okBtn.setBackground(new Color(255, 215, 0));
        okBtn.setForeground(Color.BLACK);
        okBtn.setFocusPainted(false);
        okBtn.setBorderPainted(false);
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.setPreferredSize(new Dimension(0, 45));

        okBtn.addActionListener(e -> {
            result[0] = new String(passwordField.getPassword());
            dialog.dispose();
        });

        content.add(titleLbl);
        content.add(msgLbl);
        content.add(fieldPanel);
        content.add(new JLabel(" "));
        content.add(okBtn);

        mainPanel.add(topBar);
        mainPanel.add(content);
        dialog.add(mainPanel);

        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);

        return result[0];
    }

    private static JPasswordField getPasswordField() {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBackground(new Color(45, 45, 48));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(new Color(255, 215, 0));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordField.setEchoChar('•');
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return passwordField;
    }


    public static void handlePasswordToggle(JPasswordField pwdField, JButton toggleBtn){
        ImageIcon openEye = new ImageIcon("src/icon/view.png");
        ImageIcon closedEye = new ImageIcon("src/icon/hide.png");

        if (pwdField.getEchoChar() == (char) 0) {
            pwdField.setEchoChar('●');
            toggleBtn.setIcon(openEye);
        } else {
            pwdField.setEchoChar((char) 0);
            toggleBtn.setIcon(closedEye);
        }

    }
}