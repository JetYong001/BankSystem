package views.staffPage;

import DAO.UserDAO;
import PageManager.switchPage;
import models.User.showStaffPage;
import models.User.Staff;
import views.SettingsView;
import views.customerPage.Themeable;
import views.staffPage.BACKOFFICE.Account_Frozen_Page;
import views.staffPage.BACKOFFICE.Add_Staff_Page;
import views.staffPage.BACKOFFICE.Staff_List_Page;
import views.staffPage.FRONTLINE.Cash_Transaction_Page;
import views.staffPage.FRONTLINE.StaffTransferView;
import views.staffPage.MANAFERIAL.Customer_List_Page;
import views.staffPage.FRONTLINE.Open_Account_Page;
import views.staffPage.MANAFERIAL.ApprovalListPage;
import views.staffPage.MANAFERIAL.RateSettingPage;
import views.staffPage.STAFF.FinancialCalculator;
import views.staffPage.STAFF.Staff_Profile_Page;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Staff_Dashboard extends JPanel implements Themeable {

    static {
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.ddforcevram", "true");
    }

    private final Color ACCENT = new Color(255, 204, 0);
    private SidebarButton activeBtn = null;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private final JPanel sidebar;
    private static final List<SidebarButton> animatedButtons = new CopyOnWriteArrayList<>();
    private static Timer globalTimer;

    public Staff_Dashboard(String staffId, String role) {
        setLayout(new BorderLayout());
        initGlobalTimer();

        Staff currentStaff = UserDAO.getStaffByUsername(staffId);
        boolean isDark = UserDAO.getThemePreference(staffId);

        sidebar = new JPanel(null);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(new Color(24, 28, 33));

        JLabel brand = new JLabel("NexusBank");
        brand.setBounds(30, 40, 200, 30);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brand.setForeground(ACCENT);
        sidebar.add(brand);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        Staff_List_Page staffListPage = new Staff_List_Page(isDark);
        Customer_List_Page customerListPage = new Customer_List_Page(isDark);

        HashMap<String, JPanel> pages = new HashMap<>();
        pages.put("Profile", new Staff_Profile_Page(Objects.requireNonNull(currentStaff),isDark));
        pages.put("Setting", new SettingsView(pages, staffId));
        pages.put("Financial Calculators", new FinancialCalculator(isDark));
        pages.put("Current Cash Deposit/Withdrawal", new Cash_Transaction_Page(isDark));
        pages.put("Account Opening", new Open_Account_Page(isDark));
        pages.put("Fund Transfer", new StaffTransferView(currentStaff,isDark));
        pages.put("Customer Profiling", customerListPage);
        pages.put("Application Approval", new ApprovalListPage(isDark));
        pages.put("Staff Management", new Add_Staff_Page(isDark));
        pages.put("Role-Based Access Control", staffListPage);
        pages.put("Interest Rate Management", new RateSettingPage(isDark));
        pages.put("Account Freezing/Unfreezing", new Account_Frozen_Page(isDark));

        ArrayList<String> menu = showStaffPage.getPermissionsByRole(role);
        int y = 120;
        for (String item : menu) {
            SidebarButton btn = new SidebarButton(item);
            btn.setBounds(0, y, 260, 50);
            btn.addActionListener(e -> {
                if (activeBtn != null) activeBtn.setActive(false);
                activeBtn = btn;
                btn.setActive(true);
                SwingUtilities.invokeLater(() -> {
                    if (item.equals("Customers")) customerListPage.refreshData();
                    if (item.equals("View Staff")) staffListPage.refreshData();
                    cardLayout.show(contentPanel, item);
                });
            });
            sidebar.add(btn);
            y += 60;
        }

        if (!menu.isEmpty()) {
            String firstPage = menu.getFirst();

            cardLayout.show(contentPanel, firstPage);

            for (Component c : sidebar.getComponents()) {
                if (c instanceof SidebarButton btn && btn.getText().equals(firstPage)) {
                    if (activeBtn != null) activeBtn.setActive(false);
                    activeBtn = btn;
                    btn.setActive(true);
                    break;
                }
            }
        }

        SidebarButton logoutBtn = new SidebarButton("Logout");
        logoutBtn.setForeground(new Color(255, 100, 100));
        sidebar.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                logoutBtn.setBounds(0, sidebar.getHeight() - 70, 260, 50);
            }
        });

        logoutBtn.addActionListener(e -> {
            LogoutPopup popup = new LogoutPopup(contentPanel);
            popup.setVisible(true);
            if (popup.isConfirmed()) switchPage.to("LOGIN");
        });

        sidebar.add(logoutBtn);
        for (String key : pages.keySet()) contentPanel.add(pages.get(key), key);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        updateTheme(isDark);

        if (!menu.isEmpty()) {
            String firstPage = menu.getFirst();
            cardLayout.show(contentPanel, firstPage);

            for (Component c : sidebar.getComponents()) {
                if (c instanceof SidebarButton btn && btn.getText().equals(firstPage)) {
                    if (activeBtn != null) activeBtn.setActive(false);
                    activeBtn = btn;
                    btn.setActive(true);
                    break;
                }
            }
        }
    }

    @Override
    public void updateTheme(boolean isDark) {
        applyThemeToContainer(contentPanel, isDark);
        repaint();
        revalidate();
    }

    private void applyThemeToContainer(Container container, boolean isDarkMode) {
        Color bg = isDarkMode ? new Color(18, 18, 18) : new Color(245, 246, 250);
        Color fg = isDarkMode ? Color.WHITE : new Color(33, 37, 41);

        if (container instanceof Themeable t && container != this) {
            t.updateTheme(isDarkMode);
        } else {
            container.setBackground(bg);
            for (Component c : container.getComponents()) {
                if (c instanceof JLabel) {
                    c.setForeground(fg);
                }
                if (c instanceof Container) {
                    applyThemeToContainer((Container) c, isDarkMode);
                }
            }
        }
    }

    private void initGlobalTimer() {
        if (globalTimer == null) {
            globalTimer = new Timer(8, e -> {
                for (SidebarButton btn : animatedButtons) {
                    btn.updateAnimation();
                }
            });
            globalTimer.setCoalesce(true);
            globalTimer.start();
        }
    }


    private JPanel createSimplePage(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(label);
        return panel;
    }

    private class SidebarButton extends JButton {
        private float alpha = 0f;
        private boolean isHovered = false;
        private boolean isActive = false;
        private final Color shadowColor = new Color(0, 0, 0, 15);
        private GradientPaint cachedGradient = null;

        public SidebarButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(0, 30, 0, 0));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setForeground(Color.LIGHT_GRAY);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    if (!animatedButtons.contains(SidebarButton.this)) animatedButtons.add(SidebarButton.this);
                }
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    if (!animatedButtons.contains(SidebarButton.this)) animatedButtons.add(SidebarButton.this);
                }
            });
        }

        public void updateAnimation() {
            float target = (isHovered || isActive) ? 1f : 0f;
            float diff = target - alpha;

            if (Math.abs(diff) < 0.005f) {
                alpha = target;
                animatedButtons.remove(this);
            } else {
                alpha += diff * 0.12f;
            }

            if (!getForeground().equals(new Color(255, 100, 100))) {
                setForeground((isActive || alpha > 0.5f) ? Color.BLACK : Color.LIGHT_GRAY);
            }
            repaint();
        }

        public void setActive(boolean a) {
            this.isActive = a;
            if (!animatedButtons.contains(this)) animatedButtons.add(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (alpha > 0) {
                int w = getWidth();
                int h = getHeight();
                int drawW = (int) (w * alpha);

                g2.setColor(shadowColor);
                g2.fillRect(2, 2, drawW, h);

                if (cachedGradient == null || cachedGradient.getPoint2().getX() != w) {
                    cachedGradient = new GradientPaint(0, 0, ACCENT, w, 0, new Color(255, 230, 120));
                }

                g2.setPaint(cachedGradient);
                g2.fillRect(0, 0, drawW, h);
                g2.translate((int)(alpha * 12), 0);
            }

            super.paintComponent(g2);
            g2.dispose();
        }
    }

    class LogoutPopup extends JDialog {
        private boolean confirmed = false;
        private float opacity = 0f;

        public LogoutPopup(JPanel parentContent) {
            super(SwingUtilities.getWindowAncestor(parentContent), Dialog.ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            setSize(450, 250);
            setBackground(new Color(0, 0, 0, 0));
            setOpacity(0f);

            JPanel bg = getJPanel();

            JLabel title = new JLabel("Confirm Logout", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setForeground(ACCENT);
            title.setBounds(0, 40, 450, 30);
            bg.add(title);

            JLabel desc = new JLabel("Are you sure you want to end your session?", SwingConstants.CENTER);
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            desc.setForeground(new Color(200, 200, 200));
            desc.setBounds(0, 85, 450, 25);
            bg.add(desc);

            JButton cancelBtn = createBtn("STAY HERE", new Color(60, 63, 70), Color.WHITE);
            cancelBtn.setBounds(50, 160, 165, 50);
            cancelBtn.addActionListener(e -> close(false));
            bg.add(cancelBtn);

            JButton logoutBtn = createBtn("LOGOUT", ACCENT, Color.BLACK);
            logoutBtn.setBounds(235, 160, 165, 50);
            logoutBtn.addActionListener(e -> close(true));
            bg.add(logoutBtn);

            add(bg);
            Point p = parentContent.getLocationOnScreen();
            setLocation(p.x + (parentContent.getWidth() - 450) / 2, p.y + (parentContent.getHeight() - 250) / 2);

            Timer t = new Timer(8, e -> {
                opacity = Math.min(1f, opacity + 0.05f);
                setOpacity(opacity);
                if (opacity >= 1f) ((Timer)e.getSource()).stop();
            });
            t.start();
        }

        private JPanel getJPanel() {
            JPanel bg = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(33, 37, 43));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                    g2.dispose();
                }
            };
            bg.setOpaque(false);
            return bg;
        }

        private JButton createBtn(String t, Color bg, Color fg) {
            JButton b = new JButton(t);
            b.setFont(new Font("Segoe UI", Font.BOLD, 13));
            b.setBackground(bg);
            b.setForeground(fg);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return b;
        }

        private void close(boolean res) {
            confirmed = res;
            Timer t = new Timer(8, e -> {
                opacity = Math.max(0f, opacity - 0.08f);
                setOpacity(opacity);
                if (opacity <= 0f) {
                    ((Timer)e.getSource()).stop();
                    dispose();
                }
            });
            t.start();
        }

        public boolean isConfirmed() { return confirmed; }
    }
}