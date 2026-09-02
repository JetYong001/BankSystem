package views.customerPage;

import DAO.UserDAO;
import DAO.RateDAO;
import models.Account.Account;
import models.User.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DashboardView extends JPanel implements Themeable {
    private final Color ACCENT = new Color(255, 204, 0);
    private final Color SECONDARY_ACCENT = new Color(255, 170, 0);

    private Color currentBG = new Color(248, 249, 252);
    private Color currentCardBG = Color.WHITE;
    private Color currentTextMain = new Color(33, 37, 41);
    private Color currentTextSub = new Color(108, 117, 125);
    private Color currentBorder = new Color(0, 0, 0, 18);

    private Customer customer;
    private boolean isVisible;
    private final String hiddenValue = "RM ****";
    private final String username;
    private final JLabel welcome;
    private final JLabel name;
    private final CardLayout viewLayout;
    private final JPanel viewPanel;

    public DashboardView(Customer customer, boolean isDark) {
        this.customer = customer;
        this.username = (customer != null) ? customer.getUsername() : "";
        this.isVisible = UserDAO.getEyeStatus(username);

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 50, 40, 50));

        welcome = new JLabel("Hello,");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        String fullName = (customer != null) ? customer.getFull_name() : "Valued Customer";
        name = new JLabel(fullName);
        name.setFont(new Font("Segoe UI", Font.BOLD, 32));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
        header.setOpaque(false);
        header.add(welcome);
        header.add(name);

        viewLayout = new CardLayout();
        viewPanel = new JPanel(viewLayout);
        viewPanel.setOpaque(false);
        viewPanel.add(buildTypeSelectionPage(), "TYPE_SELECTION");

        add(header, BorderLayout.NORTH);
        add(viewPanel, BorderLayout.CENTER);

        updateTheme(isDark);
        viewLayout.show(viewPanel, "TYPE_SELECTION");
    }

    private JPanel buildTypeSelectionPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(30, 0, 0, 0));

        List<Account> accounts = customer != null ? customer.getAccounts() : new ArrayList<>();
        if (accounts == null || accounts.isEmpty()) {
            page.add(buildNoRecordCard(), BorderLayout.CENTER);
            return page;
        }

        int currentCount = 0;
        int savingsCount = 0;
        int fixedCount = 0;

        for (Account account : accounts) {
            String num = account.getAccountNum();
            if (num == null) continue;

            if (num.startsWith("888")) {
                fixedCount++;
            } else if (num.startsWith("168")) {
                savingsCount++;
            } else if (num.startsWith("111")) {
                currentCount++;
            }
        }

        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setOpaque(false);

        options.add(createTypeOption("Current Accounts", currentCount + " account(s)", currentCount == 0, () -> showAccountDetailsByType("CURRENT")));
        options.add(Box.createVerticalStrut(20));
        options.add(createTypeOption("Savings Accounts", savingsCount + " account(s)", savingsCount == 0, () -> showAccountDetailsByType("SAVINGS")));
        options.add(Box.createVerticalStrut(20));
        options.add(createTypeOption("Fixed Deposits", fixedCount + " account(s)", fixedCount == 0, () -> showAccountDetailsByType("FIXED")));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(options, BorderLayout.NORTH);
        page.add(wrapper, BorderLayout.CENTER);
        return page;
    }

    private void showAccountDetailsByType(String type) {
        List<Account> accounts = getAccountsByType(type);
        if (accounts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No Account Record");
            return;
        }
        String pageKey = "DETAIL_" + type;
        viewPanel.add(buildAccountDetailPage(accounts, type), pageKey);
        viewLayout.show(viewPanel, pageKey);
    }

    private JPanel buildAccountDetailPage(List<Account> accounts, String type) {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navBar.setOpaque(false);
        navBar.setBorder(new EmptyBorder(10, 0, 20, 0));

        JButton back = new JButton("← Back to Selection");
        back.setFont(new Font("Segoe UI", Font.BOLD, 14));
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.setForeground(ACCENT);
        back.addActionListener(e -> viewLayout.show(viewPanel, "TYPE_SELECTION"));
        navBar.add(back);

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);

        for (Account acc : accounts) {
            String liveInterest = "0.00";
            boolean isMatured = false;

            if (type.equals("FIXED")) {
                try {
                    String filePath = "data/accounts.json";
                    String content = new String(Files.readAllBytes(Paths.get(filePath)));
                    String blockRegex = "(\"account_number\"\\s*:\\s*\"" + acc.getAccountNum() + "\".*?\"expiry_date\")";
                    Matcher m = Pattern.compile(blockRegex, Pattern.DOTALL).matcher(content);

                    if (m.find()) {
                        String block = m.group(1);
                        Matcher mTenure = Pattern.compile("\"tenure_months\"\\s*:\\s*(\\d+)").matcher(block);
                        if (mTenure.find()) {
                            String tenureStr = mTenure.group(1);
                            double liveRate = RateDAO.getAllRates().getOrDefault(tenureStr, 0.0);
                            double calc = acc.getBalance() * (liveRate / 100.0) * (Integer.parseInt(tenureStr) / 12.0);
                            liveInterest = String.format("%.2f", calc);

                            Matcher mInt = Pattern.compile("(\"interest_amount\"\\s*:\\s*)(\\d+\\.?\\d*)").matcher(block);
                            if (mInt.find() && !mInt.group(2).equals(liveInterest)) {
                                String updatedBlock = block.replaceFirst("(\"interest_amount\"\\s*:\\s*)\\d+\\.?\\d*", "$1" + liveInterest);
                                String newContent = content.replace(block, updatedBlock);
                                Files.write(Paths.get(filePath), newContent.getBytes());
                            }
                        }

                        Matcher mDate = Pattern.compile("\"expiry_date\"\\s*:\\s*\"([^\"]+)\"").matcher(block);
                        if (mDate.find()) {
                            if (java.time.LocalDateTime.now().isAfter(java.time.LocalDateTime.parse(mDate.group(1)))) isMatured = true;
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }

            scrollContent.add(getBalanceCard(acc, type, isMatured, liveInterest));
            scrollContent.add(Box.createVerticalStrut(15));
            scrollContent.add(getDetailCard(acc, type, liveInterest));
            scrollContent.add(Box.createVerticalStrut(40));
        }

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        page.add(navBar, BorderLayout.NORTH);
        page.add(scrollPane, BorderLayout.CENTER);
        return page;
    }

    private JPanel getBalanceCard(Account account, String type, boolean isMatured, String interestAmount) {
        double totalBalance = account.getBalance();
        if (isMatured) {
            try {
                totalBalance += Double.parseDouble(interestAmount);
            } catch (Exception ignored) {}
        }

        final String balanceValue = String.format("RM %,.2f", totalBalance);

        JPanel balanceCard = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), SECONDARY_ACCENT);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(255, 255, 255, 45));
                g2.fillOval(getWidth() - 130, -70, 260, 260);
                g2.fillOval(getWidth() - 210, 90, 190, 190);
                g2.dispose();
            }
        };
        balanceCard.setPreferredSize(new Dimension(850, 230));
        balanceCard.setMaximumSize(new Dimension(850, 230));

        JLabel balLabel = new JLabel(isMatured ? "Total Balance (Interest Included)" : "Total Balance");
        balLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        balLabel.setForeground(new Color(0, 0, 0, 130));
        balLabel.setBounds(50, 40, 400, 20);

        JLabel amountLabel = new JLabel(isVisible ? balanceValue : hiddenValue);
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        amountLabel.setForeground(Color.BLACK);
        amountLabel.setBounds(50, 70, 600, 70);

        JButton eyeBtn = getEyeButton(amountLabel, balanceValue);
        eyeBtn.setBounds(770, 90, 30, 30);

        String displayType = type.equals("CURRENT") ? "CURRENT ACCOUNT" : (type.equals("FIXED") ? "FIXED DEPOSIT" : "SAVINGS ACCOUNT");
        JLabel cardInfo = new JLabel(displayType + "  |  " + account.getAccountNum());
        cardInfo.setFont(new Font("Monospaced", Font.BOLD, 15));
        cardInfo.setForeground(new Color(0, 0, 0, 90));
        cardInfo.setBounds(50, 160, 500, 25);

        balanceCard.add(balLabel);
        balanceCard.add(amountLabel);
        balanceCard.add(eyeBtn);
        balanceCard.add(cardInfo);
        return balanceCard;
    }


    private JPanel getDetailCard(Account account, String type, String interestAmount) {
        JPanel detailCard = getCardPanel();
        detailCard.setLayout(new BorderLayout());

        if (type.equals("FIXED")) {
            detailCard.setMaximumSize(new Dimension(850, 320));
            detailCard.setPreferredSize(new Dimension(850, 320));
        }

        JLabel infoTitle = new JLabel("Account Details");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        infoTitle.setForeground(currentTextMain);
        infoTitle.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel grid = new JPanel(new GridLayout(0, 2, 20, 20));
        grid.setOpaque(false);
        grid.add(createItem("Account Number", account.getAccountNum()));
        grid.add(createItem("Account Type", type.equals("FIXED") ? "Fixed Deposit" : (type.equals("CURRENT") ? "Current Account" : "Savings Account")));

        if (type.equals("FIXED")) {
            String tenure = "N/A", maturity = "N/A";
            boolean isMatured = false;

            try {
                String content = new String(Files.readAllBytes(Paths.get("data/accounts.json")));
                String patternStr = "\"account_number\"\\s*:\\s*\"" + account.getAccountNum() + "\"(.*?)\"cards\"";
                Matcher m = Pattern.compile(patternStr, Pattern.DOTALL).matcher(content);

                if (m.find()) {
                    String block = m.group(1);
                    Matcher mTenure = Pattern.compile("\"tenure_months\"\\s*:\\s*(\\d+)").matcher(block);
                    if (mTenure.find()) tenure = mTenure.group(1);

                    Matcher mDate = Pattern.compile("\"expiry_date\"\\s*:\\s*\"([^\"]+)\"").matcher(block);
                    if (mDate.find()) {
                        maturity = mDate.group(1).split("T")[0];
                        if (java.time.LocalDateTime.now().isAfter(java.time.LocalDateTime.parse(mDate.group(1)))) isMatured = true;
                    }
                }
            } catch (Exception e) {}

            grid.add(createItem("Tenure", tenure + " Months"));
            grid.add(createItem("Expected Interest", "RM " + interestAmount));
            grid.add(createItem("Maturity Date", maturity));

            if (isMatured) {
                grid.add(createItem("Status", "MATURED ●"));
                JLabel success = new JLabel("<html><body style='width:300px; color:#2ecc71;'><b>Note:</b> Account matured. Full interest (RM " + interestAmount + ") is guaranteed upon withdrawal.</body></html>");
                success.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                grid.add(success);
            } else {
                grid.add(createItem("Status", "LOCKED ●"));
                JLabel warning = new JLabel("<html><body style='width:300px; color:#e74c3c;'><b>Warning:</b> Early withdrawal forfeits interest. Only RM " + String.format("%.2f", account.getBalance()) + " will be returned.</body></html>");
                warning.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                grid.add(warning);
            }
        } else {
            grid.add(createItem("Status", "Active ●"));
            grid.add(createItem("Currency", "Malaysian Ringgit (MYR)"));
        }

        detailCard.add(infoTitle, BorderLayout.NORTH);
        detailCard.add(grid, BorderLayout.CENTER);
        return detailCard;
    }

    private JPanel getCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentCardBG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(currentBorder);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(850, 220));
        card.setPreferredSize(new Dimension(850, 220));
        card.setBorder(new EmptyBorder(30, 40, 30, 40));
        return card;
    }

    private JButton getEyeButton(JLabel amountLabel, String balanceValue) {
        ImageIcon openEye = new ImageIcon("src/icon/view.png");
        ImageIcon closedEye = new ImageIcon("src/icon/hide.png");
        JButton eyeBtn = new JButton(isVisible ? openEye : closedEye);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setBorderPainted(false);
        eyeBtn.setFocusPainted(false);
        eyeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeBtn.addActionListener(e -> {
            isVisible = !isVisible;
            amountLabel.setText(isVisible ? balanceValue : hiddenValue);
            eyeBtn.setIcon(isVisible ? openEye : closedEye);
            UserDAO.saveEyeStatus(username, isVisible);
        });
        return eyeBtn;
    }

    private JPanel createItem(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(currentTextSub);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 16));
        v.setForeground(value.contains("●") ? new Color(34, 139, 34) : currentTextMain);
        p.add(l);
        p.add(v);
        return p;
    }

    private List<Account> getAccountsByType(String type) {
        List<Account> result = new ArrayList<>();
        if (customer == null || customer.getAccounts() == null) return result;

        for (Account account : customer.getAccounts()) {
            String num = account.getAccountNum();
            if (num == null) continue;
            if ("FIXED".equals(type) && num.startsWith("888")) result.add(account);
            else if ("SAVINGS".equals(type) && num.startsWith("168")) result.add(account);
            else if ("CURRENT".equals(type) && num.startsWith("111")) result.add(account);
        }
        return result;
    }

    private JPanel buildNoRecordCard() {
        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        JLabel label = new JLabel("No Account Record Found");
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(currentTextSub);
        card.add(label);
        return card;
    }

    @Override
    public void updateTheme(boolean isDark) {
        if (isDark) {
            currentBG = new Color(18, 18, 18);
            currentCardBG = new Color(30, 30, 30);
            currentTextMain = Color.WHITE;
            currentTextSub = new Color(160, 160, 160);
            currentBorder = new Color(255, 255, 255, 20);
        } else {
            currentBG = new Color(248, 249, 252);
            currentCardBG = Color.WHITE;
            currentTextMain = new Color(33, 37, 41);
            currentTextSub = new Color(108, 117, 125);
            currentBorder = new Color(0, 0, 0, 18);
        }
        setBackground(currentBG);
        welcome.setForeground(currentTextSub);
        name.setForeground(currentTextMain);
        refreshTypeOptionThemes(viewPanel);
        repaint();
        revalidate();
    }

    private void refreshTypeOptionThemes(Container parent) {
        for (Component c : parent.getComponents()) {
            if (c instanceof TypeOptionCard card) card.refreshTheme();
            else if (c instanceof Container con) refreshTypeOptionThemes(con);
        }
    }

    public void refreshData(Customer freshCustomer) {
        if (freshCustomer == null) return;
        this.customer = freshCustomer;
        viewPanel.removeAll();
        viewPanel.add(buildTypeSelectionPage(), "TYPE_SELECTION");
        viewLayout.show(viewPanel, "TYPE_SELECTION");
        viewPanel.revalidate();
        viewPanel.repaint();
    }

    private TypeOptionCard createTypeOption(String title, String sub, boolean disabled, Runnable onClick) {
        TypeOptionCard card = new TypeOptionCard(title, sub, disabled, onClick);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private class RoundedCard extends JPanel {
        protected RoundedCard() { setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentCardBG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
            g2.setColor(currentBorder);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 30, 30));
            g2.dispose();
        }
    }

    private class HoverCard extends RoundedCard {
        protected boolean hover = false;
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover ? (currentBG.getRGB() < -10000000 ? new Color(45, 45, 45) : new Color(250, 245, 230)) : currentCardBG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
            g2.setColor(currentBorder);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 30, 30));
            g2.dispose();
        }
    }

    private class TypeOptionCard extends HoverCard {
        private final JLabel t, s, e, a;
        private final boolean d;
        private TypeOptionCard(String title, String sub, boolean disabled, Runnable onClick) {
            this.d = disabled;
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(25, 30, 25, 30));
            setMaximumSize(new Dimension(850, 160));
            setPreferredSize(new Dimension(850, 160));
            setCursor(new Cursor(disabled ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));

            t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.BOLD, 22));
            s = new JLabel(disabled ? "No accounts available" : "View details of your " + title);
            s.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            e = new JLabel(sub); e.setFont(new Font("Monospaced", Font.BOLD, 13));
            a = new JLabel(disabled ? "LOCKED" : "OPEN");
            a.setOpaque(true); a.setBorder(new EmptyBorder(8, 15, 8, 15));
            a.setFont(new Font("Segoe UI", Font.BOLD, 12));

            JPanel right = new JPanel(new GridBagLayout()); right.setOpaque(false); right.add(a);
            add(t, BorderLayout.NORTH); add(s, BorderLayout.CENTER); add(e, BorderLayout.SOUTH); add(right, BorderLayout.EAST);

            if (!disabled) {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent ev) { onClick.run(); }
                    public void mouseEntered(java.awt.event.MouseEvent ev) { hover = true; repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent ev) { hover = false; repaint(); }
                });
            }
            refreshTheme();
        }
        public void refreshTheme() {
            t.setForeground(currentTextMain); s.setForeground(currentTextSub); e.setForeground(ACCENT);
            if (d) { a.setBackground(new Color(200, 200, 200)); a.setForeground(Color.GRAY); }
            else { a.setBackground(ACCENT); a.setForeground(Color.BLACK); }
        }
    }
}