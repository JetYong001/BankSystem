package models.User;

import java.util.ArrayList;

public class Role {
    String roleName;
    ArrayList<String> functions;

    public Role() {
    }

    public Role(String roleName, ArrayList<String> functions) {
        this.roleName = roleName;
        this.functions = functions;
    }

    public String getRoleName() {
        return roleName;
    }

    public ArrayList<String> getFunctions() {
        return functions;
    }
}
