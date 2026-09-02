package models.User;

import models.Account.Account;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    private final String customerID;
    private final List<Account> accounts = new ArrayList<>();

    public Customer(String icNumber, String full_name, LocalDateTime createdTime, String address, String phoneNumber, String gmail, String passwordHash, String username, String customerID) {
        super(icNumber, full_name, createdTime, address, phoneNumber, gmail, passwordHash, username);
        this.customerID = customerID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void addAccount(Account account) {
        this.accounts.add(account);
    }
}