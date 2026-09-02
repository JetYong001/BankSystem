package views.customerPage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StatementDialog extends JDialog {
    private Point initialClick;
    private final Color GOLD = new Color(255, 215, 0);
    private final Color BG_DARK = new Color(22, 22, 26);
    private final Color BG_CONTENT = new Color(28, 28, 32);

    public StatementDialog(Frame owner, String accNum, DefaultTableModel model) {
        super(owner, true);
        setUndecorated(true);
        setSize(500, 650);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);
        ((JPanel)getContentPane()).setBorder(BorderFactory.createLineBorder(GOLD, 2));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(BG_DARK);
        JLabel titleLabel = new JLabel("  MONTHLY STATEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        
        JButton closeBtn = new JButton("X");
        closeBtn.setBackground(new Color(60, 60, 60));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());
        
        titleBar.add(titleLabel, BorderLayout.WEST);
        titleBar.add(closeBtn, BorderLayout.EAST);
        
        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
        });
        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;
                setLocation(thisX + e.getX() - initialClick.x, thisY + e.getY() - initialClick.y);
            }
        });

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(BG_DARK);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("NEXUS BANK");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(GOLD);
        
        JLabel accInfo = new JLabel("Account: " + accNum);
        JLabel dateInfo = new JLabel("Period: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        accInfo.setForeground(Color.WHITE);
        dateInfo.setForeground(Color.WHITE);
        accInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        dateInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainContent.add(title);
        mainContent.add(Box.createVerticalStrut(10));
        mainContent.add(accInfo);
        mainContent.add(dateInfo);
        mainContent.add(Box.createVerticalStrut(20));

        JPanel tablePanel = new JPanel();
        tablePanel.setBackground(BG_CONTENT);
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String refId = model.getValueAt(i, 0).toString();
            String type = model.getValueAt(i, 3).toString();
            String amountStr = model.getValueAt(i, 6).toString();
            double amount = Double.parseDouble(amountStr);
            JLabel rowLabel = new JLabel(String.format("%-15s | %-12s | RM %s", refId, type, amountStr));
            rowLabel.setForeground(Color.WHITE);
            rowLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
            tablePanel.add(rowLabel);
            if (type.contains("IN") || type.contains("DEPOSIT")) total += amount;
            else total -= amount;
        }
        
        mainContent.add(new JScrollPane(tablePanel));
        mainContent.add(Box.createVerticalStrut(20));
        
        JLabel totalLabel = new JLabel("Total Balance Impact: RM " + String.format("%.2f", total));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(GOLD);
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContent.add(totalLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(BG_DARK);

        JButton saveBtn = new JButton("SAVE AS PDF");
        styleGoldButton(saveBtn);
        saveBtn.addActionListener(e -> printToPdf(mainContent));

        buttonPanel.add(saveBtn);

        add(titleBar, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleGoldButton(JButton btn) {
        btn.setBackground(GOLD);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(140, 35));
    }

    private void printToPdf(JPanel content) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Monthly_Statement");
        job.setPrintable((g, pf, pi) -> {
            if (pi > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            content.printAll(g2);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try { job.print(); } catch (PrinterException e) { JOptionPane.showMessageDialog(this, "Print Failed"); }
        }
    }
}
