package views.staffPage.MANAFERIAL;

import DAO.*;
import models.Application.ApplicationRecord;
import views.customerPage.Themeable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ApprovalListPage extends JPanel implements Themeable {
    private Color CARD_BG;
    private Color CARD_HOVER;
    private Color BORDER;
    private Color TEXT_MAIN;
    private Color TEXT_SUB;
    private Color SHADOW;
    private static final Color ACCENT = new Color(255, 204, 0);
    private static final Color APPROVE_GREEN = new Color(46, 204, 113);
    private static final Color REJECT_RED = new Color(231, 76, 60);

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private boolean isDark;

    public ApprovalListPage(boolean isDark) {
        this.isDark = isDark;
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        setLayout(new BorderLayout());
        updateTheme(isDark);
        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    public void updateTheme(boolean isDark) {
        this.isDark = isDark;
        Color PAGE_BG;
        if (isDark) {
            PAGE_BG = new Color(13, 13, 15);
            CARD_BG = new Color(22, 22, 26);
            CARD_HOVER = new Color(28, 28, 33);
            BORDER = new Color(45, 45, 50);
            TEXT_MAIN = new Color(255, 255, 255);
            TEXT_SUB = new Color(150, 150, 160);
            SHADOW = new Color(0, 0, 0, 80);
        } else {
            PAGE_BG = new Color(245, 246, 252);
            CARD_BG = Color.WHITE;
            CARD_HOVER = new Color(250, 251, 255);
            BORDER = new Color(220, 225, 235);
            TEXT_MAIN = new Color(33, 37, 41);
            TEXT_SUB = new Color(108, 117, 125);
            SHADOW = new Color(0, 0, 0, 20);
        }
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(40, 50, 40, 50));
        refreshMainPage();
    }

    private void refreshMainPage() {
        contentPanel.removeAll();
        contentPanel.add(buildCategoryPage(), "CATEGORIES");
        contentPanel.revalidate();
        contentPanel.repaint();
        cardLayout.show(contentPanel, "CATEGORIES");
    }

    private JPanel buildCategoryPage() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 40));
        wrapper.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleArea = new JPanel(new GridLayout(2, 1, 0, 5));
        titleArea.setOpaque(false);
        JLabel title = new JLabel("Approval Center");
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setForeground(TEXT_MAIN);
        JLabel subtitle = new JLabel("Manage and review pending customer applications");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle.setForeground(TEXT_SUB);
        titleArea.add(title);
        titleArea.add(subtitle);
        header.add(titleArea, BorderLayout.WEST);

        JPanel grid = new JPanel(new GridLayout(1, 4, 25, 0));
        grid.setOpaque(false);
        grid.add(createCategoryCard("Savings", "New Account Opening", "SAVINGS_ACCOUNT"));
        grid.add(createCategoryCard("Fixed Deposit", "Investment Review", "FIXED_DEPOSIT"));
        grid.add(createCategoryCard("Credit Card", "Credit Assessment", "NEW_CREDIT_CARD"));
        grid.add(createCategoryCard("Subcard", "Supplementary Requests", "SUBCARD"));

        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createCategoryCard(String titleText, String bodyText, String type) {
        HoverCard card = new HoverCard(30);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(35, 30, 35, 30));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_MAIN);

        JLabel body = new JLabel(bodyText);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        body.setForeground(TEXT_SUB);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 5));
        center.setOpaque(false);
        center.add(title);
        center.add(body);

        JLabel arrow = new JLabel("Review Now →");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 13));
        arrow.setForeground(ACCENT);

        card.add(center, BorderLayout.CENTER);
        card.add(arrow, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showCategoryList(type); }
            @Override public void mouseEntered(MouseEvent e) { card.hover = true; card.repaint(); }
            @Override public void mouseExited(MouseEvent e) { card.hover = false; card.repaint(); }
        });
        return card;
    }

    private void showCategoryList(String type) {
        List<ApplicationRecord> filtered = new ArrayList<>();
        List<ApplicationRecord> all = ApplyDAO.getAllApplications();
        for (ApplicationRecord record : all) {
            if ("PENDING".equalsIgnoreCase(record.getStatus()) && type.equalsIgnoreCase(record.getApplicationType())) {
                filtered.add(record);
            }
        }
        contentPanel.add(buildApplicationListPage(type, filtered), "LIST_" + type);
        cardLayout.show(contentPanel, "LIST_" + type);
    }

    private JPanel buildApplicationListPage(String type, List<ApplicationRecord> records) {
        JPanel page = new JPanel(new BorderLayout(0, 20));
        page.setOpaque(false);

        JPanel headerArea = new JPanel(new BorderLayout());
        headerArea.setOpaque(false);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setOpaque(false);
        JButton back = createIconButton("Back to Categories");
        back.addActionListener(e -> refreshMainPage());
        topBar.add(back);

        JLabel title = new JLabel(formatType(type) + " Queue");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(10, 0, 0, 0));

        headerArea.add(topBar, BorderLayout.NORTH);
        headerArea.add(title, BorderLayout.CENTER);

        page.add(headerArea, BorderLayout.NORTH);
        page.add(buildListContent(type, records), BorderLayout.CENTER);
        return page;
    }

    private JComponent buildListContent(String type, List<ApplicationRecord> records) {
        if (records.isEmpty()) return buildEmptyState("No pending " + formatType(type) + " requests.");

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        for (ApplicationRecord record : records) {
            listPanel.add(createApplicationRow(type, record));
            listPanel.add(Box.createVerticalStrut(15));
        }

        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setOpaque(false);
        scrollWrapper.add(listPanel, BorderLayout.NORTH);
        return wrapScroll(scrollWrapper);
    }

    private JPanel createApplicationRow(String type, ApplicationRecord record) {
        HoverCard row = new HoverCard(20);
        row.setLayout(new BorderLayout(25, 0));
        row.setPreferredSize(new Dimension(0, 90));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setBorder(new EmptyBorder(15, 30, 15, 30));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel n = new JLabel(safe(record.getFullName()));
        n.setFont(new Font("Segoe UI", Font.BOLD, 18));
        n.setForeground(TEXT_MAIN);
        JLabel s = new JLabel("Ref: #" + record.getApplicationId() + " • " + safe(record.getApplicantUsername()));
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setForeground(TEXT_SUB);
        left.add(n); left.add(s);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        right.setOpaque(false);

        JLabel date = new JLabel(safe(record.getSubmittedAt()));
        date.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        date.setForeground(TEXT_SUB);

        JLabel badge = new JLabel(" PENDING ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        badge.setForeground(ACCENT);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));

        right.add(date);
        right.add(badge);

        row.add(left, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showApplicationDetail(type, record); }
            @Override public void mouseEntered(MouseEvent e) { row.hover = true; row.repaint(); }
            @Override public void mouseExited(MouseEvent e) { row.hover = false; row.repaint(); }
        });
        return row;
    }

    private void showApplicationDetail(String type, ApplicationRecord record) {
        JPanel detailPage = new JPanel(new BorderLayout(0, 20));
        detailPage.setOpaque(false);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setOpaque(false);
        JButton back = createIconButton("Back to Queue");
        back.addActionListener(e -> showCategoryList(type));
        topBar.add(back);

        JLabel title = new JLabel("Application Details: " + record.getApplicationId());
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TEXT_MAIN);
        title.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(topBar, BorderLayout.NORTH);
        top.add(title, BorderLayout.SOUTH);

        detailPage.add(top, BorderLayout.NORTH);
        detailPage.add(wrapScroll(buildDetailContent(record, type)), BorderLayout.CENTER);

        contentPanel.add(detailPage, "DETAIL");
        cardLayout.show(contentPanel, "DETAIL");
    }

    private JPanel buildDetailContent(ApplicationRecord record, String type) {
        JPanel main = new JPanel(new BorderLayout(0, 35));
        main.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(0, 2, 25, 25));
        grid.setOpaque(false);

        for (String[] entry : buildApplicationDetailRows(record)) {
            grid.add(createDetailItem(entry[0], entry[1]));
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        actions.setOpaque(false);

        JButton btnReject = createFancyButton("Reject Application", REJECT_RED);
        JButton btnApprove = createFancyButton("Approve Application", APPROVE_GREEN);

        btnReject.addActionListener(e -> updateStatus(record, "REJECTED", type));
        btnApprove.addActionListener(e -> updateStatus(record, "APPROVED", type));

        actions.add(btnReject);
        actions.add(btnApprove);

        main.add(grid, BorderLayout.CENTER);
        main.add(actions, BorderLayout.SOUTH);
        return main;
    }

    private JPanel createDetailItem(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_SUB);

        JLabel v = new JLabel("<html>" + value + "</html>");
        v.setFont(new Font("Segoe UI", Font.BOLD, 15));
        v.setForeground(TEXT_MAIN);

        p.add(l, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JButton createIconButton(String text) {
        JButton b = new JButton("< " + text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(ACCENT);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createFancyButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(220, 55));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateStatus(ApplicationRecord record, String status, String type) {
        if (ApplyDAO.updateApplicationStatus(record.getApplicationId(), status)) {
            String username = record.getApplicantUsername();
            String usersContent = UserDAO.readJsonFile();
            String customerId = null;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\{\\s*\"customer_id\"\\s*:\\s*\"([^\"]+)\"[^}]*\"username\"\\s*:\\s*\"" + java.util.regex.Pattern.quote(username) + "\"").matcher(usersContent);
            if (m.find()) customerId = m.group(1);

            if (customerId != null) {
                if ("REJECTED".equalsIgnoreCase(status)) {
                    String depStr = record.getInitialDeposit();
                    if (depStr != null && !depStr.trim().isEmpty()) {
                        try {
                            double ref = Double.parseDouble(depStr.trim());
                            if (ref > 0) {
                                String targetAcc = AccountDAO.findCurrentAccountNumber(customerId);
                                AccountDAO.refundToAccount(targetAcc, ref);
                                String refId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                                String ts = new SimpleDateFormat("HH:mm:ss").format(new Date());
                                TransferDAO.addRecord(targetAcc, "REFUND", ref, "Application Rejected", refId, ts, "SYSTEM");
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                } else if ("APPROVED".equalsIgnoreCase(status)) {
                    String depStr = record.getInitialDeposit();
                    double dep = 0.0;
                    if (depStr != null && !depStr.trim().isEmpty()) {
                        try { dep = Double.parseDouble(depStr.trim()); } catch (NumberFormatException ignored) {}
                    }

                    switch (type.toUpperCase()) {
                        case "SAVINGS_ACCOUNT" -> AccountDAO.createApprovedSavingsAccount(customerId, dep);
                        case "FIXED_DEPOSIT" -> {
                            String tenureStr = safe(record.getTenureMonths()).replace(" Months", "").trim();
                            int mos = 0;
                            try { if (!tenureStr.equals("-")) mos = Integer.parseInt(tenureStr); } catch (NumberFormatException ignored) {}
                            double intRate = RateDAO.calculateFDInterest(dep, String.valueOf(mos));
                            AccountDAO.createApprovedFixedDeposit(customerId, dep, intRate, mos);
                        }
                        case "NEW_CREDIT_CARD" -> {
                            String limitStr = record.getRequestedLimit();
                            double limit = 0.0;
                            if (limitStr != null && !limitStr.trim().isEmpty()) {
                                try {
                                    String cleanLimit = limitStr.replace("RM", "").replace(",", "").trim();
                                    limit = Double.parseDouble(cleanLimit);
                                } catch (NumberFormatException ignored) {}
                            }
                            AccountDAO.createApprovedCreditCard(customerId, limit);
                        }
                        case "SUBCARD" -> AccountDAO.createApprovedSubcard(customerId, record.getPrimaryCardNumber());
                    }
                }
            }
            showCategoryList(type);
        }
    }

    private List<String[]> buildApplicationDetailRows(ApplicationRecord r) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Full Name", safe(r.getFullName())});
        rows.add(new String[]{"ID / IC Number", safe(r.getIdentityNumber())});
        rows.add(new String[]{"Monthly Income", valueWithMoney(r.getMonthlyIncome())});
        rows.add(new String[]{"Occupation", safe(r.getOccupation())});
        rows.add(new String[]{"Employer", safe(r.getEmployerName())});
        rows.add(new String[]{"Contact Email", safe(r.getEmail())});
        rows.add(new String[]{"Contact Phone", safe(r.getPhoneNumber())});
        rows.add(new String[]{"Full Address", safe(r.getAddressLine()) + ", " + safe(r.getCity()) + " " + safe(r.getPostCode()) + ", " + safe(r.getState())});

        String type = r.getApplicationType();
        if ("FIXED_DEPOSIT".equalsIgnoreCase(type)) {
            rows.add(new String[]{"Deposit Amount", valueWithMoney(r.getInitialDeposit())});
            rows.add(new String[]{"Tenure Period", safe(r.getTenureMonths()) + " Months"});
        } else if ("NEW_CREDIT_CARD".equalsIgnoreCase(type)) {
            rows.add(new String[]{"Requested Limit", safe(r.getRequestedLimit())});
        } else if ("SUBCARD".equalsIgnoreCase(type)) {
            rows.add(new String[]{"Primary Card", safe(r.getPrimaryCardNumber())});
            rows.add(new String[]{"Relationship", safe(r.getRelationshipToPrimary())});
        }
        return rows;
    }

    private JScrollPane wrapScroll(JPanel content) {
        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null); sp.setOpaque(false); sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        return sp;
    }

    private JComponent buildEmptyState(String t) {
        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false);
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.ITALIC, 16)); l.setForeground(TEXT_SUB); p.add(l);
        return p;
    }

    private String formatType(String t) { return switch (t) { case "SAVINGS_ACCOUNT" -> "Savings"; case "FIXED_DEPOSIT" -> "Fixed Deposit"; case "NEW_CREDIT_CARD" -> "Credit Card"; default -> "Application"; }; }
    private String valueWithMoney(String v) { return (v == null || v.isBlank()) ? "-" : "RM " + v; }
    private String safe(String v) { return (v == null || v.isBlank()) ? "-" : v; }

    private class HoverCard extends JPanel {
        private final int radius;
        private boolean hover = false;
        private HoverCard(int r) { this.radius = r; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!isDark) {
                g2.setColor(SHADOW);
                g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, radius, radius);
            }

            g2.setColor(hover ? CARD_HOVER : CARD_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(hover ? ACCENT : BORDER);
            g2.setStroke(new BasicStroke(hover ? 2f : 1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }
}