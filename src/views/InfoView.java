package views;

import PageManager.switchPage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

public class InfoView extends JPanel {
    private final String categoryTitle;
    private final String[] infoPoints;
    private final String iconSymbol;
    private final Color GOLD = new Color(255, 204, 0);
    private final Color DANGER_RED = new Color(220, 53, 69);

    public InfoView(String title, String icon, String[] points) {
        this.categoryTitle = title;
        this.iconSymbol = icon;
        this.infoPoints = points;

        this.setLayout(null);
        this.setOpaque(true);

        int sw = Toolkit.getDefaultToolkit().getScreenSize().width;
        int sh = Toolkit.getDefaultToolkit().getScreenSize().height;

        // 1. Title
        JLabel titleLabel = new JLabel("NexusBank | " + categoryTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 35));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(40, 30, 700, 50);
        add(titleLabel);

        // 2. Back Button
        JButton backBtn = createCustomBackButton();
        backBtn.setBounds(sw - 150, 30, 100, 40);
        backBtn.addActionListener(e -> switchPage.back());
        add(backBtn);

        // 3. Dynamic Content Card
        JPanel contentCard = createInfoCard(sw, sh);
        add(contentCard);
    }

    private JPanel createInfoCard(int sw, int sh) {
        JPanel card = getJPanel(sw, sh);

        JLabel ico = new JLabel(iconSymbol);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        ico.setBounds(40, 30, 60, 75);
        card.add(ico);

        // Header Section
        JLabel header = new JLabel(categoryTitle + " Services");
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(new Color(40, 40, 40));
        header.setBounds(110, 40, 500, 40);
        card.add(header);

        int yOffset = 120;

        // Loop through the points passed in the constructor
        for (String point : infoPoints) {
            JLabel label = new JLabel("<html>" + point + "</html>");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            label.setForeground(new Color(60, 60, 60));
            label.setBounds(60, yOffset, 780, 60);

            JPanel decor = new JPanel();
            decor.setBackground(DANGER_RED);
            decor.setBounds(40, yOffset + 15, 5, 40);
            card.add(decor);

            card.add(label);
            yOffset += 100; // Move down for the next point
        }

        JLabel phoneIcon = new JLabel("📞");
        phoneIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        phoneIcon.setForeground(DANGER_RED);
        phoneIcon.setBounds(200, 530, 30, 30);
        card.add(phoneIcon);

        // Hotline Info
        JLabel hotline;
        if(Objects.equals(categoryTitle, "Scam Alert")) {
            hotline = new JLabel("National Scam Response Centre: 997 (24/7 Hotline)");
        }else{
            hotline = new JLabel("Customer Service Hotline: 1-234-56-7890 (24/7)");
        }
        hotline.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hotline.setForeground(DANGER_RED);
        hotline.setBounds(235, 530, 500, 30);
        card.add(hotline);

        return card;
    }

    private JPanel getJPanel(int sw, int sh) {
        int cardW = 900;
        int cardH = 600;

        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 240));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBounds((sw - cardW) / 2, (sh - cardH) / 2, cardW, cardH);
        return card;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ImageIcon bg = new ImageIcon("src/image1/CusLoginBackground.jpg");
        g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), null);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private JButton createCustomBackButton() {
        JButton backBtn = new JButton("← BACK");

        backBtn.setBounds(50, 30, 100, 40); // Make sure it's not overlapping the title
        backBtn.addActionListener(e -> switchPage.to("LOGIN"));
        add(backBtn);

        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setForeground(Color.WHITE);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        backBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backBtn.setForeground(GOLD);
                backBtn.setBorder(BorderFactory.createLineBorder(GOLD, 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                backBtn.setForeground(Color.WHITE);
                backBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            }

        });
        return backBtn;
    }
}