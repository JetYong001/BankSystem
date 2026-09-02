package views.customerPage.Transfer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PinInputDialog extends CustomDialog {
    private JPasswordField pinField = new JPasswordField();
    private boolean confirmed = false;

    public PinInputDialog(Frame parent) {
        super(parent, "Verify PIN");
        
        contentPanel.add(createTitleLabel("Verify PIN"), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(bgColor);
        centerPanel.setLayout(new GridBagLayout());
        
        JLabel msgLabel = createMessageLabel("Please enter your 6-digit PIN to proceed:");
        
        pinField.setPreferredSize(new Dimension(250, 50));
        pinField.setBackground(new Color(40, 40, 40)); 
        pinField.setForeground(Color.WHITE);
        pinField.setCaretColor(goldColor);
        pinField.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        pinField.setBorder(BorderFactory.createLineBorder(goldColor, 1)); 

        pinField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    confirmed = true;
                    dispose();
                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        centerPanel.add(msgLabel, gbc);
        gbc.gridy = 1;
        centerPanel.add(pinField, gbc);
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(bgColor);

        JButton btnCancel = createStayButton("CANCEL");
        btnCancel.addActionListener(e -> { confirmed = false; dispose(); });

        JButton btnConfirm = createActionButton("CONFIRM");
        btnConfirm.addActionListener(e -> { confirmed = true; dispose(); });

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnConfirm);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setSize(DIALOG_SIZE);
        setLocationRelativeTo(parent);
    }

    public boolean showDialog() {
        pinField.setText(""); 
        pinField.requestFocusInWindow();
        setVisible(true);
        return confirmed;
    }

    public String getPin() { 
        return new String(pinField.getPassword()); 
    }
}