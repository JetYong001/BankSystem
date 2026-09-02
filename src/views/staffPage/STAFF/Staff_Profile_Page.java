package views.staffPage.STAFF;

import models.User.Staff;
import DAO.UserDAO;
import views.customerPage.Themeable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public class Staff_Profile_Page extends JPanel implements Themeable {

    private final Color ACCENT_GOLD = new Color(255, 204, 0);
    private Color cardBg;
    private Color textPrimary;
    private Color textSecondary;
    private Color borderColor;

    private BufferedImage customAvatar = null;
    private boolean isHovered = false;

    private final JPanel headerPanel;
    private final JLabel sectionTitle;
    private final ArrayList<JPanel> infoTiles = new ArrayList<>();
    private final ArrayList<JPanel> statCards = new ArrayList<>();

    public Staff_Profile_Page(Staff staff,boolean isDark) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 50, 30, 50));

        String avatarPath = UserDAO.getAvatar(staff.getStaffID());
        if (!avatarPath.isEmpty()) {
            try {
                customAvatar = ImageIO.read(new File(avatarPath));
            } catch (Exception ignored) {}
        }


        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);

        headerPanel = createHeader(staff);
        scrollContent.add(headerPanel);
        scrollContent.add(Box.createVerticalStrut(30));

        JPanel quickStats = new JPanel(new GridLayout(1, 3, 20, 0));
        quickStats.setOpaque(false);
        quickStats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel s1 = createStatMiniCard("LOGIN STATUS", "ACTIVE NOW", new Color(46, 204, 113));
        JPanel s2 = createStatMiniCard("SECURITY LEVEL", "ENCRYPTED", ACCENT_GOLD);
        JPanel s3 = createStatMiniCard("SESSION TIME", "2H 14M", new Color(52, 152, 219));

        statCards.add(s1); statCards.add(s2); statCards.add(s3);
        quickStats.add(s1); quickStats.add(s2); quickStats.add(s3);

        scrollContent.add(quickStats);
        scrollContent.add(Box.createVerticalStrut(30));

        sectionTitle = new JLabel("DETAILED INFORMATION");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(sectionTitle);
        scrollContent.add(Box.createVerticalStrut(15));

        JPanel infoGrid = new JPanel(new GridLayout(3, 2, 20, 20));
        infoGrid.setOpaque(false);

        infoGrid.add(addTile("FULL NAME", staff.getFull_name(), "Verification: Approved"));
        infoGrid.add(addTile("STAFF ID", staff.getStaffID(), "Hardware Encrypted"));
        infoGrid.add(addTile("POSITION", staff.getPosition().toUpperCase(), "Executive Management"));
        infoGrid.add(addTile("NETWORK ID", "@" + staff.getUsername(), "Internal VPN Tunnel"));
        infoGrid.add(addTile("IDENTITY NO.", mask(staff.getIcNumber()), "Redacted for Privacy"));
        infoGrid.add(addTile("MEMBER SINCE", staff.getCreatedTime().toLocalDate().toString(), "Tenure Verified"));

        infoGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
        scrollContent.add(infoGrid);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        updateTheme(isDark);
    }

    @Override
    public void updateTheme(boolean isDark) {
        Color bgMain;
        if (isDark) {
            bgMain = new Color(13, 13, 15);
            cardBg = new Color(22, 22, 26);
            textPrimary = new Color(255, 255, 255);
            textSecondary = new Color(150, 150, 160);
            borderColor = new Color(45, 45, 50);
        } else {
            bgMain = new Color(245, 246, 250);
            cardBg = Color.WHITE;
            textPrimary = new Color(33, 37, 41);
            textSecondary = new Color(108, 117, 125);
            borderColor = new Color(220, 225, 230);
        }

        setBackground(bgMain);
        sectionTitle.setForeground(ACCENT_GOLD);

        updateHeaderTheme();
        for (JPanel p : infoTiles) updateTileTheme(p);
        for (JPanel p : statCards) p.repaint();

        repaint();
        revalidate();
    }

    private JPanel addTile(String t, String v, String d) {
        JPanel p = createInfoTile(t, v, d);
        infoTiles.add(p);
        return p;
    }

    private void updateHeaderTheme() {
        for (Component c : headerPanel.getComponents()) {
            if (c instanceof JLabel l) {
                if (l.getFont().getSize() > 20) l.setForeground(textPrimary);
                else l.setForeground(textSecondary);
            }
        }
    }

    private void updateTileTheme(JPanel p) {
        for (Component c : p.getComponents()) {
            if (c instanceof JLabel l) {
                if (l.getFont().getSize() > 15) l.setForeground(textPrimary);
                else if (!l.getForeground().equals(ACCENT_GOLD)) l.setForeground(textSecondary);
            }
        }
    }

    private JPanel createHeader(Staff staff) {
        JPanel h = getJPanel();

        JLabel avatar = getJLabel(staff);
        avatar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { isHovered = true; avatar.repaint(); }
            public void mouseExited(MouseEvent e) { isHovered = false; avatar.repaint(); }
            public void mouseClicked(MouseEvent e) {
                FileDialog fd = new FileDialog((Frame)SwingUtilities.getWindowAncestor(Staff_Profile_Page.this), "Avatar", FileDialog.LOAD);
                fd.setVisible(true);
                if (fd.getFile() != null) {
                    try {
                        String p = new File(fd.getDirectory(), fd.getFile()).getAbsolutePath();
                        customAvatar = ImageIO.read(new File(p));
                        UserDAO.saveAvatar(staff.getStaffID(), p);
                        avatar.repaint();
                    } catch (Exception ignored) {}
                }
            }
        });

        JLabel name = new JLabel(staff.getFull_name());
        name.setFont(new Font("Segoe UI", Font.BOLD, 32));
        name.setBounds(180, 65, 500, 40);

        JLabel sub = new JLabel(staff.getPosition() + " • System Management Console");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setBounds(182, 105, 500, 20);

        h.add(avatar); h.add(name); h.add(sub);
        return h;
    }

    private JLabel getJLabel(Staff staff) {
        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = 120;
                g2.setClip(new Ellipse2D.Float(0, 0, s, s));
                if (customAvatar != null) {
                    g2.drawImage(customAvatar, 0, 0, s, s, null);
                } else {
                    g2.setColor(borderColor);
                    g2.fillRect(0, 0, s, s);
                    g2.setColor(ACCENT_GOLD);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 48));
                    String init = staff.getFull_name().substring(0, 1).toUpperCase();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(init, (s-fm.stringWidth(init))/2, s/2 + 18);
                }
                if (isHovered) {
                    g2.setColor(new Color(0, 0, 0, 150));
                    g2.fillRect(0, 0, s, s);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString("EDIT", s/2 - 12, s/2 + 5);
                }
                g2.setClip(null);
                g2.setStroke(new BasicStroke(3));
                g2.setColor(ACCENT_GOLD);
                g2.draw(new Ellipse2D.Float(1.5f, 1.5f, s-3, s-3));
                g2.dispose();
            }
        };
        avatar.setBounds(40, 40, 120, 120);
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return avatar;
    }

    private JPanel getJPanel() {
        JPanel h = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cardBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));

                GradientPaint grad = new GradientPaint(0, 0, new Color(ACCENT_GOLD.getRed(), ACCENT_GOLD.getGreen(), ACCENT_GOLD.getBlue(), 30), 400, 0, new Color(0,0,0,0));
                g2.setPaint(grad);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));

                g2.setColor(borderColor);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 30, 30));
                g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(0, 200));
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        h.setOpaque(false);
        return h;
    }

    private JPanel createStatMiniCard(String label, String value, Color accent) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cardBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 50));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 20, 20));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Monospaced", Font.BOLD, 11));
        l.setForeground(new Color(120, 120, 130));

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        v.setForeground(accent);

        p.add(l, BorderLayout.NORTH); p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JPanel createInfoTile(String title, String val, String desc) {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cardBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(borderColor);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 20, 20));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.BOLD, 11)); t.setForeground(ACCENT_GOLD);
        JLabel v = new JLabel(val); v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel d = new JLabel(desc); d.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        p.add(t); p.add(v); p.add(d);
        return p;
    }

    private String mask(String s) {
        return (s == null || s.length() < 4) ? "REDACTED" : s.substring(0, 4) + " **** ****";
    }
}