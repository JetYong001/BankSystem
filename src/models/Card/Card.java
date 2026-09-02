package models.Card;

import models.Account.Account;

public abstract class Card {
    private final String cardNumber;
    private String pinHash;
    private String cvv;
    private String expiryDate;
    private boolean isLocked = false;
    protected Account linkedAccount;

    public Card(String cardNumber, String cvv, String expiryDate, String pinHash, Account linkedAccount) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
        this.pinHash = pinHash;
        this.linkedAccount = linkedAccount;
    }

    public String getCardNumber() { return cardNumber; }

    public String getCvv() {
        return cvv;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public Account getLinkedAccount() {
        return linkedAccount;
    }
}