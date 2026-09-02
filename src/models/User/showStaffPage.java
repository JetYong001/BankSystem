package models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class showStaffPage {
    public static final List<Role> FRONTLINE = new ArrayList<>();
    public static final List<Role> MANAGERIAL = new ArrayList<>();
    public static final List<Role> BACKOFFICE = new ArrayList<>();

    static {
        FRONTLINE.add(createRole("Bank Teller", new ArrayList<>(Arrays.asList(
                "Current Cash Deposit/Withdrawal", "Account Opening", "Fund Transfer"))));

        MANAGERIAL.add(createRole("Branch Manager", new ArrayList<>(Arrays.asList(
                "Application Approval", "Customer Profiling"))));

        BACKOFFICE.add(createRole("System Administrator", new ArrayList<>(Arrays.asList(
                "Staff Management", "Role-Based Access Control"))));

        BACKOFFICE.add(createRole("System Configuration", new ArrayList<>(Arrays.asList(
                "Interest Rate Management", "Account Freezing/Unfreezing"))));

        Set<String> allFunctions = new LinkedHashSet<>();
        List<Role> allExistingRoles = new ArrayList<>();
        allExistingRoles.addAll(FRONTLINE);
        allExistingRoles.addAll(MANAGERIAL);
        allExistingRoles.addAll(BACKOFFICE);

        for (Role r : allExistingRoles) {
            allFunctions.addAll(r.getFunctions());
        }

        allFunctions.remove("Profile");

        MANAGERIAL.add(createRole("BOSS", new ArrayList<>(allFunctions)));
    }

    private static Role createRole(String roleName, ArrayList<String> specificFunctions) {
        ArrayList<String> finalFunctions = new ArrayList<>();

        if (specificFunctions != null) {
            finalFunctions.addAll(specificFunctions);
        }

        finalFunctions.add("Financial Calculators");
        finalFunctions.add("Setting");
        finalFunctions.add("Profile");
        return new Role(roleName, finalFunctions);
    }

    public static ArrayList<String> getPermissionsByRole(String roleName) {
        ArrayList<Role> allRoles = new ArrayList<>();
        allRoles.addAll(FRONTLINE);
        allRoles.addAll(MANAGERIAL);
        allRoles.addAll(BACKOFFICE);

        for (Role r : allRoles) {
            if (r.getRoleName().equalsIgnoreCase(roleName)) {
                return r.getFunctions();
            }
        }

        ArrayList<String> defaultPerms = new ArrayList<>();
        defaultPerms.add("Financial Calculators");
        defaultPerms.add("Setting");
        defaultPerms.add("Profile");
        return defaultPerms;
    }
}