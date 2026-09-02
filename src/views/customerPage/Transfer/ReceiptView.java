package views.customerPage.Transfer;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReceiptView extends CustomDialog {
    private String recipient, amount, date, note, referenceId, timestamp;

    public ReceiptView(Frame parent, String recipient, double amount, Date date, String note, String referenceId, String timestamp) {
        super(parent, "Transaction Receipt");
        this.recipient = recipient;
        this.amount = String.format("%.2f", amount);
        this.date = new SimpleDateFormat("yyyy-MM-dd").format(date);
        this.note = note;
        this.referenceId = referenceId;
        this.timestamp = timestamp;

        contentPanel.add(createTitleLabel("RECEIPT"), BorderLayout.NORTH);

        // 关键点：将行数改为 6
        JPanel detailsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        detailsPanel.setBackground(bgColor);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        addReceiptRow(detailsPanel, "Reference ID:", referenceId);
        addReceiptRow(detailsPanel, "Timestamp:", timestamp);
        addReceiptRow(detailsPanel, "Recipient:", recipient);
        addReceiptRow(detailsPanel, "Amount (RM):", this.amount);
        addReceiptRow(detailsPanel, "Date:", this.date);
        addReceiptRow(detailsPanel, "Note:", note);
        
        contentPanel.add(detailsPanel, BorderLayout.CENTER);

        // ... (按钮逻辑保持不变)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setBackground(bgColor);
        JButton btnCancel = createStayButton("CANCEL");
        btnCancel.addActionListener(e -> dispose());
        JButton btnPrint = createActionButton("PRINT PDF");
        btnPrint.addActionListener(e -> { printToPDF(); dispose(); });
        btnPanel.add(btnCancel);
        btnPanel.add(btnPrint);
        contentPanel.add(btnPanel, BorderLayout.SOUTH);
        
        setSize(450, 500); // 可能需要稍微调大高度以容纳新行
        setLocationRelativeTo(parent);
    }

    private void addReceiptRow(JPanel p, String label, String value) {
        JLabel l1 = new JLabel(label); l1.setForeground(Color.GRAY);
        JLabel l2 = new JLabel(value); l2.setForeground(Color.WHITE);
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        l2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        p.add(l1); p.add(l2);
    }

    private void printToPDF() {
        // 更新 PDF 打印逻辑以包含新字段
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setColor(Color.BLACK);
            g2.drawString("TRANSACTION RECEIPT", 100, 80);
            g2.drawString("Ref ID: " + referenceId, 100, 110);
            g2.drawString("Time: " + timestamp, 100, 130);
            g2.drawString("--------------------------", 100, 150);
            g2.drawString("Recipient: " + recipient, 100, 170);
            g2.drawString("Amount: RM " + amount, 100, 190);
            g2.drawString("Date: " + date, 100, 210);
            g2.drawString("Note: " + note, 100, 230);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) { try { job.print(); } catch (PrinterException e) { e.printStackTrace(); } }
    }
}