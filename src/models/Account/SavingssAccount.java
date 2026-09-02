package models.Account;

import models.Card.Card;

import java.util.List;

public class SavingssAccount extends Account {
    public SavingssAccount(String accountNum, double balance, List<Card> cards) {
        super(accountNum, balance, cards);
    }
}
