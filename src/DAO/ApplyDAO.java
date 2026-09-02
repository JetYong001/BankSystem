package DAO;

import models.Application.ApplicationRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplyDAO {
    private static final Path JSON_PATH = Paths.get("data/application.json");
    private static final DateTimeFormatter ID_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized boolean saveApplication(ApplicationRecord record) {
        try {
            ensureFileExists();
            List<ApplicationRecord> records = getAllApplications();
            records.add(record);
            writeAll(records);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized List<ApplicationRecord> getAllApplications() {
        try {
            ensureFileExists();
            String content = Files.readString(JSON_PATH, StandardCharsets.UTF_8).trim();
            if (content.isEmpty() || "[]".equals(content)) {
                return new ArrayList<>();
            }

            List<String> objectJsonList = splitObjects(content);
            List<ApplicationRecord> records = new ArrayList<>();
            for (String objectJson : objectJsonList) {
                Map<String, String> values = parseObject(objectJson);
                records.add(new ApplicationRecord(
                        values.getOrDefault("applicationId", ""),
                        values.getOrDefault("applicationType", ""),
                        values.getOrDefault("applicantUsername", ""),
                        values.getOrDefault("status", ""),
                        values.getOrDefault("submittedAt", ""),
                        values.getOrDefault("fullName", ""),
                        values.getOrDefault("email", ""),
                        values.getOrDefault("phoneNumber", ""),
                        values.getOrDefault("identityNumber", ""),
                        values.getOrDefault("addressLine", ""),
                        values.getOrDefault("city", ""),
                        values.getOrDefault("state", ""),
                        values.getOrDefault("postCode", ""),
                        values.getOrDefault("initialDeposit", ""),
                        values.getOrDefault("tenureMonths", ""), 
                        values.getOrDefault("applicationPurpose", ""),
                        values.getOrDefault("dateOfBirth", ""),
                        values.getOrDefault("employerName", ""),
                        values.getOrDefault("occupation", ""),
                        values.getOrDefault("monthlyIncome", ""),
                        values.getOrDefault("existingCustomer", ""),
                        values.getOrDefault("cardType", ""),
                        values.getOrDefault("requestedLimit", ""),
                        values.getOrDefault("primaryCardholderName", ""),
                        values.getOrDefault("primaryCardNumber", ""),
                        values.getOrDefault("relationshipToPrimary", ""),
                        values.getOrDefault("subcardName", "")
                ));
            }
            return records;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<ApplicationRecord> getApplicationsByUsername(String username) {
        List<ApplicationRecord> all = getAllApplications();
        List<ApplicationRecord> result = new ArrayList<>();
        for (ApplicationRecord record : all) {
            if (record.getApplicantUsername() != null &&
                    record.getApplicantUsername().equalsIgnoreCase(username)) {
                result.add(record);
            }
        }
        result.sort((a, b) -> b.getSubmittedAt().compareTo(a.getSubmittedAt()));
        return result;
    }

    public static synchronized boolean updateApplicationStatus(String applicationId, String newStatus) {
        try {
            ensureFileExists();
            List<ApplicationRecord> records = getAllApplications();
            boolean updated = false;
            for (int i = 0; i < records.size(); i++) {
                ApplicationRecord record = records.get(i);
                if (record.getApplicationId().equalsIgnoreCase(applicationId)) {
                    records.set(i, copyWithStatus(record, newStatus));
                    updated = true;
                    break;
                }
            }
            if (!updated) return false;
            writeAll(records);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static ApplicationRecord copyWithStatus(ApplicationRecord record, String newStatus) {
        return new ApplicationRecord(
                record.getApplicationId(),
                record.getApplicationType(),
                record.getApplicantUsername(),
                newStatus,
                record.getSubmittedAt(),
                record.getFullName(),
                record.getEmail(),
                record.getPhoneNumber(),
                record.getIdentityNumber(),
                record.getAddressLine(),
                record.getCity(),
                record.getState(),
                record.getPostCode(),
                record.getInitialDeposit(),
                record.getTenureMonths(),
                record.getApplicationPurpose(),
                record.getDateOfBirth(),
                record.getEmployerName(),
                record.getOccupation(),
                record.getMonthlyIncome(),
                record.getExistingCustomer(),
                record.getCardType(),
                record.getRequestedLimit(),
                record.getPrimaryCardholderName(),
                record.getPrimaryCardNumber(),
                record.getRelationshipToPrimary(),
                record.getSubcardName()
        );
    }

    private static void writeAll(List<ApplicationRecord> records) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        for (int i = 0; i < records.size(); i++) {
            ApplicationRecord record = records.get(i);
            json.append("  {\n");
            appendField(json, "applicationId", record.getApplicationId(), true);
            appendField(json, "applicationType", record.getApplicationType(), true);
            appendField(json, "applicantUsername", record.getApplicantUsername(), true);
            appendField(json, "status", record.getStatus(), true);
            appendField(json, "submittedAt", record.getSubmittedAt(), true);
            appendField(json, "fullName", record.getFullName(), true);
            appendField(json, "email", record.getEmail(), true);
            appendField(json, "phoneNumber", record.getPhoneNumber(), true);
            appendField(json, "identityNumber", record.getIdentityNumber(), true);
            appendField(json, "addressLine", record.getAddressLine(), true);
            appendField(json, "city", record.getCity(), true);
            appendField(json, "state", record.getState(), true);
            appendField(json, "postCode", record.getPostCode(), true);
            appendField(json, "initialDeposit", record.getInitialDeposit(), true);
            appendField(json, "tenureMonths", record.getTenureMonths(), true); // 3. 写入JSON文件
            appendField(json, "applicationPurpose", record.getApplicationPurpose(), true);
            appendField(json, "dateOfBirth", record.getDateOfBirth(), true);
            appendField(json, "employerName", record.getEmployerName(), true);
            appendField(json, "occupation", record.getOccupation(), true);
            appendField(json, "monthlyIncome", record.getMonthlyIncome(), true);
            appendField(json, "existingCustomer", record.getExistingCustomer(), true);
            appendField(json, "cardType", record.getCardType(), true);
            appendField(json, "requestedLimit", record.getRequestedLimit(), true);
            appendField(json, "primaryCardholderName", record.getPrimaryCardholderName(), true);
            appendField(json, "primaryCardNumber", record.getPrimaryCardNumber(), true);
            appendField(json, "relationshipToPrimary", record.getRelationshipToPrimary(), true);
            appendField(json, "subcardName", record.getSubcardName(), false);
            json.append("  }");
            if (i < records.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("]\n");
        Files.writeString(JSON_PATH, json.toString(), StandardCharsets.UTF_8);
    }


    private static void ensureFileExists() throws IOException {
        Path parent = JSON_PATH.getParent();
        if (parent != null && Files.notExists(parent)) Files.createDirectories(parent);
        if (Files.notExists(JSON_PATH)) Files.writeString(JSON_PATH, "[]", StandardCharsets.UTF_8);
    }

    private static void appendField(StringBuilder json, String key, String value, boolean withComma) {
        json.append("    \"").append(escape(key)).append("\": \"").append(escape(value)).append("\"");
        if (withComma) json.append(",");
        json.append("\n");
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "\\n");
    }

    private static List<String> splitObjects(String content) {
        List<String> objects = new ArrayList<>();
        int depth = 0; boolean inString = false; int start = -1;
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            char previous = i > 0 ? content.charAt(i - 1) : '\0';
            if (ch == '"' && previous != '\\') inString = !inString;
            if (inString) continue;
            if (ch == '{') { if (depth == 0) start = i; depth++; }
            else if (ch == '}') { depth--; if (depth == 0 && start >= 0) objects.add(content.substring(start, i + 1)); }
        }
        return objects;
    }

    private static Map<String, String> parseObject(String objectJson) {
        Map<String, String> map = new HashMap<>();
        String body = objectJson.trim();
        if (body.startsWith("{")) body = body.substring(1);
        if (body.endsWith("}")) body = body.substring(0, body.length() - 1);
        List<String> pairs = splitPairs(body);
        for (String pair : pairs) {
            int colonIndex = findColonOutsideQuotes(pair);
            if (colonIndex < 0) continue;
            map.put(unquote(pair.substring(0, colonIndex).trim()), unquote(pair.substring(colonIndex + 1).trim()));
        }
        return map;
    }

    private static List<String> splitPairs(String body) {
        List<String> pairs = new ArrayList<>();
        boolean inString = false; int start = 0;
        for (int i = 0; i < body.length(); i++) {
            char ch = body.charAt(i);
            char previous = i > 0 ? body.charAt(i - 1) : '\0';
            if (ch == '"' && previous != '\\') inString = !inString;
            else if (ch == ',' && !inString) { pairs.add(body.substring(start, i).trim()); start = i + 1; }
        }
        if (start < body.length()) pairs.add(body.substring(start).trim());
        return pairs;
    }

    private static int findColonOutsideQuotes(String text) {
        boolean inString = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            char previous = i > 0 ? text.charAt(i - 1) : '\0';
            if (ch == '"' && previous != '\\') inString = !inString;
            else if (ch == ':' && !inString) return i;
        }
        return -1;
    }

    private static String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"")) trimmed = trimmed.substring(1);
        if (trimmed.endsWith("\"")) trimmed = trimmed.substring(0, trimmed.length() - 1);
        return trimmed.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public static String generateApplicationId(String prefix) {
        return prefix + "-" + LocalDateTime.now().format(ID_TIME_FORMAT);
    }

    public static String nowForDisplay() {
        return LocalDateTime.now().format(DISPLAY_TIME_FORMAT);
    }
}