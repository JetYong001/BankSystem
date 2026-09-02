package models.User;

import java.time.LocalDateTime;

public class Staff extends User {
    private final String staffID;
    private String position;


    public Staff(String icNumber, String full_name, LocalDateTime createdTime, String address, String phoneNumber, String gmail, String passwordHash, String username, String staffID, String position) {
        super(icNumber, full_name, createdTime, address, phoneNumber, gmail, passwordHash, username);
        this.staffID = staffID;
        this.position = position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getStaffID() {
        return staffID;
    }

    public String getPosition() {
        return position;
    }

    public String getStaffId() {
        return staffID;
    }
}
