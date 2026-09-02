package models.User;
import java.time.LocalDateTime;

public abstract class User {
    private final String icNumber;
    private final String full_name;
    private String username;
    private String passwordHash;
    private String gmail;
    private String phoneNumber;
    private String address;
    private final LocalDateTime createdTime;


    public User(String icNumber, String full_name, LocalDateTime createdTime, String address, String phoneNumber, String gmail, String passwordHash, String username) {
        this.icNumber = icNumber;
        this.full_name = full_name;
        this.createdTime = createdTime;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.gmail = gmail;
        this.passwordHash = passwordHash;
        this.username = username;
    }

    public String getIcNumber() {
        return icNumber;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getUsername() {return username;}

    public String getPasswordHash() {return passwordHash;}

    public String getGmail() {
        return gmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }



    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void updatePassword(String plainPassword) {
        if (plainPassword != null && !plainPassword.isEmpty()) {
            this.passwordHash = org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
        }
    }

}