package DAO;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RateDAO {
    private static final String FILE_PATH = "data/ratejson";

    public static Map<String, Double> getAllRates() {
        Map<String, Double> rates = new HashMap<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));

            Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\\d+\\.?\\d*)");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                rates.put(matcher.group(1), Double.parseDouble(matcher.group(2)));
            }
        } catch (Exception e) {

            rates.put("3", 2.85);
            rates.put("6", 3.10);
            rates.put("12", 3.50);
            rates.put("24", 3.75);
        }
        return rates;
    }

    public static boolean updateRates(Map<String, Double> newRates) {
        StringBuilder json = new StringBuilder("{\n");
        int count = 0;
        for (Map.Entry<String, Double> entry : newRates.entrySet()) {
            json.append("  \"").append(entry.getKey()).append("\": ").append(entry.getValue());
            if (++count < newRates.size()) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("}");

        try {
            Files.write(Paths.get(FILE_PATH), json.toString().getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static double calculateFDInterest(double amount, String months) {
        Map<String, Double> rates = getAllRates();
        double rate = rates.getOrDefault(months, 0.0) / 100;
        return amount * rate * (Integer.parseInt(months) / 12.0);
    }
}