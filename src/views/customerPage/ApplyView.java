package views.customerPage;

import DAO.AccountDAO;
import DAO.ApplyDAO;
import models.Application.ApplicationRecord;
import models.Card.Card;
import models.Account.Account;
import models.User.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ApplyView extends JPanel implements Themeable {
    private static final Color ACCENT = new Color(255, 204, 0);
    private static final Color ACCENT_SOFT = new Color(255, 230, 120);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+ -]{8,15}$");
    private static final Pattern POSTCODE_PATTERN = Pattern.compile("^\\d{5}$");
    private static final Pattern MONEY_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    private final Customer customer;
    private final String username;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final JLabel title;
    private final JLabel subtitle;
    private final SelectionCard savingsAccount;
    private final SelectionCard creditCard;
    private final SelectionCard subcard;
    private final JButton historyButton;

    private Color currentBG;
    private Color currentCardBG;
    private Color currentTextMain;
    private Color currentTextSub;
    private Color currentBorder;

    public ApplyView(Customer customer,boolean isDark) {
        this.customer = customer;
        this.username = customer != null ? customer.getUsername() : "";

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 50, 40, 50));

        title = new JLabel("Application Page");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        subtitle = new JLabel("Choose the service you want to apply for");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        historyButton = new JButton("Application History");
        historyButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        historyButton.setFocusPainted(false);
        historyButton.setBorderPainted(false);
        historyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        historyButton.addActionListener(e -> showApplicationHistory());


        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 5));
        headerText.setOpaque(false);
        headerText.add(title);
        headerText.add(subtitle);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(headerText, BorderLayout.WEST);

        JPanel historyWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 18));
        historyWrapper.setOpaque(false);
        historyWrapper.add(historyButton);
        header.add(historyWrapper, BorderLayout.EAST);


        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        savingsAccount = new SelectionCard(
                "Apply for Savings Account",
                "Open a savings account to grow your money\n\nwith competitive interest rates."
        );
        creditCard = new SelectionCard(
                "Apply for New Credit Card",
                "Submit a new credit card request with your identity, employment, income, and preferred\n\ncard details."
        );
        subcard = new SelectionCard(
                "Apply for Subcard",
                "Request an additional supplementary card for\n\na family member."
        );

        JPanel selectionPage = buildSelectionPage();
        SavingsFormPanel savingsFormPanel = new SavingsFormPanel();
        CreditCardFormPanel creditCardFormPanel = new CreditCardFormPanel();
        SubcardFormPanel subcardFormPanel = new SubcardFormPanel();

        cardPanel.add(selectionPage, "SELECT");
        cardPanel.add(savingsFormPanel, "SAVINGS");
        cardPanel.add(creditCardFormPanel, "CREDIT_CARD");
        cardPanel.add(subcardFormPanel,"SUBCARD");

        savingsAccount.button.addActionListener(e -> {
            savingsFormPanel.prefillFromCustomer();
            cardLayout.show(cardPanel, "SAVINGS");
        });
        creditCard.button.addActionListener(e -> {
            creditCardFormPanel.prefillFromCustomer();
            cardLayout.show(cardPanel, "CREDIT_CARD");
        });
        subcard.button.addActionListener(e -> {
            subcardFormPanel.prefillFromCustomer();
            cardLayout.show(cardPanel, "SUBCARD");
        });

        add(header, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        updateTheme(isDark);
        cardLayout.show(cardPanel, "SELECT");
    }

    private JPanel buildSelectionPage() {
        JPanel page = new JPanel(new GridLayout(1, 3, 25, 0));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(35, 0, 0, 0));
        page.add(savingsAccount);
        page.add(creditCard);
        page.add(subcard);
        return page;
    }

    @Override
    public void updateTheme(boolean isDarkMode) {
        if (isDarkMode) {
            currentBG = new Color(18, 18, 18);
            currentCardBG = new Color(30, 30, 30);
            currentTextMain = Color.WHITE;
            currentTextSub = new Color(160, 160, 160);
            currentBorder = new Color(255, 255, 255, 20);
        } else {
            currentBG = new Color(248, 249, 252);
            currentCardBG = Color.WHITE;
            currentTextMain = new Color(33, 37, 41);
            currentTextSub = new Color(108, 117, 125);
            currentBorder = new Color(0, 0, 0, 20);
        }
        historyButton.setBackground(isDarkMode ? ACCENT : new Color(255, 220, 90));
        historyButton.setForeground(Color.BLACK);


        setBackground(currentBG);
        title.setForeground(currentTextMain);
        subtitle.setForeground(currentTextSub);
        updateChildrenTheme(this);
        repaint();
        revalidate();
    }

    private void updateChildrenTheme(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof Themeable themeable && component != this) {
                themeable.updateTheme(currentBG.getRed() < 30);
            } else if (component instanceof Container childContainer) {
                updateChildrenTheme(childContainer);
            }
        }
    }

    private JPanel createForm(JPanel content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(25, 0, 0, 0));
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private boolean isDarkModeActive() {
        return currentBG != null && currentBG.getRed() < 30;
    }

    private String safeCustomerValue(String value) {
        return value == null ? "" : value;
    }

    private String getCustomerValue(String... methodNames) {
        if (customer == null) {
            return "";
        }
        for (String methodName : methodNames) {
            try {
                Method method = customer.getClass().getMethod(methodName);
                Object result = method.invoke(customer);
                if (result != null) {
                    return result.toString();
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    private String getPrimaryCardNumber() {
        if (customer == null || customer.getAccounts() == null || customer.getAccounts().isEmpty()) {
            return "";
        }

        for (Account account : customer.getAccounts()) {
            if (account == null) continue;

            Card[] cards = account.getCardNumber();
            if (cards != null && cards.length > 0 && cards[0] != null) {
                return cards[0].getCardNumber();
            }
        }

        return "";
    }

    private JPanel emptyCell() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private void showSelection() {
        cardLayout.show(cardPanel, "SELECT");
    }

    private void showApplicationHistory() {
        java.util.List<ApplicationRecord> records = ApplyDAO.getApplicationsByUsername(username);

        if (records == null || records.isEmpty()) {
            showNoRecordsDialog();
        } else {
            showHistoryDialog(records);
        }
        return;

    }

    private void showNoRecordsDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Application History", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setSize(420, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout(0, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = isDarkModeActive() ? new Color(30, 30, 30) : Color.WHITE;
                Color border = isDarkModeActive() ? new Color(255, 204, 0, 80) : new Color(220, 220, 220);

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);

                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel titleLabel = new JLabel("Application History", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(isDarkModeActive() ? ACCENT : new Color(33, 37, 41));

        JLabel messageLabel = new JLabel("No records", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        messageLabel.setForeground(isDarkModeActive() ? Color.WHITE : new Color(90, 90, 90));

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setBackground(ACCENT);
        okButton.setForeground(Color.BLACK);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setPreferredSize(new Dimension(100, 40));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(titleLabel);
        centerPanel.add(messageLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showHistoryDialog(java.util.List<ApplicationRecord> records) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Application History", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setSize(820, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout(0, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = isDarkModeActive() ? new Color(30, 30, 30) : Color.WHITE;
                Color border = isDarkModeActive() ? new Color(255, 204, 0, 80) : new Color(220, 220, 220);

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);

                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel titleLabel = new JLabel("Application History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(isDarkModeActive() ? ACCENT : new Color(33, 37, 41));


        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(titleLabel, BorderLayout.WEST);

        String[] columns = {"Application ID", "Type", "Submitted At", "Status"};
        String[][] data = new String[records.size()][4];

        for (int i = 0; i < records.size(); i++) {
            ApplicationRecord record = records.get(i);
            data[i][0] = record.getApplicationId();
            data[i][1] = formatApplicationType(record.getApplicationType());
            data[i][2] = record.getSubmittedAt();
            data[i][3] = record.getStatus();
        }

        JTable table = new JTable(data, columns);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setEnabled(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(255, 240, 180));

        if (isDarkModeActive()) {
            table.setBackground(new Color(36, 36, 36));
            table.setForeground(Color.WHITE);
            table.setGridColor(new Color(70, 70, 70));
            table.getTableHeader().setBackground(new Color(255, 204, 0));
            table.getTableHeader().setForeground(Color.BLACK);
        } else {
            table.setBackground(new Color(250, 250, 250));
            table.setForeground(new Color(33, 37, 41));
            table.setGridColor(new Color(220, 220, 220));
            table.getTableHeader().setBackground(new Color(255, 204, 0));
            table.getTableHeader().setForeground(Color.BLACK);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(isDarkModeActive() ? new Color(36, 36, 36) : new Color(250, 250, 250));

        JButton okButton = new JButton("Close");
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setBackground(ACCENT);
        okButton.setForeground(Color.BLACK);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setPreferredSize(new Dimension(110, 40));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.add(okButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private String formatApplicationType(String type) {
        if (type == null) return "";
        return switch (type) {
            case "SAVINGS_ACCOUNT" -> "Savings Account";
            case "NEW_CREDIT_CARD" -> "New Credit Card";
            case "SUBCARD" -> "Subcard";
            default -> type;
        };
    }

    private abstract class AbstractFormPanel extends JPanel implements Themeable {
        protected final JLabel formTitle;
        protected final JLabel formSubtitle;
        protected final JButton closeButton;
        protected final JPanel bodyPanel;
        protected final JButton submitButton;
        protected final Map<String, JComponent> fields = new LinkedHashMap<>();

        protected AbstractFormPanel(String pageTitle, String pageSubtitle) {
            setLayout(new BorderLayout());
            setOpaque(false);

            formTitle = new JLabel(pageTitle);
            formTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
            formSubtitle = new JLabel(pageSubtitle);
            formSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));

            closeButton = new JButton("x");
            closeButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setFocusPainted(false);
            closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeButton.addActionListener(e -> showSelection());

            JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 4));
            headerText.setOpaque(false);
            headerText.add(formTitle);
            headerText.add(formSubtitle);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.add(headerText, BorderLayout.WEST);
            header.add(closeButton, BorderLayout.EAST);

            bodyPanel = new RoundedFormCard();
            bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
            bodyPanel.setBorder(new EmptyBorder(30, 35, 35, 35));
            bodyPanel.setOpaque(false);

            submitButton = new JButton("Submit Application");
            submitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            submitButton.setBackground(ACCENT);
            submitButton.setForeground(Color.BLACK);
            submitButton.setFocusPainted(false);
            submitButton.setBorderPainted(false);
            submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            submitButton.setPreferredSize(new Dimension(220, 46));

            JPanel content = new JPanel(new BorderLayout());
            content.setOpaque(false);
            content.add(header, BorderLayout.NORTH);
            content.add(bodyPanel, BorderLayout.CENTER);

            add(createForm(content), BorderLayout.CENTER);
        }

        protected void addSectionTitle(String text) {
            JLabel label = new JLabel(text);
            label.setFont(new Font("Segoe UI", Font.BOLD, 18));
            label.setBorder(new EmptyBorder(0, 0, 15, 0));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            bodyPanel.add(label);
        }

        protected void addFieldRow(JComponent left, JComponent right) {
            JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.add(left);
            row.add(right);
            bodyPanel.add(row);
            bodyPanel.add(Box.createVerticalStrut(18));
        }

        protected JPanel createTextField(String key, String label, String placeholder) {
            JTextField textField = new JTextField();
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textField.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            fields.put(key, textField);
            return createFieldContainer(label, placeholder, textField);
        }

        protected JPanel createAreaField(String key, String label, String placeholder) {
            JTextArea textArea = new JTextArea(4, 20);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            fields.put(key, textArea);
            return createFieldContainer(label, placeholder, new JScrollPane(textArea));
        }

        protected JPanel createComboField(String key, String label, String[] options) {
            JComboBox<String> comboBox = new JComboBox<>(options);
            comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            fields.put(key, comboBox);
            return createFieldContainer(label, "", comboBox);
        }

        protected JPanel createFieldContainer(String label, String helper, JComponent input) {
            JPanel container = new JPanel(new BorderLayout(0, 8));
            container.setOpaque(false);

            JLabel topLabel = new JLabel(label);
            topLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

            JPanel inputShell = new RoundedInputPanel();
            inputShell.setLayout(new BorderLayout());
            inputShell.setOpaque(false);

            if (input instanceof JScrollPane scrollPane) {
                scrollPane.setBorder(null);
                scrollPane.setOpaque(false);
                scrollPane.getViewport().setOpaque(false);
                inputShell.add(scrollPane, BorderLayout.CENTER);
                scrollPane.setPreferredSize(new Dimension(0, 96));
            } else {
                input.setPreferredSize(new Dimension(0, 44));
                inputShell.add(input, BorderLayout.CENTER);
            }

            container.add(topLabel, BorderLayout.NORTH);
            container.add(inputShell, BorderLayout.CENTER);
            if (!helper.isBlank()) {
                JLabel helperLabel = new JLabel(helper);
                helperLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                container.add(helperLabel, BorderLayout.SOUTH);
            }
            return container;
        }

        protected JPanel createSubmitRow() {
            JPanel submitRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
            submitRow.setOpaque(false);
            submitRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            submitRow.add(submitButton);
            return submitRow;
        }

        protected String valueOf(String key) {
            JComponent component = fields.get(key);
            if (component instanceof JTextComponent textComponent) {
                return textComponent.getText().trim();
            }
            if (component instanceof JComboBox<?> comboBox) {
                Object selected = comboBox.getSelectedItem();
                return selected == null ? "" : selected.toString().trim();
            }
            return "";
        }

        protected void setValue(String key, String value) {
            JComponent component = fields.get(key);
            if (component instanceof JTextComponent textComponent) {
                textComponent.setText(value);
            } else if (component instanceof JComboBox<?> comboBox) {
                comboBox.setSelectedItem(value);
            }
        }

        protected void clearForm() {
            for (JComponent component : fields.values()) {
                if (component instanceof JTextComponent textComponent) {
                    textComponent.setText("");
                } else if (component instanceof JComboBox<?> comboBox && comboBox.getItemCount() > 0) {
                    comboBox.setSelectedIndex(0);
                }
            }
        }

        protected boolean validateRequired(String key, String label) {
            if (valueOf(key).isBlank()) {
                showError(label + " is required.");
                return false;
            }
            return true;
        }

        protected boolean validateEmail(String key) {
            if (!EMAIL_PATTERN.matcher(valueOf(key)).matches()) {
                showError("Please enter a valid email address.");
                return false;
            }
            return true;
        }

        protected boolean validatePhone(String key) {
            if (!PHONE_PATTERN.matcher(valueOf(key)).matches()) {
                showError("Please enter a valid phone number.");
                return false;
            }
            return true;
        }

        protected boolean validatePostcode(String key) {
            if (!POSTCODE_PATTERN.matcher(valueOf(key)).matches()) {
                showError("Postcode must be exactly 5 digits.");
                return false;
            }
            return true;
        }

        protected boolean validateMoney(String key, String label) {
            String value = valueOf(key);
            if (!MONEY_PATTERN.matcher(value).matches() || Double.parseDouble(value) <= 0) {
                showError(label + " must be a valid amount greater than 0.");
                return false;
            }
            return true;
        }

        protected boolean validateAdult(String key) {
            try {
                LocalDate dob = LocalDate.parse(valueOf(key));
                if (Period.between(dob, LocalDate.now()).getYears() < 18) {
                    showError("Applicant must be at least 18 years old.");
                    return false;
                }
                return true;
            } catch (DateTimeParseException e) {
                showError("Date of birth must use format YYYY-MM-DD.");
                return false;
            }
        }

        protected boolean validateDate(String key, String label) {
            try {
                LocalDate.parse(valueOf(key));
                return true;
            } catch (DateTimeParseException e) {
                showError(label + " must use format YYYY-MM-DD.");
                return false;
            }
        }

        protected void showError(String message) {
            JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
        }

        protected void showSuccess(String message) {
            JOptionPane.showMessageDialog(this, message, "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
        }

        @Override
        public void updateTheme(boolean isDarkMode) {
            if (isDarkMode) {
                currentBG = new Color(18, 18, 18);
                currentCardBG = new Color(30, 30, 30);
                currentTextMain = Color.WHITE;
                currentTextSub = new Color(160, 160, 160);
                currentBorder = new Color(255, 255, 255, 35);
            } else {
                currentBG = new Color(248, 249, 252);
                currentCardBG = new Color(252, 252, 252);
                currentTextMain = new Color(33, 37, 41);
                currentTextSub = new Color(108, 117, 125);
                currentBorder = new Color(210, 216, 224);
            }
            historyButton.setBackground(isDarkMode ? ACCENT : new Color(255, 220, 90));
            historyButton.setForeground(Color.BLACK);

            setBackground(currentBG);
            title.setForeground(currentTextMain);
            subtitle.setForeground(currentTextSub);
            updateChildrenTheme(this);
            updateComponentColors(this);
            repaint();
            revalidate();
        }


        private void updateComponentColors(Container parent) {
            for (Component component : parent.getComponents()) {
                if (component instanceof JLabel label) {
                    if (label != formTitle && label != formSubtitle) {
                        label.setForeground(label.getFont().getSize() >= 17 ? currentTextMain : currentTextSub);
                    }
                } else if (component instanceof JTextField textField) {
                    textField.setBackground(isDarkModeActive() ? new Color(40, 40, 40) : new Color(245, 247, 250));
                    textField.setForeground(isDarkModeActive() ? Color.WHITE : new Color(33, 37, 41));
                    textField.setCaretColor(isDarkModeActive() ? Color.WHITE : new Color(33, 37, 41));
                    textField.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                } else if (component instanceof JTextArea textArea) {
                    textArea.setBackground(isDarkModeActive() ? new Color(40, 40, 40) : new Color(245, 247, 250));
                    textArea.setForeground(isDarkModeActive() ? Color.WHITE : new Color(33, 37, 41));
                    textArea.setCaretColor(isDarkModeActive() ? Color.WHITE : new Color(33, 37, 41));
                    textArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                } else if (component instanceof JComboBox<?> comboBox) {
                    comboBox.setBackground(isDarkModeActive() ? new Color(40, 40, 40) : new Color(245, 247, 250));
                    comboBox.setForeground(isDarkModeActive() ? Color.WHITE : new Color(33, 37, 41));
                }

                if (component instanceof Container container) {
                    updateComponentColors(container);
                }
            }
        }


        protected abstract void prefillFromCustomer();
    }

    private class SavingsFormPanel extends AbstractFormPanel {
        public SavingsFormPanel() {
            super("Savings Account Application", "Complete the savings account application form to open your savings account.");

            addSectionTitle("Savings Account Details");
            addFieldRow(
                    createTextField("fullName", "Full Name", "Enter your full name"),
                    createTextField("identityNumber", "IC / Passport Number", "Enter your IC or passport number")
            );
            addFieldRow(
                    createComboField("accountType", "Account Type", new String[]{
                            "Select account type",
                            "Regular Savings",
                            "Fixed Deposit (3 Months)",
                            "Fixed Deposit (6 Months)",
                            "Fixed Deposit (12 Months)",
                            "Fixed Deposit (24 Months)"
                    }),
                    createTextField("initialDeposit", "Initial Deposit (RM)", "Example: 1000.00")
            );
            addFieldRow(
                    createAreaField("addressLine", "Address", "Enter your full address"),
                    createAreaField("purpose", "Account Purpose", "Describe the expected account usage")
            );
            addFieldRow(
                    createTextField("city", "City", "City"),
                    createTextField("state", "State", "State")
            );
            addFieldRow(
                    createTextField("postCode", "Postcode", "5-digit postcode"),
                    emptyCell()
            );

            addFieldRow(
                    createTextField("email", "Email Address", "name@example.com"),
                    createTextField("phoneNumber", "Phone Number", "Example: 0123456789")
            );

            bodyPanel.add(createSubmitRow());
            submitButton.addActionListener(e -> submitSavingsApplication());
        }

        @Override
        protected void prefillFromCustomer() {
            setValue("fullName", safeCustomerValue(getCustomerValue("getFull_name")));
            setValue("identityNumber", safeCustomerValue(getCustomerValue("getIcNumber")));
            setValue("email", safeCustomerValue(getCustomerValue("getGmail")));
            setValue("phoneNumber", safeCustomerValue(getCustomerValue("getPhoneNumber")));
        }

        private void submitSavingsApplication() {

            String selectedType = valueOf("accountType");
            String typeForDB = "SAVINGS_ACCOUNT";
            String tenure = "";

            if (selectedType.toUpperCase().contains("FIXED DEPOSIT")) {
                typeForDB = "FIXED_DEPOSIT";
                tenure = selectedType.replaceAll("[^0-9]", "");
            }

            boolean valid = validateRequired("fullName", "Full name")
                    && validateRequired("identityNumber", "IC / Passport number")
                    && !valueOf("accountType").startsWith("Select")
                    && validateRequired("initialDeposit", "Initial deposit")
                    && validateMoney("initialDeposit", "Initial deposit")
                    && validateRequired("addressLine", "Registered business address")
                    && validateRequired("purpose", "Account purpose")
                    && validateRequired("city", "City")
                    && validateRequired("state", "State")
                    && validateRequired("postCode", "Postcode")
                    && validatePostcode("postCode")
                    && validateRequired("email", "Email address")
                    && validateEmail("email")
                    && validateRequired("phoneNumber", "Phone number")
                    && validatePhone("phoneNumber");


            if (!valid) {
                if (valueOf("accountType").startsWith("Select")) {
                    showError("Please choose an account type.");
                }
                return;
            }

            double requiredDeposit = Double.parseDouble(valueOf("initialDeposit"));
            String customerId = customer.getCustomerID();
            String acc = AccountDAO.findCurrentAccountNumber(customerId);
            System.out.println(acc);



            String result = AccountDAO.processJsonTransaction(acc, String.valueOf(requiredDeposit), "CURRENT");


            if (result.equals("INSUFFICIENT_FUNDS")) {
                showError("Termination: Your Current Account has insufficient funds to cover the initial deposit.");
                return;
            } else if (!result.equals("SUCCESS")) {
                showError("Transaction Error: Unable to process payment from Current Account (" + result + ").");
                return;
            }

            if (selectedType.contains("Fixed Deposit")) {
                typeForDB = "FIXED_DEPOSIT";
                tenure = selectedType.replaceAll("[^0-9]", "");
            }

            ApplicationRecord record = new ApplicationRecord(
                    ApplyDAO.generateApplicationId(typeForDB.equals("FIXED_DEPOSIT") ? "FD" : "SA"),
                    typeForDB,
                    username,
                    "PENDING",
                    ApplyDAO.nowForDisplay(),
                    valueOf("fullName"),
                    valueOf("email"),
                    valueOf("phoneNumber"),
                    valueOf("identityNumber"),
                    valueOf("addressLine"),
                    valueOf("city"),
                    valueOf("state"),
                    valueOf("postCode"),
                    valueOf("initialDeposit"),
                    tenure,
                    valueOf("purpose"),
                    "",
                    "",
                    "",
                    "",
                    "false",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
            );

            if (ApplyDAO.saveApplication(record)) {
                showSuccess("Savings account application submitted successfully. Staff can now review it from the shared application records.");
                clearForm();
                prefillFromCustomer();
                showSelection();
            } else {
                showError("Unable to save the application right now.");
            }
        }
    }

    private class CreditCardFormPanel extends AbstractFormPanel {
        public CreditCardFormPanel() {
            super("New Credit Card Application", "Submit your personal and employment information to request a new credit card.");

            addSectionTitle("Personal Information");
            addFieldRow(
                    createTextField("fullName", "Full Name", "Name as per IC / passport"),
                    createTextField("identityNumber", "IC / Passport Number", "NRIC or passport number")
            );
            addFieldRow(
                    createTextField("dateOfBirth", "Date of Birth", "YYYY-MM-DD"),
                    createTextField("phoneNumber", "Phone Number", "Example: 0123456789")
            );
            addFieldRow(
                    createTextField("email", "Email Address", "name@example.com"),
                    createComboField("existingCustomer", "Existing NexusBank Customer", new String[]{"Select option", "Yes", "No"})
            );

            addSectionTitle("Residential Details");
            addFieldRow(
                    createAreaField("addressLine", "Residential Address", "Enter your current residential address"),
                    createTextField("occupation", "Occupation", "Current occupation")
            );
            addFieldRow(
                    createTextField("city", "City", "City"),
                    createTextField("state", "State", "State")
            );
            addFieldRow(
                    createTextField("postCode", "Postcode", "5-digit postcode"),
                    createTextField("employerName", "Employer Name", "Company or employer name")
            );

            addSectionTitle("Financial Information");
            addFieldRow(
                    createTextField("monthlyIncome", "Monthly Income (RM)", "Example: 5000.00"),
                    createComboField("cardType", "Requested Card Type", new String[]{"Select card type", "Classic Credit Card", "Gold Credit Card", "Platinum Credit Card"})
            );
            addFieldRow(
                    createComboField("requestedLimit", "Requested Credit Limit", new String[]{"Select credit limit", "RM 2,000", "RM 5,000", "RM 10,000", "RM 20,000", "RM 50,000"}),
                    emptyCell()
            );

            bodyPanel.add(createSubmitRow());
            submitButton.addActionListener(e -> submitCreditCardApplication());
        }

        @Override
        protected void prefillFromCustomer() {
            setValue("fullName", safeCustomerValue(getCustomerValue("getFull_name")));
            setValue("identityNumber", safeCustomerValue(getCustomerValue("getIcNumber")));
            setValue("email", safeCustomerValue(getCustomerValue("getGmail")));
            setValue("phoneNumber", safeCustomerValue(getCustomerValue("getPhoneNumber")));
            setValue("existingCustomer", username.isBlank() ? "No" : "Yes");
        }

        private void submitCreditCardApplication() {
            boolean valid = validateRequired("fullName", "Full name")
                    && validateRequired("identityNumber", "IC / Passport number")
                    && validateRequired("dateOfBirth", "Date of birth")
                    && validateAdult("dateOfBirth")
                    && validateDate("dateOfBirth", "Date of birth")
                    && validateRequired("phoneNumber", "Phone number")
                    && validatePhone("phoneNumber")
                    && validateRequired("email", "Email address")
                    && validateEmail("email")
                    && !valueOf("existingCustomer").startsWith("Select")
                    && validateRequired("addressLine", "Residential address")
                    && validateRequired("occupation", "Occupation")
                    && validateRequired("city", "City")
                    && validateRequired("state", "State")
                    && validateRequired("postCode", "Postcode")
                    && validatePostcode("postCode")
                    && validateRequired("employerName", "Employer name")
                    && validateRequired("monthlyIncome", "Monthly income")
                    && validateMoney("monthlyIncome", "Monthly income")
                    && !valueOf("cardType").startsWith("Select")
                    && !valueOf("requestedLimit").startsWith("Select");

            if (!valid) {
                if (valueOf("existingCustomer").startsWith("Select")) {
                    showError("Please choose whether you are an existing customer.");
                } else if (valueOf("cardType").startsWith("Select")) {
                    showError("Please choose a requested card type.");
                } else if (valueOf("requestedLimit").startsWith("Select")) {
                    showError("Please choose a requested credit limit.");
                }
                return;
            }

            ApplicationRecord record = new ApplicationRecord(
                    ApplyDAO.generateApplicationId("CC"),
                    "NEW_CREDIT_CARD",
                    username,
                    "PENDING",
                    ApplyDAO.nowForDisplay(),
                    valueOf("fullName"),
                    valueOf("email"),
                    valueOf("phoneNumber"),
                    valueOf("identityNumber"),
                    valueOf("addressLine"),
                    valueOf("city"),
                    valueOf("state"),
                    valueOf("postCode"),
                    "",
                    "",
                    "",
                    valueOf("dateOfBirth"),
                    valueOf("employerName"),
                    valueOf("occupation"),
                    valueOf("monthlyIncome"),
                    valueOf("existingCustomer"),
                    valueOf("cardType"),
                    valueOf("requestedLimit"),
                    "",
                    "",
                    "",
                    ""
            );

            if (ApplyDAO.saveApplication(record)) {
                showSuccess("Credit card application submitted successfully.");
                clearForm();
                prefillFromCustomer();
                showSelection();
            } else {
                showError("Unable to save the application right now.");
            }
        }
    }

    private class SubcardFormPanel extends AbstractFormPanel {
        public SubcardFormPanel() {
            super("Subcard Application", "Request a supplementary card linked to an existing primary credit card.");

            addSectionTitle("Primary Card Details");
            addFieldRow(
                    createTextField("primaryCardholderName", "Primary Cardholder Name", "Full name of the principal cardholder"),
                    createTextField("primaryCardNumber", "Primary Card Number", "Enter full card number")
            );
            addFieldRow(
                    createComboField("relationshipToPrimary", "Relationship to Primary Cardholder", new String[]{"Select relationship", "Spouse", "Parent", "Child", "Sibling", "Employee", "Other"}),
                    createComboField("cardType", "Linked Card Type", new String[]{"Select card type", "Classic Credit Card", "Gold Credit Card", "Platinum Credit Card"})
            );

            addSectionTitle("Subcard Holder Information");
            addFieldRow(
                    createTextField("subcardName", "Subcard Holder Name", "Name to emboss on card"),
                    createTextField("identityNumber", "IC / Passport Number", "NRIC or passport number")
            );
            addFieldRow(
                    createTextField("dateOfBirth", "Date of Birth", "YYYY-MM-DD"),
                    createTextField("phoneNumber", "Phone Number", "Example: 0123456789")
            );
            addFieldRow(
                    createTextField("email", "Email Address", "name@example.com"),
                    createTextField("occupation", "Occupation", "Current occupation")
            );
            addFieldRow(
                    createAreaField("addressLine", "Residential Address", "Enter the current residential address"),
                    createAreaField("purpose", "Reason for Subcard", "Example: family spending, emergency use, company expenses")
            );
            addFieldRow(
                    createTextField("city", "City", "City"),
                    createTextField("state", "State", "State")
            );
            addFieldRow(
                    createTextField("postCode", "Postcode", "5-digit postcode"),
                    createTextField("monthlyIncome", "Monthly Income (RM)", "Example: 3000.00")
            );

            bodyPanel.add(createSubmitRow());
            submitButton.addActionListener(e -> submitSubcardApplication());
        }

        @Override
        protected void prefillFromCustomer() {
            setValue("primaryCardholderName", safeCustomerValue(getCustomerValue("getFull_name")));
            setValue("primaryCardNumber", safeCustomerValue(getPrimaryCardNumber()));
        }

        private void submitSubcardApplication() {
            boolean valid = validateRequired("primaryCardholderName", "Primary cardholder name")
                    && validateRequired("primaryCardNumber", "Primary card number")
                    && valueOf("primaryCardNumber").replaceAll("\\s+", "").matches("\\d{16}")
                    && !valueOf("relationshipToPrimary").startsWith("Select")
                    && !valueOf("cardType").startsWith("Select")
                    && validateRequired("subcardName", "Subcard holder name")
                    && validateRequired("identityNumber", "IC / Passport number")
                    && validateRequired("dateOfBirth", "Date of birth")
                    && validateAdult("dateOfBirth")
                    && validateDate("dateOfBirth", "Date of birth")
                    && validateRequired("phoneNumber", "Phone number")
                    && validatePhone("phoneNumber")
                    && validateRequired("email", "Email address")
                    && validateEmail("email")
                    && validateRequired("occupation", "Occupation")
                    && validateRequired("addressLine", "Residential address")
                    && validateRequired("purpose", "Reason for subcard")
                    && validateRequired("city", "City")
                    && validateRequired("state", "State")
                    && validateRequired("postCode", "Postcode")
                    && validatePostcode("postCode")
                    && validateRequired("monthlyIncome", "Monthly income")
                    && validateMoney("monthlyIncome", "Monthly income");

            if (!valid) {
                if (!valueOf("primaryCardNumber").replaceAll("\\s+", "").matches("\\d{16}")) {
                    showError("Primary card number must contain 16 digits.");
                } else if (valueOf("relationshipToPrimary").startsWith("Select")) {
                    showError("Please choose the relationship to the primary cardholder.");
                } else if (valueOf("cardType").startsWith("Select")) {
                    showError("Please choose the linked card type.");
                }
                return;
            }

            ApplicationRecord record = new ApplicationRecord(
                    ApplyDAO.generateApplicationId("SC"),
                    "SUBCARD",
                    username,
                    "PENDING",
                    ApplyDAO.nowForDisplay(),
                    valueOf("subcardName"),
                    valueOf("email"),
                    valueOf("phoneNumber"),
                    valueOf("identityNumber"),
                    valueOf("addressLine"),
                    valueOf("city"),
                    valueOf("state"),
                    valueOf("postCode"),
                    "",
                    "",
                    valueOf("purpose"),
                    valueOf("dateOfBirth"),
                    "",
                    valueOf("occupation"),
                    valueOf("monthlyIncome"),
                    "false",
                    valueOf("cardType"),
                    "",
                    valueOf("primaryCardholderName"),
                    valueOf("primaryCardNumber"),
                    valueOf("relationshipToPrimary"),
                    valueOf("subcardName")
            );

            if (ApplyDAO.saveApplication(record)) {
                showSuccess("Subcard application submitted successfully.");
                clearForm();
                prefillFromCustomer();
                showSelection();
            } else {
                showError("Unable to save the application right now.");
            }
        }
    }

    private class SelectionCard extends JPanel implements Themeable {
        private final JLabel cardTitle;
        private final JLabel cardDescription;
        private final JButton button;

        public SelectionCard(String titleText, String descriptionText) {
            setLayout(new BorderLayout(0, 20));
            setBorder(new EmptyBorder(30, 30, 30, 30));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
            textPanel.setOpaque(false);

            cardTitle = new JLabel(titleText);
            cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
            cardDescription = new JLabel("<html><body style='width:260px'>" + descriptionText + "</body></html>");
            cardDescription.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            textPanel.add(cardTitle);
            textPanel.add(cardDescription);

            button = new JButton("Open Form");
            button.setPreferredSize(new Dimension(140, 44));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            add(textPanel, BorderLayout.CENTER);
            add(button, BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gradientPaint = new GradientPaint(0, 0, currentCardBG, getWidth(), getHeight(), isDarkModeActive() ? new Color(38, 38, 38) : new Color(255, 255, 255));
            g2.setPaint(gradientPaint);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.setColor(currentBorder);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            g2.setColor(new Color(255, 204, 0, isDarkModeActive() ? 35 : 55));
            g2.fillOval(getWidth() - 130, -50, 180, 180);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public void updateTheme(boolean isDarkMode) {
            cardTitle.setForeground(currentTextMain);
            cardDescription.setForeground(currentTextSub);
            button.setBackground(isDarkMode ? ACCENT_SOFT : ACCENT);
            button.setForeground(Color.BLACK);
            repaint();
        }
    }

    private class RoundedFormCard extends JPanel {
        public RoundedFormCard(){
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentCardBG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.setColor(currentBorder);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            g2.dispose();
        }
    }

    private class RoundedInputPanel extends JPanel {
        public RoundedInputPanel(){
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = isDarkModeActive()
            ? new Color(40, 40, 40)
            : new Color(245,247,250);
            Color border = isDarkModeActive()
            ? new Color(90, 90, 90)
            : new Color(210, 216, 224);

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
        }
    }
}