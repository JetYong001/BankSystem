package views.staffPage.MANAFERIAL;

import DAO.UserDAO;
import models.Account.Account;
import models.Card.Card;
import models.Card.CreditCard;
import models.User.Customer;
import views.customerPage.Themeable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Customer_List_Page extends JPanel implements Themeable {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);
    private final JPanel listContainer;
    private final SearchField searchField;
    private List<Customer> allCustomers = new ArrayList<>();

    private final Color GOLD = new Color(212, 175, 55);
    private Color BG = new Color(13, 13, 15);
    private Color CARD_BG = new Color(22, 22, 26);
    private Color HOVER_BG = new Color(30, 30, 35);
    private Color TEXT_PRIMARY = new Color(255, 255, 255);
    private Color TEXT_SECONDARY = new Color(150, 150, 160);
    private Color BORDER = new Color(45, 45, 50);
    private final Color ACCENT_BLUE = new Color(88, 166, 255);
    private Color INPUT_BG = new Color(30, 30, 35);

    private final String FONT_MAIN = "Inter";
    private final String FONT_MONO = "JetBrains Mono";
    private final JLabel titleLabel;
    private boolean isEditing = false;

    public Customer_List_Page(boolean isDark) {
        setLayout(new BorderLayout());

        JPanel listPage = new JPanel(new BorderLayout(0, 40));
        listPage.setOpaque(false);
        listPage.setBorder(new EmptyBorder(60, 100, 60, 100));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        titleLabel = new JLabel("CLIENT CENTRAL");
        titleLabel.setFont(new Font(FONT_MAIN, Font.BOLD, 36));

        searchField = new SearchField(400, 50);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                renderList(searchField.getText());
            }
        });

        head.add(titleLabel, BorderLayout.WEST);
        head.add(searchField, BorderLayout.EAST);
        listPage.add(head, BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);

        listPage.add(createScroll(listContainer), BorderLayout.CENTER);

        container.setOpaque(false);
        container.add(listPage, "LIST");
        add(container, BorderLayout.CENTER);

        updateTheme(isDark);
        refreshData();
    }

    private void setThemeColors(boolean isDark) {
        if (isDark) {
            BG = new Color(13, 13, 15);
            CARD_BG = new Color(22, 22, 26);
            HOVER_BG = new Color(30, 30, 35);
            TEXT_PRIMARY = Color.WHITE;
            TEXT_SECONDARY = new Color(150, 150, 160);
            BORDER = new Color(45, 45, 50);
            INPUT_BG = new Color(30, 30, 35);
        } else {
            BG = new Color(248, 249, 252);
            CARD_BG = Color.WHITE;
            HOVER_BG = new Color(240, 240, 245);
            TEXT_PRIMARY = new Color(33, 37, 41);
            TEXT_SECONDARY = new Color(108, 117, 125);
            BORDER = new Color(220, 220, 225);
            INPUT_BG = Color.WHITE;
        }
    }

    @Override
    public void updateTheme(boolean isDark) {
        setThemeColors(isDark);
        setBackground(BG);
        titleLabel.setForeground(TEXT_PRIMARY);
        renderList(searchField.getText());
        repaint();
        revalidate();
    }

    private void renderList(String query) {
        listContainer.removeAll();
        String q = query.toLowerCase();
        List<Customer> filtered = allCustomers.stream()
                .filter(c -> c.getFull_name().toLowerCase().contains(q) ||
                        c.getCustomerID().toLowerCase().contains(q) ||
                        c.getIcNumber().contains(q))
                .toList();

        for (Customer c : filtered) {
            listContainer.add(createRow(c));
            listContainer.add(Box.createVerticalStrut(15));
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createRow(Customer c) {
        RowPanel row = new RowPanel(16);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setLayout(new BorderLayout(25, 0));
        row.setBorder(new EmptyBorder(18, 30, 18, 35));

        Avatar avatar = new Avatar(c.getFull_name().substring(0, 1).toUpperCase(), 54, UserDAO.getAvatar(c.getCustomerID()));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        JLabel name = new JLabel(c.getFull_name());
        name.setFont(new Font(FONT_MAIN, Font.BOLD, 18));
        name.setForeground(TEXT_PRIMARY);
        JLabel id = new JLabel("ID: " + c.getCustomerID() + " • IC: " + c.getIcNumber());
        id.setFont(new Font(FONT_MONO, Font.PLAIN, 12));
        id.setForeground(TEXT_SECONDARY);
        info.add(name);
        info.add(id);

        JPanel action = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 18));
        action.setOpaque(false);
        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font(FONT_MAIN, Font.PLAIN, 24));
        arrow.setForeground(BORDER);
        action.add(arrow);

        row.add(new JLabel(avatar), BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(action, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { showDetails(c); }
            @Override
            public void mouseEntered(MouseEvent e) {
                row.hover = true;
                row.repaint();
                arrow.setForeground(GOLD);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                row.hover = false;
                row.repaint();
                arrow.setForeground(BORDER);
            }
        });
        return row;
    }

    private void showDetails(Customer c) {
        isEditing = false;
        JPanel detail = new JPanel(new BorderLayout(0, 40));
        detail.setBackground(BG);
        detail.setBorder(new EmptyBorder(50, 80, 50, 80));

        JPanel nav = getNavPanel();
        detail.add(nav, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(50, 0));
        content.setOpaque(false);

        RowPanel left = new RowPanel(20);
        left.setPreferredSize(new Dimension(420, 0));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(new EmptyBorder(45, 45, 45, 45));

        Avatar icon = new Avatar(c.getFull_name().substring(0, 1).toUpperCase(), 120, UserDAO.getAvatar(c.getCustomerID()));
        JLabel avatarLabel = new JLabel(icon);
        avatarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isEditing) {
                    FileDialog fd = new FileDialog((Frame) SwingUtilities.getWindowAncestor(Customer_List_Page.this), "Select Avatar", FileDialog.LOAD);
                    fd.setFile("*.jpg;*.jpeg;*.png");
                    fd.setVisible(true);
                    if (fd.getFile() != null) {
                        String path = fd.getDirectory() + fd.getFile();
                        UserDAO.saveAvatar(c.getCustomerID(), path);
                        showDetails(c);
                        editModeTrigger(left);
                    }
                }
            }
        });

        JTextField nameEdit  = createEditableField(c.getFull_name());
        JTextField addrEdit  = createEditableField(c.getAddress());
        JTextField phoneEdit = createEditableField(c.getPhoneNumber());
        JTextField gmailEdit = createEditableField(c.getGmail());
        JTextField[] fields  = {nameEdit, addrEdit, phoneEdit, gmailEdit};

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        controlPanel.setOpaque(false);
        controlPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton editBtn   = createModernBtn("EDIT PROFILE", ACCENT_BLUE, Color.BLACK);
        JButton saveBtn   = createModernBtn("SAVE CHANGES", GOLD, Color.BLACK);
        JButton cancelBtn = createModernBtn("CANCEL", BORDER, TEXT_PRIMARY);

        saveBtn.setVisible(false);
        cancelBtn.setVisible(false);

        editBtn.addActionListener(e -> {
            isEditing = true;
            avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            for(JTextField f : fields) {
                f.setEditable(true);
                f.setBackground(INPUT_BG);
                f.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(GOLD, 1, 10),
                        new EmptyBorder(10, 15, 10, 15)));
            }
            editBtn.setVisible(false);
            saveBtn.setVisible(true);
            cancelBtn.setVisible(true);
        });

        cancelBtn.addActionListener(e -> showDetails(c));

        saveBtn.addActionListener(e -> {
            boolean success = UserDAO.updateCustomerInfo(c.getCustomerID(), nameEdit.getText(),
                    addrEdit.getText(), phoneEdit.getText(), gmailEdit.getText());
            if (success) {
                refreshData();

                Customer updated = allCustomers.stream()
                        .filter(cust -> cust.getCustomerID().equals(c.getCustomerID()))
                        .findFirst()
                        .orElse(c);

                showDetails(updated);

                container.revalidate();
                container.repaint();

                JOptionPane.showMessageDialog(this, "Profile Synchronized.");
            }
        });

        controlPanel.add(editBtn);
        controlPanel.add(saveBtn);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(cancelBtn);

        left.add(avatarLabel);
        left.add(Box.createVerticalStrut(35));
        left.add(createDetailItem("FULL NAME"));
        left.add(nameEdit);
        left.add(Box.createVerticalStrut(15));
        left.add(createDetailItem("RESIDENTIAL ADDRESS"));
        left.add(addrEdit);
        left.add(Box.createVerticalStrut(15));
        left.add(createDetailItem("CONTACT NUMBER"));
        left.add(phoneEdit);
        left.add(Box.createVerticalStrut(15));
        left.add(createDetailItem("EMAIL ADDRESS"));
        left.add(gmailEdit);
        left.add(Box.createVerticalStrut(35));
        left.add(controlPanel);

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 30));
        right.setOpaque(false);
        right.add(createAccountSection(c.getAccounts()));

        List<Card> cards = new ArrayList<>();
        if (c.getAccounts() != null) {
            for (Account a : c.getAccounts()) {
                if (a.getCardNumber() != null) cards.addAll(Arrays.asList(a.getCardNumber()));
            }
        }
        right.add(createCardSection(cards));

        content.add(left, BorderLayout.WEST);
        content.add(right, BorderLayout.CENTER);
        detail.add(content, BorderLayout.CENTER);

        container.add(detail, "DETAILS");
        cardLayout.show(container, "DETAILS");
    }

    private JPanel getNavPanel() {
        JButton back = new JButton("←  BACK TO REPOSITORY");
        back.setFont(new Font(FONT_MAIN, Font.BOLD, 12));
        back.setForeground(TEXT_SECONDARY);
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> {
            refreshData();
            cardLayout.show(container, "LIST");
        });

        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);
        nav.add(back, BorderLayout.WEST);
        return nav;
    }

    private void editModeTrigger(JPanel left) {
        isEditing = true;
        for (Component comp : left.getComponents()) {
            if (comp instanceof JTextField f) {
                f.setEditable(true);
                f.setBackground(INPUT_BG);
                f.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(GOLD, 1, 10),
                        new EmptyBorder(10, 15, 10, 15)));
            }
            if (comp instanceof JPanel) {
                for (Component sub : ((JPanel) comp).getComponents()) {
                    if (sub instanceof JButton) {
                        String txt = ((JButton) sub).getText();
                        sub.setVisible(!txt.equals("EDIT PROFILE"));
                    }
                }
            }
            if (comp instanceof JLabel && ((JLabel) comp).getIcon() instanceof Avatar) {
                comp.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        }
    }

    private JButton createModernBtn(String t, Color bg, Color fg) {
        JButton b = new JButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        b.setFont(new Font(FONT_MAIN, Font.BOLD, 11));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(10, 20, 10, 20));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTextField createEditableField(String text) {
        JTextField f = new JTextField(text);
        f.setEditable(false);
        f.setBackground(BG.darker());
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(GOLD);
        f.setFont(new Font(FONT_MAIN, Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER, 1, 10),
                new EmptyBorder(10, 15, 10, 15)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JLabel createDetailItem(String label) {
        JLabel l = new JLabel(label);
        l.setFont(new Font(FONT_MAIN, Font.BOLD, 10));
        l.setForeground(GOLD);
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private RowPanel createAccountSection(List<Account> accounts) {
        RowPanel p = new RowPanel(20);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel t = new JLabel("FINANCIAL ACCOUNTS");
        t.setFont(new Font(FONT_MAIN, Font.BOLD, 13));
        t.setForeground(TEXT_SECONDARY);
        t.setBorder(new EmptyBorder(0, 0, 20, 0));
        p.add(t, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        if (accounts != null) {
            for (Account acc : accounts) {
                JPanel row = getAccountRow(acc);
                list.add(row);
                JSeparator s = new JSeparator();
                s.setForeground(BORDER);
                s.setBackground(BORDER);
                list.add(s);
            }
        }
        p.add(createScroll(list), BorderLayout.CENTER);
        return p;
    }

    private JPanel getAccountRow(Account acc) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setBorder(new EmptyBorder(12, 0, 12, 0));
        JLabel n = new JLabel(acc.getAccountNum());
        n.setFont(new Font(FONT_MONO, Font.PLAIN, 15));
        n.setForeground(TEXT_PRIMARY);
        JLabel b = new JLabel(String.format("RM %.2f", acc.getBalance()));
        b.setFont(new Font(FONT_MONO, Font.BOLD, 15));
        b.setForeground(GOLD);
        row.add(n, BorderLayout.WEST);
        row.add(b, BorderLayout.EAST);
        return row;
    }

    private RowPanel createCardSection(List<Card> cards) {
        RowPanel p = new RowPanel(20);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel t = new JLabel("PROVISIONED CARDS");
        t.setFont(new Font(FONT_MAIN, Font.BOLD, 13));
        t.setForeground(TEXT_SECONDARY);
        t.setBorder(new EmptyBorder(0, 0, 20, 0));
        p.add(t, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        if (cards != null) {
            for (Card card : cards) {
                JPanel row = getCardRow(card);
                list.add(row);
                JSeparator s = new JSeparator();
                s.setForeground(BORDER);
                s.setBackground(BORDER);
                list.add(s);
            }
        }
        p.add(createScroll(list), BorderLayout.CENTER);
        return p;
    }

    private JPanel getCardRow(Card card) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setBorder(new EmptyBorder(12, 0, 12, 0));
        String cNum = card.getCardNumber();
        String last4 = cNum.substring(Math.max(0, cNum.length() - 4));
        JLabel n = new JLabel("•••• •••• •••• " + last4);
        n.setFont(new Font(FONT_MONO, Font.PLAIN, 15));
        n.setForeground(TEXT_PRIMARY);
        JLabel ty = new JLabel(card instanceof CreditCard ? "CREDIT" : "DEBIT");
        ty.setFont(new Font(FONT_MAIN, Font.BOLD, 10));
        ty.setForeground(ACCENT_BLUE);
        row.add(n, BorderLayout.WEST);
        row.add(ty, BorderLayout.EAST);
        return row;
    }

    public void refreshData() {
        allCustomers = UserDAO.getAllCustomer();
        renderList(searchField.getText());
    }

    private JScrollPane createScroll(JPanel p) {
        JScrollPane s = new JScrollPane(p);
        s.setBorder(null);
        s.setOpaque(false);
        s.getViewport().setOpaque(false);
        s.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() { this.thumbColor = BORDER; this.trackColor = BG; }
            @Override protected JButton createDecreaseButton(int o) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
            @Override protected JButton createIncreaseButton(int o) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
        });
        s.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
        return s;
    }

    private class RowPanel extends JPanel {
        int r; boolean hover = false;
        RowPanel(int r) { this.r = r; setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover ? HOVER_BG : CARD_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));
            g2.setColor(hover ? GOLD : BORDER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2.5f, getHeight() - 2.5f, r, r));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class Avatar implements Icon {
        String t; int s; Image img;
        Avatar(String t, int s, String path) {
            this.t = t; this.s = s;
            if (path != null && !path.isEmpty()) {
                try { img = ImageIO.read(new File(path)); } catch (Exception ignored) {}
            }
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (img != null) {
                g2.setClip(new Ellipse2D.Float(x + 1, y + 1, s - 2, s - 2));
                g2.drawImage(img, x, y, s, s, null);
                g2.setClip(null);
            } else {
                g2.setColor(HOVER_BG);
                g2.fillOval(x, y, s, s);
                g2.setColor(GOLD);
                g2.setFont(new Font(FONT_MAIN, Font.BOLD, s / 2));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(t, x + (s - fm.stringWidth(t)) / 2, y + (s - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(GOLD);
            g2.drawOval(x + 1, y + 1, s - 2, s - 2);
            g2.dispose();
        }
        @Override public int getIconWidth() { return s; }
        @Override public int getIconHeight() { return s; }
    }

    private class SearchField extends JTextField {
        SearchField(int w, int h) {
            setPreferredSize(new Dimension(w, h));
            setOpaque(false);
            setForeground(TEXT_PRIMARY);
            setCaretColor(GOLD);
            setBorder(new EmptyBorder(0, 50, 0, 20));
            setFont(new Font(FONT_MAIN, Font.PLAIN, 15));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            g2.setColor(hasFocus() ? GOLD : BORDER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 25, 25);
            g2.setColor(TEXT_SECONDARY);
            g2.drawOval(20, getHeight()/2 - 7, 14, 14);
            g2.drawLine(32, getHeight()/2 + 5, 38, getHeight()/2 + 11);
            if (getText().isEmpty() && !hasFocus()) {
                g2.drawString("Filter identity...", 50, getHeight() / 2 + 6);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private record RoundedBorder(Color color, int thickness, int radius) implements javax.swing.border.Border {
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + thickness, y + thickness, width - (thickness * 2) - 1, height - (thickness * 2) - 1, radius, radius);
            g2.dispose();
        }
        public Insets getBorderInsets(Component c) { return new Insets(thickness, thickness, thickness, thickness); }
        public boolean isBorderOpaque() { return false; }
    }
}