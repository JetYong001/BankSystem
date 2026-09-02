package models.Card;

import models.Account.Account;

public class DebitCard extends Card {

    public DebitCard(String cardNumber, String cvv, String expiryDate, String pinHash, Account linkedAccount) {
        super(cardNumber, cvv, expiryDate, pinHash, linkedAccount);
    }
}
