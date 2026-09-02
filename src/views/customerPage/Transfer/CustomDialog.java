package views.customerPage.Transfer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomDialog extends JDialog {
    public JPanel contentPanel;
    public Color bgColor = new Color(0, 0, 0); 
    public Color goldColor = new Color(255, 215, 0); 
    protected Point initialClick;
    protected static final Dimension DIALOG_SIZE = new Dimension(450, 300);

    public CustomDialog(Frame parent, String title) {
        super(parent, title, true);
        setUndecorated(true);
        setPreferredSize(DIALOG_SIZE);
        
        
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(bgColor);
        contentPanel.setBorder(new LineBorder(Color.BLACK, 1)); 

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
        });
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });

        setContentPane(contentPanel);
    }

    public JButton createStayButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(50, 50, 50)); 
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(150, 50));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(70, 70, 70));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(50, 50, 50));
            }
        });
        return btn;
    }

    public JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.BLACK);
        btn.setBackground(goldColor); 
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(150, 50));
        return btn;
    }

    public JLabel createTitleLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 24));
        l.setForeground(goldColor);
        l.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        return l;
    }

    public JLabel createMessageLabel(String text) {
        JLabel l = new JLabel("<html><div style='text-align: center;'>" + text + "</div></html>", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        l.setForeground(Color.WHITE);
        l.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        return l;
    }
}