package models.Card;

import models.Account.Account;

public class CreditCard extends Card {
    private double creditLimit;
    private double currentDebt;

    public CreditCard(String cardNumber, String cvv, String expiryDate, String pinHash, Account linkedAccount, double creditLimit, double currentDebt) {
        super(cardNumber, cvv, expiryDate, pinHash, linkedAccount);
        this.creditLimit = creditLimit;
        this.currentDebt = currentDebt;
    }
}
