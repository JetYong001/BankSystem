package views.staffPage.MANAFERIAL;

import DAO.RateDAO;
import views.customerPage.Themeable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;

public class RateSettingPage extends JPanel implements Themeable {
    private Color CARD_BG;
    private Color CARD_HOVER;
    private Color BORDER;
    private Color TEXT_MAIN;
    private Color TEXT_SUB;
    private Color PAGE_BG;
    private Color INPUT_BG;
    private static final Color ACCENT = new Color(255, 204, 0);
    private static final Color SAVE_GREEN = new Color(39, 174, 96);

    private boolean isDark;
    private final Map<String, JTextField> fieldMap = new HashMap<>();

    public RateSettingPage(boolean isDark) {
        this.isDark = isDark;
        updateThemeColors();
        initUI();
    }

    @Override
    public void updateTheme(boolean isDark) {
        setDarkMode(isDark); // 调用你之前写的刷新 UI 的方法
    }

    public void setDarkMode(boolean isDark) {
        this.isDark = isDark;
        updateThemeColors();
        initUI();
    }

    private void updateThemeColors() {
        if (isDark) {
            PAGE_BG = new Color(25, 25, 25);
            CARD_BG = new Color(35, 35, 35);
            CARD_HOVER = new Color(42, 42, 42);
            BORDER = new Color(50, 50, 50);
            TEXT_MAIN = new Color(240, 240, 240);
            TEXT_SUB = new Color(150, 150, 160);
            INPUT_BG = new Color(45, 45, 45);
        } else {
            PAGE_BG = new Color(248, 249, 252);
            CARD_BG = Color.WHITE;
            CARD_HOVER = new Color(252, 253, 255);
            BORDER = new Color(230, 235, 245);
            TEXT_MAIN = new Color(45, 52, 54);
            TEXT_SUB = new Color(99, 110, 114);
            INPUT_BG = new Color(245, 246, 250);
        }
    }

    private void initUI() {
        removeAll();
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(40, 60, 40, 60));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 8));
        header.setOpaque(false);

        JLabel title = new JLabel("Rate Configuration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(TEXT_MAIN);

        JLabel subtitle = new JLabel("Adjust global interest rates for banking products");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(TEXT_SUB);

        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        gbc.insets = new Insets(0, 0, 25, 0);
        content.add(createSection("Fixed Deposit Rates (p.a.)", new String[]{"3", "6", "12", "24"}), gbc);

        gbc.insets = new Insets(0, 0, 40, 0);
        content.add(createSection("Savings Account Rate (p.a.)", new String[]{"SAVINGS"}), gbc);

        JButton saveBtn = createBtn("Sync Changes", SAVE_GREEN);
        saveBtn.addActionListener(e -> saveAction());

        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btm.setOpaque(false);
        btm.add(saveBtn);

        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(btm, gbc);

        add(content, BorderLayout.CENTER);
        loadData();

        revalidate();
        repaint();
    }

    private JPanel createSection(String t, String[] keys) {
        JPanel s = new JPanel(new BorderLayout(0, 20));
        s.setOpaque(false);
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setForeground(ACCENT);
        s.add(l, BorderLayout.NORTH);

        JPanel g = new JPanel(new GridLayout(1, keys.length, 20, 0));
        g.setOpaque(false);
        for (String k : keys) g.add(createCard(k));
        s.add(g, BorderLayout.CENTER);
        return s;
    }

    private JPanel createCard(String k) {
        HCard c = new HCard(15);
        c.setLayout(new BorderLayout(0, 10));
        c.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel l = new JLabel(k.equals("SAVINGS") ? "Savings Base" : k + "m Term");
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_SUB);

        JTextField in = new JTextField();
        in.setFont(new Font("JetBrains Mono", Font.BOLD, 22));
        in.setForeground(TEXT_MAIN);
        in.setBackground(INPUT_BG);
        in.setCaretColor(ACCENT);
        in.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        fieldMap.put(k, in);

        JPanel w = new JPanel(new BorderLayout(8, 0));
        w.setOpaque(false);
        w.add(in, BorderLayout.CENTER);
        JLabel p = new JLabel("%");
        p.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p.setForeground(TEXT_SUB);
        w.add(p, BorderLayout.EAST);

        c.add(l, BorderLayout.NORTH);
        c.add(w, BorderLayout.CENTER);
        return c;
    }

    private void loadData() {
        Map<String, Double> r = RateDAO.getAllRates();
        fieldMap.forEach((k, f) -> {
            Double v = r.getOrDefault(k, 0.0);
            f.setText(String.format("%.2f", v));
        });
    }

    private void saveAction() {
        try {
            Map<String, Double> n = new HashMap<>();
            for (Map.Entry<String, JTextField> e : fieldMap.entrySet()) {
                n.put(e.getKey(), Double.parseDouble(e.getValue().getText()));
            }
            if (RateDAO.updateRates(n)) {
                JOptionPane.showMessageDialog(this, "Rates updated.");
                initUI();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid format.", "Error", 0);
        }
    }

    private JButton createBtn(String t, Color b) {
        JButton btn = new JButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(b);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(12));
        return btn;
    }

    private class HCard extends JPanel {
        private final int r;
        private boolean h = false;
        private HCard(int r) {
            this.r = r; setOpaque(false);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { h = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { h = false; repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(h ? CARD_HOVER : CARD_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));
            g2.setColor(BORDER);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, r, r));
            g2.dispose();
        }
    }
}