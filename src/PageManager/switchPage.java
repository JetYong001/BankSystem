package PageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class switchPage {
    public static JPanel container;
    public static CardLayout cardLayout;
    private static final Stack<String> history = new Stack<>();

    public static void to(String pageName) {
        if (cardLayout != null && container != null) {
            cardLayout.show(container, pageName);
        }
    }

    public static JButton createCloseButton() {

        int sw = Toolkit.getDefaultToolkit().getScreenSize().width;

        JButton button = new JButton("×");
        button.setBounds(sw - 70, 40, 30, 30);
        button.setFont(new Font("Arial", Font.BOLD, 45));
        button.setForeground(new Color(255, 255, 255, 120));
        button.setContentAreaFilled(false);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { button.setForeground(new Color(255, 255, 255, 100)); }
        });
        button.addActionListener(e -> System.exit(0));
        return button;
    }

    public static void back(){
        if(!history.isEmpty() && cardLayout != null){
            String previousPage = history.pop();
            cardLayout.show(container, previousPage);
        }
    }
}