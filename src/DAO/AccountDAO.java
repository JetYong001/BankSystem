package DAO;

import org.mindrot.jbcrypt.BCrypt;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountDAO {
    private static final String FILE_PATH = "data/accounts.json";
    private static final String TRANS_PATH = "data/transactions.json";

    public static synchronized String readJsonFile() {
        try {
            File file = new File(FILE_PATH);
            Path path = Paths.get(FILE_PATH);
            if (!file.exists() || file.length() == 0) {
                if (file.getParentFile() != null) file.getParentFile().mkdirs();
                Files.writeString(path, "{\"accounts\":[]}");
                return "{\"accounts\":[]}";
            }
            return Files.readString(path).trim();
        } catch (Exception e) {
            return "{\"accounts\":[]}";
        }
    }

    public static synchronized void writeJsonFile(String content) {
        try {
            Files.writeString(Paths.get(FILE_PATH), content);
        } catch (Exception ignored) {}
    }

    public static String openCustomerAccount(String name, String ic, String pin, String cardType, String accountType, String address, String phone, String gmail) {
        if (!name.matches("^[a-zA-Z\\s]+$")) return "ERR_NAME";
        if (!ic.matches("^\\d{12}$")) return "ERR_IC";
        if (!pin.matches("^\\d{6}$")) return "ERR_PIN";
        if (!phone.matches("^[0-9+ -]{8,15}$")) return "ERR_PHONE";
        if (!gmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) return "ERR_EMAIL";

        try {
            String usersContent = UserDAO.readJsonFile();
            if (usersContent.contains("\"ic_number\": \"" + ic + "\"")) return "WRONG IC NUMBER !!!";

            String id = "CUST-" + java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            String accNum = "111" + String.format("%09d", (long)(Math.random() * 1000000000L));
            String prefix = cardType.equalsIgnoreCase("CREDIT") ? "5" : "4";
            String cardNum = prefix + String.format("%015d", (long)(Math.random() * 1000000000000000L));
            String hashedPin = BCrypt.hashpw(pin, BCrypt.gensalt());

            String cardDetails = getString(cardType, cardNum, hashedPin);

            String userJson = String.format(
                    "{\n  \"customer_id\": \"%s\",\n  \"full_name\": \"%s\",\n  \"ic_number\": \"%s\",\n  \"username\": \"\",\n  \"password_hash\": \"\",\n  \"address\": \"%s\",\n  \"phone\": \"%s\",\n  \"gmail\": \"%s\",\n  \"created_at\": \"%s\"\n}",
                    id, name, ic, address, phone, gmail, LocalDateTime.now()
            );

            UserDAO.writeJsonFile(usersContent.replaceFirst("\"customer\"\\s*:\\s*\\[", "\"customer\": [\n    " + Matcher.quoteReplacement(userJson) + ","));

            String accJson = String.format(
                    "{\n  \"customer_id\": \"%s\",\n  \"account_number\": \"%s\",\n  \"account_type\": \"%s\",\n  \"balance\": 0.00,\n  \"status\": \"ACTIVE\",\n  \"security_question\": \"\",\n  \"security_answer\": \"\",\n  \"cards\": [\n    %s\n  ]\n}",
                    id, accNum, accountType.toUpperCase(), cardDetails
            );

            String accContent = readJsonFile();
            writeJsonFile(accContent.replaceFirst("\"accounts\"\\s*:\\s*\\[", "\"accounts\": [\n    " + Matcher.quoteReplacement(accJson) + ","));

            return id + "|" + cardNum + "|" + accNum;
        } catch (Exception e) {
            return "ERR_SYS";
        }
    }

    public static boolean updateAccountStatus(String accNum, String newStatus) {
        try {
            String content = readJsonFile();
            String regex = "(\"account_number\"\\s*:\\s*\"" + Pattern.quote(accNum) + "\".*?\"status\"\\s*:\\s*\")([^\"]+)(\")";
            Pattern p = Pattern.compile(regex, Pattern.DOTALL);
            Matcher m = p.matcher(content);

            if (m.find()) {
                String updatedContent = m.replaceFirst("$1" + newStatus + "$3");
                writeJsonFile(updatedContent);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean isAccountFrozen(String accNum) {
        String content = readJsonFile();
        Pattern p = Pattern.compile("\"account_number\"\\s*:\\s*\"" + Pattern.quote(accNum) + "\".*?\"status\"\\s*:\\s*\"(FROZEN)\"", Pattern.DOTALL);
        return p.matcher(content).find();
    }

    public static String generateCVV() {
        return String.format("%03d", (int) (Math.random() * 1000));
    }

    public static String generateExpiryDate() {
        LocalDateTime future = LocalDateTime.now().plusYears(5);
        return String.format("%02d/%02d", future.getMonthValue(), future.getYear() % 100);
    }

    private static String getString(String cardType, String cardNum, String hashedPin) {
        String cvv = generateCVV();
        String expiry = generateExpiryDate();

        if (cardType.equalsIgnoreCase("CREDIT")) {
            return String.format(
                    "{\n      \"card_number\": \"%s\",\n      \"card_type\": \"CREDIT\",\n      \"cvv\": \"%s\",\n      \"expiry_date\": \"%s\",\n      \"pin_hash\": \"%s\",\n      \"credit_limit\": 5000.00,\n      \"current_debt\": 0.00\n    }",
                    cardNum, cvv, expiry, hashedPin);
        } else {
            return String.format(
                    "{\n      \"card_number\": \"%s\",\n      \"card_type\": \"DEBIT\",\n      \"cvv\": \"%s\",\n      \"expiry_date\": \"%s\",\n      \"pin_hash\": \"%s\"\n    }",
                    cardNum, cvv, expiry, hashedPin);
        }
    }

    public static String getCustomerIdByCard(String card) {
        String content = readJsonFile();
        Pattern p = Pattern.compile("(?s)\\{[^{]*?\"customer_id\"\\s*:\\s*\"([^\"]+)\"[^}]*?\"card_number\"\\s*:\\s*\"" + Pattern.quote(card) + "\"[^}]*?}");
        Matcher m = p.matcher(content);
        return m.find() ? m.group(1) : "";
    }

    public static int checkCardForRegistration(String card, String pin) {
        String customerId = getCustomerIdByCard(card);
        if (customerId.isEmpty()) return 0;

        String content = readJsonFile();
        Pattern p = Pattern.compile("(?s)\"card_number\"\\s*:\\s*\"" + Pattern.quote(card) + "\".*?\"pin_hash\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(content);

        if (m.find()) {
            String storedPinHash = m.group(1);
            String usersContent = UserDAO.readJsonFile();
            Pattern up = Pattern.compile("(?s)\"customer_id\"\\s*:\\s*\"" + Pattern.quote(customerId) + "\".*?\"username\"\\s*:\\s*\"([^\"]*)\"");
            Matcher um = up.matcher(usersContent);

            if (um.find()) {
                String username = um.group(1);
                if (username != null && !username.trim().isEmpty()) return 3;
            }
            if (BCrypt.checkpw(pin, storedPinHash)) return 2;
        }
        return 0;
    }



    public static synchronized String processJsonTransaction(String accNum, String amountStr, String type) {
        try {
            double amount = Double.parseDouble(amountStr);
            String content = readJsonFile();


            String targetAcc = "\"account_number\": \"" + accNum + "\"";
            System.out.println(targetAcc);

            int accIndex = content.indexOf(targetAcc);
            if (accIndex == -1) {
                targetAcc = "\"account_number\":\"" + accNum + "\"";
                accIndex = content.indexOf(targetAcc);
            }

            if (accIndex != -1) {
                String remainingContent = content.substring(accIndex);
                String balanceRegex = "(\"balance\"\\s*:\\s*)([\\d.-]+)";
                Matcher m = Pattern.compile(balanceRegex).matcher(remainingContent);

                if (m.find()) {
                    double currentBalance = Double.parseDouble(m.group(2));
                    double newBalance;

                    if (type.equalsIgnoreCase("DEPOSIT")) {
                        newBalance = currentBalance + amount;
                    } else {
                        if (currentBalance < amount) return "INSUFFICIENT_FUNDS";
                        newBalance = currentBalance - amount;
                    }

                    String formattedBalance = String.format(java.util.Locale.US, "%.2f", newBalance);
                    String oldBalanceSegment = m.group(0);
                    String newBalanceSegment = m.group(1) + formattedBalance;

                    String beforeAcc = content.substring(0, accIndex);
                    String afterAccWithNewBalance = remainingContent.replaceFirst(Pattern.quote(oldBalanceSegment), Matcher.quoteReplacement(newBalanceSegment));

                    writeJsonFile(beforeAcc + afterAccWithNewBalance);
                    logCashTransaction(accNum, type, amount);
                    String refId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    String ts = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    String note = "Transaction " + type;
                    TransferDAO.addRecord(
                            accNum,
                            type,
                            amount,
                            note,
                            refId,
                            ts,
                            "ATM / SELF"
                    );
                    return "SUCCESS";
                }
            }
            return "ACCOUNT_NOT_FOUND";
        } catch (Exception e) {
            return "SYSTEM_ERROR";
        }
    }

    private static synchronized void logCashTransaction(String accNum, String type, double amount) {
        try {
            java.io.File file = new java.io.File(TRANS_PATH);
            Path path = Paths.get(TRANS_PATH);
            String content = (file.exists() && file.length() > 0) ? Files.readString(path) : "{\"transactions\":[]}";

            String logEntry = String.format(
                    "{\"acc_num\":\"%s\",\"type\":\"%s\",\"amount\":%.2f,\"date\":\"%s\"}",
                    accNum, type.toUpperCase(), amount, LocalDateTime.now()
            );

            String updated;
            if (content.contains("\"acc_num\"")) {
                updated = content.replaceFirst("\\[", "[\n    " + Matcher.quoteReplacement(logEntry) + ",");
            } else {
                updated = content.replaceFirst("\\[", "[\n    " + Matcher.quoteReplacement(logEntry));
            }

            Files.writeString(path, updated);
        } catch (Exception ignored) {}
    }

    public static boolean verifyAccountPin(String accNum, String inputPin) {
        try {
            String content = readJsonFile();

            String blockRegex = "(?s)\\{[^{}]*?\"account_number\"\\s*:\\s*\"" + Pattern.quote(accNum) + "\".*?\"cards\"\\s*:\\s*\\[.*?\\]\\s*\\}";
            Matcher blockMatcher = Pattern.compile(blockRegex).matcher(content);

            if (blockMatcher.find()) {
                String accountBlock = blockMatcher.group();


                Pattern pinPattern = Pattern.compile("\"pin_hash\"\\s*:\\s*\"([^\"]+)\"");
                Matcher pinMatcher = pinPattern.matcher(accountBlock);

                if (pinMatcher.find()) {
                    String storedPinHash = pinMatcher.group(1);

                    return BCrypt.checkpw(inputPin, storedPinHash);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isSavingsAccount(String accNum) {
        try {
            String content = readJsonFile();
            String regex = "\"account_number\"\\s*:\\s*\"" + Pattern.quote(accNum) + "\".*?\"account_type\"\\s*:\\s*\"SAVINGS\"";
            Pattern p = Pattern.compile(regex, Pattern.DOTALL);
            return p.matcher(content).find();
        } catch (Exception e) {
            return false;
        }
    }

    public static void createApprovedSavingsAccount(String customerId, double initialBalance) {
        createAccountByType(customerId, "SAVINGS", initialBalance, 0.0, 0);
    }

    public static void createApprovedFixedDeposit(String customerId, double initialBalance, double interest, int months) {
        createAccountByType(customerId, "FIXED", initialBalance, interest, months);
    }

    private static void createAccountByType(String customerId, String type, double balance, double interest, int months) {
        try {
            String accContent = readJsonFile();
            String existingPinHash = "";

            Pattern p = Pattern.compile("\"customer_id\"\\s*:\\s*\"" + Pattern.quote(customerId) + "\".*?\"pin_hash\"\\s*:\\s*\"([^\"]+)\"", Pattern.DOTALL);
            Matcher m = p.matcher(accContent);
            if (m.find()) existingPinHash = m.group(1);

            if (existingPinHash.isEmpty()) {
                existingPinHash = BCrypt.hashpw("000000", BCrypt.gensalt());
            }

            String prefix = type.equals("FIXED") ? "888" : "168";
            String accNum = prefix + String.format("%09d", (long)(Math.random() * 1000000000L));


            String cardsJson = "";
            if (!type.equals("FIXED")) {
                String cardNum = "4" + String.format("%015d", (long)(Math.random() * 1000000000000000L));
                cardsJson = String.format(
                        "{\n      \"card_number\": \"%s\",\n      \"card_type\": \"DEBIT\",\n      \"cvv\": \"%s\",\n      \"expiry_date\": \"%s\",\n      \"pin_hash\": \"%s\"\n    }",
                        cardNum, generateCVV(), generateExpiryDate(), existingPinHash);
            }

            String tenureStr = "";
            if (type.equals("FIXED")) {
                tenureStr = String.format(
                        ",\n  \"tenure_months\": %d,\n  \"interest_amount\": %.2f,\n  \"expiry_date\": \"%s\"",
                        months, interest, java.time.LocalDateTime.now().plusMonths(months)
                );
            }

            String accJson = String.format(
                    "{\n  \"customer_id\": \"%s\",\n  \"account_number\": \"%s\",\n  \"account_type\": \"%s\",\n  \"balance\": %.2f,\n  \"status\": \"ACTIVE\",\n  \"security_question\": \"\",\n  \"security_answer\": \"\"%s,\n  \"cards\": [%s]\n}",
                    customerId, accNum, type, balance, tenureStr, cardsJson
            );

            String updatedContent = accContent.replaceFirst("\"accounts\"\\s*:\\s*\\[",
                    "\"accounts\": [\n    " + Matcher.quoteReplacement(accJson) + ",");

            writeJsonFile(updatedContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isAccountExists(String accNum) {
        try {
            String content = readJsonFile();
            String regex = "\"account_number\"\\s*:\\s*\"" + Pattern.quote(accNum) + "\"";
            Pattern p = Pattern.compile(regex);
            return p.matcher(content).find();
        } catch (Exception e) {
            return false;
        }
    }

    public static void createApprovedCreditCard(String customerId, double limit) {
        String pinHash = UserDAO.getExistingCardPin(customerId);
        String accNum = findCurrentAccountNumber(customerId);
        if (accNum != null) {
            bindNewCardToAccount(accNum, "CREDIT", pinHash, limit);
        }
    }

    public static void createApprovedSubcard(String customerId, String primaryCardNum) {
        String pinHash = UserDAO.getExistingCardPin(customerId);
        String accNum = findCurrentAccountNumber(customerId);
        if (accNum != null) {
            bindNewCardToAccount(accNum, "SUBCARD", pinHash, 0.0);
        }
    }

    public static void bindNewCardToAccount(String accNum, String cardType, String pinHash, double limit) {
        try {
            String content = Files.readString(Paths.get(FILE_PATH));
            String newCardNum = (cardType.equalsIgnoreCase("CREDIT") ? "5" : "4")
                    + String.format("%015d", (long)(Math.random() * 1000000000000000L));

            String cardJson = "{\n" +
                    "      \"card_number\": \"" + newCardNum + "\",\n" +
                    "      \"card_type\": \"" + cardType.toUpperCase() + "\",\n" +
                    "      \"cvv\": \"" + (int)(Math.random()*900+100) + "\",\n" +
                    "      \"expiry_date\": \"" + generateExpiryDate() + "\",\n" +
                    "      \"pin_hash\": \"" + pinHash + "\",\n" +
                    "      \"limit\": " + String.format("%.2f", limit) + "\n" +
                    "    }";

            String regex = "(\"account_number\"\\s*:\\s*\"" + Pattern.quote(accNum) + "\"[^}]*?\"cards\"\\s*:\\s*\\[)";
            Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(content);

            if (m.find()) {
                String match = m.group(1);
                int insertPos = m.end();
                String afterMatch = content.substring(insertPos).trim();
                String replacement;

                if (afterMatch.startsWith("]")) {
                    replacement = match + "\n    " + cardJson + "\n  ";
                } else {
                    replacement = match + "\n    " + cardJson + ",\n  ";
                }

                String newContent = content.replaceFirst(Pattern.quote(match), Matcher.quoteReplacement(replacement));
                writeJsonFile(newContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static boolean isCurrentAccount(String accNum) {
        try {
            String content = readJsonFile();

            String[] objects = content.split("\\{[\\s]*\"customer_id\"");

            for (String block : objects) {
                if (block.contains("\"account_number\"") && block.contains("\"" + accNum + "\"")) {
                    if (block.contains("\"account_type\"") && block.contains("\"CURRENT\"")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static String findCurrentAccountNumber(String customerId) {
        try {
            String content = readJsonFile();
            String[] blocks = content.split("\\{[\\s]*\"customer_id\"");

            for (String block : blocks) {
                if (block.contains("\"" + customerId + "\"")) {
                    if (block.contains("\"account_type\"") && (block.contains("\"CURRENT\"") || block.contains("CURRENT"))) {
                        Pattern accNumP = Pattern.compile("\"account_number\"\\s*:\\s*\"(111\\d+)\"");
                        Matcher accNumM = accNumP.matcher(block);
                        if (accNumM.find()) {
                            return accNumM.group(1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static synchronized void refundToAccount(String accNum, double amount) {
        if (accNum == null) return;
        try {
            String content = readJsonFile();
            String regex = "(?s)\\{[^{}]*?\"account_number\"\\s*:\\s*\"" + Pattern.quote(accNum) + "\".*?}";
            Matcher m = Pattern.compile(regex).matcher(content);

            if (m.find()) {
                String block = m.group();
                Pattern balancePattern = Pattern.compile("(\"balance\"\\s*:\\s*)([\\d.-]+)");
                Matcher balanceMatcher = balancePattern.matcher(block);

                if (balanceMatcher.find()) {
                    double currentBalance = Double.parseDouble(balanceMatcher.group(2));
                    double newBalance = currentBalance + amount;


                    String updatedBalance = balanceMatcher.group(1) + String.format(java.util.Locale.US, "%.2f", newBalance);

                    String updatedBlock = block.replaceFirst(Pattern.quote(balanceMatcher.group(0)), Matcher.quoteReplacement(updatedBalance));
                    String updatedContent = content.replaceFirst(Pattern.quote(block), Matcher.quoteReplacement(updatedBlock));

                    writeJsonFile(updatedContent);
                    logCashTransaction(accNum, "REFUND", amount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}