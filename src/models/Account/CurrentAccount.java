package models.Account;

import models.Card.Card;

import java.util.List;

public class CurrentAccount extends Account {

    public CurrentAccount(String accountNum, double balance, List<Card> cards) {
        super(accountNum, balance, cards);
    }
}
