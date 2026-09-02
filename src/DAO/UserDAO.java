package DAO;

import models.Card.Card;
import models.Card.CreditCard;
import models.Card.DebitCard;
import models.User.Customer;
import models.User.Staff;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserDAO {
    private static final String FILE_PATH = "data/users.json";
    private static final String AVATAR_FILE = "data/user_avatars.json";
    private static final String SETTINGS_FILE = "data/setting.json";

    public static synchronized String readJsonFile() {
        try {
            File file = new File(FILE_PATH);
            Path path = Paths.get(FILE_PATH);
            if (!file.exists() || file.length() == 0) {
                Files.writeString(path, "{\"staff\":[],\"customer\":[]}");
                return "{\"staff\":[],\"customer\":[]}";
            }
            String content = Files.readString(path).trim();
            if (content.isEmpty() || (!content.contains("\"staff\"") && !content.contains("\"customer\""))) {
                Files.writeString(path, "{\"staff\":[],\"customer\":[]}");
                return "{\"staff\":[],\"customer\":[]}";
            }
            return content;
        } catch (Exception e) {
            return "{\"staff\":[],\"customer\":[]}";
        }
    }

    public static synchronized void writeJsonFile(String content) {
        try {
            Files.writeString(Paths.get(FILE_PATH), content);
        } catch (Exception ignored) {}
    }

    public static boolean login(String inputUsername, String inputPassword, boolean isStaff) {
        if (isStaff) {
            for (Staff s : getAllStaff()) {
                if (s.getUsername().equals(inputUsername)) {
                    return BCrypt.checkpw(inputPassword, s.getPasswordHash());
                }
            }
        } else {
            for (Customer c : getAllCustomer()) {
                if (c.getUsername().equals(inputUsername)) {
                    return BCrypt.checkpw(inputPassword, c.getPasswordHash());
                }
            }
        }
        return false;
    }

    public static boolean completeRegistration(String card, String newUsername, String newPassword, String q, String a) {
        String customerId = AccountDAO.getCustomerIdByCard(card);
        if (customerId.isEmpty()) return false;

        try {
            String userContent = UserDAO.readJsonFile();
            Pattern up = Pattern.compile("(?s)\\{[^{}]*?\"customer_id\":\\s*\"" + Pattern.quote(customerId) + "\"[^{}]*?}");
            Matcher um = up.matcher(userContent);

            if (um.find()) {
                String oldBlock = um.group();
                String hashedPass = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                String updatedBlock = oldBlock
                        .replaceFirst("\"username\":\\s*\"[^\"]*\"", Matcher.quoteReplacement("\"username\": \"" + newUsername + "\""))
                        .replaceFirst("\"password_hash\":\\s*\"[^\"]*\"", Matcher.quoteReplacement("\"password_hash\": \"" + hashedPass + "\""));

                UserDAO.writeJsonFile(userContent.replace(oldBlock, updatedBlock));
            } else {
                return false;
            }

            String accContent = AccountDAO.readJsonFile();
            String regex = "(\"customer_id\":\\s*\"" + Pattern.quote(customerId) + "\"(?:(?!\"customer_id\").)*?\"security_question\":\\s*\")[^\"]*(\".*?\"security_answer\":\\s*\")[^\"]*(\")";
            Matcher am = Pattern.compile(regex, Pattern.DOTALL).matcher(accContent);

            if (am.find()) {
                String updatedAccContent = am.replaceFirst(Matcher.quoteReplacement(am.group(1) + q + am.group(2) + a + am.group(3)));
                AccountDAO.writeJsonFile(updatedAccContent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean saveStaff(Staff s) {
        try {
            String content = readJsonFile();
            String staffJson = String.format(
                    "{\n  \"staff_id\": \"%s\",\n  \"full_name\": \"%s\",\n  \"ic_number\": \"%s\",\n  \"username\": \"%s\",\n  \"password_hash\": \"%s\",\n  \"position\": \"%s\",\n  \"created_at\": \"%s\"\n}",
                    s.getStaffID(), s.getFull_name(), s.getIcNumber(), s.getUsername(), s.getPasswordHash(), s.getPosition(), s.getCreatedTime().toString()
            );

            String updated;
            if (content.matches("(?s).*\"staff\"\\s*:\\s*\\[\\s*].*")) {
                updated = content.replaceFirst("\"staff\"\\s*:\\s*\\[\\s*]", "\"staff\": [\n    " + Matcher.quoteReplacement(staffJson) + "\n  ]");
            } else {
                updated = content.replaceFirst("\"staff\"\\s*:\\s*\\[", "\"staff\": [\n    " + Matcher.quoteReplacement(staffJson) + ",");
            }

            writeJsonFile(updated);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getStaffPosition(String identifier) {
        for (Staff s : getAllStaff()) {
            if (s.getStaffID().equalsIgnoreCase(identifier) || s.getUsername().equalsIgnoreCase(identifier)) {
                String pos = s.getPosition();
                return (pos == null || pos.isEmpty()) ? "Staff" : pos;
            }
        }
        return "Staff";
    }

    public static Staff getStaffByUsername(String username) {
        for (Staff s : getAllStaff()) {
            if (s.getUsername().equalsIgnoreCase(username)) {
                return s;
            }
        }
        return null;
    }

    public static Customer getCustomerByUsername(String username) {
        for (Customer c : getAllCustomer()) {
            if (c.getUsername().equalsIgnoreCase(username)) {
                return c;
            }
        }
        return null;
    }

    public static ArrayList<Staff> getAllStaff() {
        ArrayList<Staff> list = new ArrayList<>();
        String content = readJsonFile();
        Pattern p = Pattern.compile("(?s)\\{[^{}]*?\"staff_id\":[^{}]*?}");
        Matcher m = p.matcher(content);

        while (m.find()) {
            String b = m.group();
            String id = gv(b, "staff_id");
            String nm = gv(b, "full_name");
            String ic = gv(b, "ic_number");
            String un = gv(b, "username");
            String pw = gv(b, "password_hash");
            String ps = gv(b, "position");
            String ad = gv(b, "address");
            String ph = gv(b, "phone");
            String gm = gv(b, "gmail");

            try {
                list.add(new Staff(ic, nm, LocalDateTime.now(), ad, ph, gm, pw, un, id, ps));
            } catch (Exception e) {
                list.add(new Staff(ic, nm, LocalDateTime.now(), ad, ph, gm, pw, un, id, ps));
            }
        }
        list.sort(Comparator.comparing(Staff::getFull_name, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public static ArrayList<Customer> getAllCustomer() {
        ArrayList<Customer> list = new ArrayList<>();
        String usersContent = readJsonFile();
        String accountsContent = AccountDAO.readJsonFile();
        Pattern up = Pattern.compile("(?s)\\{[^{]*?\"customer_id\"\\s*:\\s*\"([^\"]+)\"[^}]*?}");
        Matcher um = up.matcher(usersContent);
        while (um.find()) {
            String ub = um.group();
            String id = gv(ub, "customer_id");
            if (id.isEmpty()) continue;
            String timeStr = gv(ub, "created_at");
            LocalDateTime dt;
            try { dt = LocalDateTime.parse(timeStr); } catch(Exception e) { dt = LocalDateTime.now(); }

            Customer c = new Customer(
                    UserDAO.gv(ub, "ic_number"),
                    UserDAO.gv(ub, "full_name"),
                    dt,
                    UserDAO.gv(ub, "address"),
                    UserDAO.gv(ub, "phone"),
                    UserDAO.gv(ub, "gmail"),
                    UserDAO.gv(ub, "password_hash"),
                    UserDAO.gv(ub, "username"),
                    id
            );

            Pattern ap = Pattern.compile("(?s)\\{[^{]*?\"customer_id\"\\s*:\\s*\"" + Pattern.quote(id) + "\"[^}]*?}");
            Matcher am = ap.matcher(accountsContent);
            while (am.find()) {
                String ab = am.group();
                double bal = 0.0;
                Matcher bm = Pattern.compile("\"balance\"\\s*:\\s*([\\d.]+)").matcher(ab);
                if (bm.find()) try { bal = Double.parseDouble(bm.group(1)); } catch(Exception ignored) {}
                String accNum = UserDAO.gv(ab, "account_number");
                String accType = UserDAO.gv(ab, "account_type");
                ArrayList<Card> cardList = new ArrayList<>();

                models.Account.Account acc;
                if ("SAVINGS".equalsIgnoreCase(accType)) {
                    acc = new models.Account.SavingssAccount(accNum, bal, cardList);
                } else {
                    acc = new models.Account.CurrentAccount(accNum, bal, cardList);
                }
                Pattern cp = Pattern.compile("(?s)\\{[^{]*?\"card_number\"[^{]*?}");
                Matcher cm = cp.matcher(ab);
                while (cm.find()) {
                    String cb = cm.group();
                    String cNum = UserDAO.gv(cb, "card_number");
                    String cType = UserDAO.gv(cb, "card_type");
                    String pHash = UserDAO.gv(cb, "pin_hash");
                    String cvv = UserDAO.gv(cb, "cvv");
                    String expiry = UserDAO.gv(cb, "expiry_date");
                    if ("CREDIT".equalsIgnoreCase(cType)) {
                        double limit = 0.0, debt = 0.0;
                        Matcher lm = Pattern.compile("\"credit_limit\"\\s*:\\s*([\\d.]+)").matcher(cb);
                        Matcher dm = Pattern.compile("\"current_debt\"\\s*:\\s*([\\d.]+)").matcher(cb);
                        if (lm.find()) try { limit = Double.parseDouble(lm.group(1)); } catch(Exception ignored) {}
                        if (dm.find()) try { debt = Double.parseDouble(dm.group(1)); } catch(Exception ignored) {}

                        cardList.add(new CreditCard(cNum, cvv, expiry, pHash, acc, limit, debt));
                    } else {
                        cardList.add(new DebitCard(cNum, cvv,expiry, pHash, acc));
                    }
                }
                c.addAccount(acc);
            }
            list.add(c);
        }
        list.sort(Comparator.comparing(Customer::getFull_name, String.CASE_INSENSITIVE_ORDER));
        return list;
    }


    public static boolean isUsernameTaken(String username, boolean isStaff) {
        if (username == null || username.isEmpty()) return false;
        if (isStaff) {
            for (Staff s : getAllStaff()) {
                if (username.equalsIgnoreCase(s.getUsername())) return true;
            }
        } else {
            for (Customer c : getAllCustomer()) {
                if (username.equalsIgnoreCase(c.getUsername())) return true;
            }
        }
        return false;
    }

    public static String gv(String b, String k) {
        Pattern p = Pattern.compile("\"" + k + "\":\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(b);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }

    public static String getUserIDByUsername(String username, boolean isStaff) {
        if (isStaff) {
            for (Staff s : getAllStaff()) {
                if (s.getUsername().equalsIgnoreCase(username)) {
                    return s.getStaffID();
                }
            }
        } else {
            for (Customer c : getAllCustomer()) {
                if (c.getUsername().equalsIgnoreCase(username)) {
                    return c.getCustomerID();
                }
            }
        }
        return "";
    }



    public static String getAvatar(String id) {
        try {
            File file = new File(AVATAR_FILE);
            if (!file.exists()) return "";
            String content = Files.readString(Paths.get(AVATAR_FILE)).trim();
            return gv(content, id);
        } catch (Exception e) {
            return "";
        }
    }

    public static void saveAvatar(String id, String path) {
        try {
            File file = new File(AVATAR_FILE);
            String content = "{}";
            Path path1 = Paths.get(AVATAR_FILE);
            if (file.exists()) {
                content = Files.readString(path1).trim();
            }

            String updated;
            if (content.contains("\"" + id + "\"")) {
                updated = content.replaceFirst("\"" + Pattern.quote(id) + "\"\\s*:\\s*\"[^\"]*\"", "\"" + id + "\": \"" + Matcher.quoteReplacement(path.replace("\\", "/")) + "\"");
            } else if (content.equals("{}") || content.isEmpty()) {
                updated = "{\n  \"" + id + "\": \"" + path.replace("\\", "/") + "\"\n}";
            } else {
                updated = content.replaceFirst("\\s*}", ",\n  \"" + id + "\": \"" + Matcher.quoteReplacement(path.replace("\\", "/")) + "\"\n}");
            }
            Files.writeString(path1, updated);
        } catch (Exception ignored) {}
    }

    public static boolean updateCustomerInfo(String id, String name, String addr, String phone, String gmail) {
        try {
            String content = readJsonFile();
            Pattern p = Pattern.compile("(?s)\\{[^{]*?\"customer_id\"\\s*:\\s*\"" + Pattern.quote(id) + "\"[^}]*?}");
            Matcher m = p.matcher(content);
            if (m.find()) {
                String oldJson = m.group();
                String newJson = oldJson
                        .replaceFirst("\"full_name\"\\s*:\\s*\"[^\"]*\"", "\"full_name\": \"" + name + "\"")
                        .replaceFirst("\"address\"\\s*:\\s*\"[^\"]*\"", "\"address\": \"" + addr + "\"")
                        .replaceFirst("\"phone\"\\s*:\\s*\"[^\"]*\"", "\"phone\": \"" + phone + "\"")
                        .replaceFirst("\"gmail\"\\s*:\\s*\"[^\"]*\"", "\"gmail\": \"" + gmail + "\"");
                writeJsonFile(content.replace(oldJson, newJson));
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static void saveSetting(String username, boolean isDark) {
        updateSettingField(username, "isDarkMode", isDark);
    }

    public static boolean getThemePreference(String username) {
        return getSettingField(username, "isDarkMode", false);
    }

    public static void saveEyeStatus(String username, boolean isVisible) {
        updateSettingField(username, "isEyeOpen", isVisible);
    }

    public static boolean getEyeStatus(String username) {
        return getSettingField(username, "isEyeOpen", true);
    }

    private static synchronized void updateSettingField(String username, String field, boolean value) {
        if (username == null || username.isEmpty()) return;
        try {
            Path path = Paths.get(SETTINGS_FILE);
            File file = new File(SETTINGS_FILE);
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

            String content = file.exists() ? Files.readString(path).trim() : "{}";
            if (content.isEmpty()) content = "{}";

            String updated;
            String quotedID = Pattern.quote(username);

            if (content.contains("\"" + username + "\"")) {
                Pattern userPattern = Pattern.compile("\"" + quotedID + "\"\\s*:\\s*\\{([^}]*)}");
                Matcher m = userPattern.matcher(content);
                if (m.find()) {
                    String body = m.group(1);
                    String newBody;
                    if (body.contains("\"" + field + "\"")) {
                        newBody = body.replaceFirst("\"" + Pattern.quote(field) + "\"\\s*:\\s*(true|false)", "\"" + field + "\": " + value);
                    } else {
                        String trimmedBody = body.trim();
                        newBody = trimmedBody + (trimmedBody.isEmpty() ? "" : ",") + "\n      \"" + field + "\": " + value;
                    }
                    updated = content.replace(m.group(0), "\"" + username + "\": {\n      " + newBody.trim() + "\n    }");
                } else {
                    updated = content;
                }
            } else if (content.equals("{}")) {
                updated = "{\n  \"" + username + "\": {\n    \"" + field + "\": " + value + "\n  }\n}";
            } else {
                updated = content.replaceFirst("\\s*}", ",\n  \"" + username + "\": {\n    \"" + field + "\": " + value + "\n  }\n}");
            }
            Files.writeString(path, updated);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean getSettingField(String username, String field, boolean defaultValue) {
        if (username == null || username.isEmpty()) return defaultValue;
        try {
            File file = new File(SETTINGS_FILE);
            if (!file.exists()) return defaultValue;
            String content = Files.readString(Paths.get(SETTINGS_FILE));
            Pattern p = Pattern.compile("\"" + Pattern.quote(username) + "\"\\s*:\\s*\\{[^}]*?\"" + Pattern.quote(field) + "\"\\s*:\\s*(true|false)");
            Matcher m = p.matcher(content);
            if (m.find()) return Boolean.parseBoolean(m.group(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static boolean verifyStaffIdentity(String username, String ic, boolean isStaffSystem) {
        if(isStaffSystem) {
            for (Staff s : getAllStaff()) {
                if (s.getStaffID().equals(username) &&
                        s.getIcNumber().equals(ic)) {
                    return true;
                }
            }
        }else{
            for (Customer c : getAllCustomer()) {
                if (c.getUsername().equals(username) &&
                        c.getIcNumber().equals(ic)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean resetStaffPassword(String staffID, String newPassword) {
        try {
            String content = readJsonFile();

            String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            String regex = "(\"staff_id\"\\s*:\\s*\"" + Pattern.quote(staffID) + "\".*?\"password_hash\"\\s*:\\s*\")([^\"]*)(\")";

            Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(content);

            if (m.find()) {
                String updatedContent = m.replaceFirst(
                        Matcher.quoteReplacement(m.group(1) + hashed + m.group(3))
                );

                writeJsonFile(updatedContent);
                return true;
            } else {
                System.out.println("Staff NOT FOUND for reset!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getSecurityQuestionByUsername(String username) {
        Customer c = getCustomerByUsername(username);

        if (c == null || username == null || username.trim().isEmpty()) {
            return "";
        }

        String customerID = c.getCustomerID();
        String accContent = AccountDAO.readJsonFile();

        // Directly find question using customer_id
        String regex = "\"customer_id\"\\s*:\\s*\"" + Pattern.quote(customerID) + "\".*?\"security_question\"\\s*:\\s*\"([^\"]*)\"";
        Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(accContent);

        if (m.find()) {
            return m.group(1); // THIS captures the question
        }

        return "";
    }

    public static boolean verifySecurityAnswer(String username, String answer) {
        Customer c = getCustomerByUsername(username);
        if (c == null) return false;

        String customerID = c.getCustomerID();
        String accContent = AccountDAO.readJsonFile();

        String regex = "\"customer_id\"\\s*:\\s*\"" + Pattern.quote(customerID) + "\".*?\"security_answer\"\\s*:\\s*\"([^\"]*)\"";
        Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(accContent);

        if (m.find()) {
            String storedAnswer = m.group(1);
            return storedAnswer != null && storedAnswer.equals(answer);
        }

        return false;
    }

    public static boolean resetCustomerPassword(String username, String newPassword) {
        try {
            String content = readJsonFile();

            String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            String regex = "(\"username\"\\s*:\\s*\"" + Pattern.quote(username) + "\".*?\"password_hash\"\\s*:\\s*\")([^\"]*)(\")";

            Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(content);

            if (m.find()) {
                String updatedContent = m.replaceFirst(
                        Matcher.quoteReplacement(m.group(1) + hashed + m.group(3))
                );

                writeJsonFile(updatedContent);
                return true;
            } else {
                System.out.println("Customer NOT FOUND for reset!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean isStaff(String staffID) {
        for (Staff s : getAllStaff()) {
            if (s.getStaffID().equals(staffID)) return true;
        }
        return false;
    }

    public static boolean isCustomer(String username) {
        return getCustomerByUsername(username) != null;
    }

    public static boolean updateFullStaffInfo(String staffID, String name, String ic, String username, String phone, String gmail, String role) {
        try {
            String content = readJsonFile();
            Pattern p = Pattern.compile("(?s)\\{[^{}]*?\"staff_id\":\\s*\"" + Pattern.quote(staffID) + "\"[^{}]*?}");
            Matcher m = p.matcher(content);

            if (m.find()) {
                String oldBlock = m.group();
                String updatedBlock = oldBlock
                        .replaceFirst("\"full_name\"\\s*:\\s*\"[^\"]*\"", "\"full_name\": \"" + name + "\"")
                        .replaceFirst("\"ic_number\"\\s*:\\s*\"[^\"]*\"", "\"ic_number\": \"" + ic + "\"")
                        .replaceFirst("\"username\"\\s*:\\s*\"[^\"]*\"", "\"username\": \"" + username + "\"")
                        .replaceFirst("\"phone\"\\s*:\\s*\"[^\"]*\"", "\"phone\": \"" + phone + "\"")
                        .replaceFirst("\"gmail\"\\s*:\\s*\"[^\"]*\"", "\"gmail\": \"" + gmail + "\"")
                        .replaceFirst("\"position\"\\s*:\\s*\"[^\"]*\"", "\"position\": \"" + role + "\"");

                writeJsonFile(content.replace(oldBlock, updatedBlock));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getExistingCardPin(String customerId) {
        try {
            String content = AccountDAO.readJsonFile();

            String regex = "\"customer_id\"\\s*:\\s*\"" + Pattern.quote(customerId) + "\".*?\"pin_hash\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.DOTALL).matcher(content);

            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "123456";
    }

}