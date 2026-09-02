package views;

import DAO.UserDAO;
import views.customerPage.Themeable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class SettingsView extends JPanel implements Themeable {
    private final Color ACCENT = new Color(255, 204, 0);
    private Color bgLight = new Color(248, 249, 252);
    private Color textMain = new Color(33, 37, 41);
    private Color textSub = new Color(108, 117, 125);
    private Color cardBG = Color.WHITE;
    private final JLabel title;
    private final JLabel subtitle;
    private final Map<String, JPanel> allPages;
    private final String username;

    public SettingsView(Map<String, JPanel> pages, String username) {
        this.allPages = pages;
        this.username = username;
        boolean initialDark = UserDAO.getThemePreference(username);
        setThemeColors(initialDark);

        setLayout(new BorderLayout());
        setBackground(bgLight);
        setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
        header.setOpaque(false);
        title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(textMain);
        subtitle = new JLabel("Manage your application preferences");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(textSub);
        header.add(title);
        header.add(subtitle);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(30, 0, 0, 0));

        content.add(createSettingCard("Dark Mode", "Adjust the visual appearance of the interface", initialDark, true));
        content.add(Box.createVerticalStrut(20));
        content.add(createSettingCard("Email Notifications", "Receive updates about your account activity", false, false));
        content.add(Box.createVerticalStrut(20));
        content.add(createSettingCard("Two-Factor Authentication", "Add an extra layer of security to your account", true, false));

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        updateTheme(initialDark);
    }

    private JPanel createSettingCard(String tStr, String dStr, boolean isSelected, boolean isThemeToggle) {
        JPanel card = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cardBG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(0, 0, 0, 15));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(1200, 100));
        card.setPreferredSize(new Dimension(850, 100));
        card.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 5));
        info.setOpaque(false);
        JLabel labelTitle = new JLabel(tStr);
        labelTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        labelTitle.setForeground(textMain);
        JLabel labelDesc = new JLabel(dStr);
        labelDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelDesc.setForeground(textSub);
        info.add(labelTitle);
        info.add(labelDesc);

        JToggleButton toggle = new JToggleButton(isSelected ? "ON" : "OFF");
        toggle.setSelected(isSelected);
        toggle.setPreferredSize(new Dimension(80, 40));
        toggle.setFocusPainted(false);
        toggle.setBackground(isSelected ? ACCENT : Color.LIGHT_GRAY);
        toggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggle.addActionListener(e -> {
            boolean dark = toggle.isSelected();
            toggle.setText(dark ? "ON" : "OFF");
            toggle.setBackground(dark ? ACCENT : Color.LIGHT_GRAY);
            if (isThemeToggle) applyGlobalTheme(dark);
        });

        card.add(info, BorderLayout.CENTER);
        card.add(toggle, BorderLayout.EAST);
        return card;
    }

    private void setThemeColors(boolean isDark) {
        if (isDark) {
            bgLight = new Color(18, 18, 18);
            cardBG = new Color(30, 30, 30);
            textMain = new Color(240, 240, 240);
            textSub = new Color(160, 160, 160);
        } else {
            bgLight = new Color(248, 249, 252);
            cardBG = Color.WHITE;
            textMain = new Color(33, 37, 41);
            textSub = new Color(108, 117, 125);
        }
    }

    private void applyGlobalTheme(boolean isDark) {
        UserDAO.saveSetting(username, isDark);
        if (allPages != null) {
            for (JPanel p : allPages.values()) {
                if (p instanceof Themeable) ((Themeable) p).updateTheme(isDark);
                else {
                    p.setBackground(isDark ? new Color(18, 18, 18) : new Color(248, 249, 252));
                    p.repaint();
                }
            }
        }
        Container parent = getTopLevelAncestor();
        while (parent != null) {
            if (parent instanceof Themeable) {
                ((Themeable) parent).updateTheme(isDark);
                break;
            }
            parent = parent.getParent();
        }
    }

    @Override
    public void updateTheme(boolean isDark) {
        setThemeColors(isDark);
        setBackground(bgLight);
        title.setForeground(textMain);
        subtitle.setForeground(textSub);
        updateChildrenColors(this);
        repaint();
    }

    private void updateChildrenColors(Container parent) {
        for (Component c : parent.getComponents()) {
            if (c instanceof JLabel label) {
                if (label == title || label == subtitle) continue;
                if (label.getFont().getSize() >= 18) label.setForeground(textMain);
                else label.setForeground(textSub);
            } else if (c instanceof Container container) {
                updateChildrenColors(container);
            }
        }
    }
}