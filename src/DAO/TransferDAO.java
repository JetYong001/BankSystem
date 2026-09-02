package DAO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.format.DateTimeFormatter;

public class TransferDAO {
    private static final String FILE_PATH = "data/accounts.json";
    private static final String PATH = "data/transactions.json";

    public static String readJsonFile() {
        try { return Files.readString(Paths.get(FILE_PATH)); } catch (Exception e) { return "{}"; }
    }

    public static void writeJsonFile(String content) {
        try { Files.writeString(Paths.get(FILE_PATH), content); } catch (Exception e) { e.printStackTrace(); }
    }

    public static double getAccountBalance(String accNum) {
        String content = readJsonFile();
        if (content.isEmpty()) return 0.0;
        String regex = "\\{[^{]*?\"account_number\"\\s*:\\s*\"" + Pattern.quote(accNum) + "\".*?\"balance\"\\s*:\\s*([\\d.]+)[^}]*?}";
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = p.matcher(content);
        if (m.find()) return Double.parseDouble(m.group(1));
        return 0.0;
    }

    public static boolean updateBalance(String accNum, double amountDelta) {
        String content = readJsonFile();
        String regex = "(?s)(\"account_number\"\\s*:\\s*\"" + Pattern.quote(accNum) + "\".*?\"balance\"\\s*:\\s*)([\\d.]+)";
        Matcher m = Pattern.compile(regex).matcher(content);
        if (m.find()) {
            double newBal = Double.parseDouble(m.group(2)) + amountDelta;
            writeJsonFile(content.replaceFirst(regex, Matcher.quoteReplacement(m.group(1) + String.format("%.2f", newBal))));
            return true;
        }
        return false;
    }

    public static boolean updateCreditLimit(String cardNum, double amountDelta) {
        String content = readJsonFile();
        String regex = "(?s)(\"card_number\"\\s*:\\s*\"" + Pattern.quote(cardNum) + "\".*?\"current_limit\"\\s*:\\s*)([\\d.]+)";
        Matcher m = Pattern.compile(regex).matcher(content);
        if (m.find()) {
            double newLimit = Double.parseDouble(m.group(2)) + amountDelta;
            writeJsonFile(content.replaceFirst(regex, Matcher.quoteReplacement(m.group(1) + String.format("%.2f", newLimit))));
            return true;
        }
        return false;
    }

    public static void addRecord(String accNum, String type, double amount, String details, String refId, String timestamp, String relatedAcc) {
        saveTransaction(accNum, type, amount, details, refId, timestamp, relatedAcc);
    }

    private static synchronized void saveTransaction(String accNum, String type, double amount, String details, String refId, String timestamp, String relatedAcc) {
        if ("DEPOSIT".equalsIgnoreCase(type) || "REFUND".equalsIgnoreCase(type)) {
            amount = Math.abs(amount);
        }
        else if ("WITHDRAW".equalsIgnoreCase(type) || "TRANSFER".equalsIgnoreCase(type)) {
            amount = -Math.abs(amount);
        }
        try {
            Path path = Paths.get(PATH);
            String content = Files.exists(path) ? Files.readString(path) : "[]";
            if (content.trim().isEmpty()) content = "[]";

            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String entry = String.format(
                    "{\"acc_num\":\"%s\",\"type\":\"%s\",\"amount\":%.2f,\"details\":\"%s\",\"date\":\"%s\",\"timestamp\":\"%s\",\"reference_id\":\"%s\",\"related_acc\":\"%s\"}",
                    accNum, type, amount, details, date, timestamp, refId, relatedAcc
            );

            String updated;
            if (content.contains("{")) {
                updated = content.replaceFirst("\\[", "[" + Matcher.quoteReplacement(entry) + ",");
            } else {
                updated = "[" + entry + "]";
            }
            Files.writeString(path, updated);
        } catch (Exception e) { e.printStackTrace(); }
    }
}