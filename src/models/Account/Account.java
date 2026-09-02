package models.Account;

import models.Card.Card;

import java.util.List;

public abstract class Account {
    protected String accountNum;
    protected double balance;
    protected List<Card> boundCard;

    public Account(String accountNum, double balance, List<Card> card) {
        this.accountNum = accountNum;
        this.balance = balance;
        this.boundCard = card;
    }

    public String getAccountNum() { return accountNum; }

    public double getBalance() {
        return balance;
    }


    public Card[] getCardNumber() {
        if (boundCard == null) return new Card[0];
        return boundCard.toArray(new Card[0]);
    }

    public void setBalance(double v) {
    }
}