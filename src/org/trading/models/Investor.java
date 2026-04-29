package org.trading.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Investor extends User {
    private double balance;
    private Map<String, PortfolioItem> portfolio;
    private List<Transaction> transactions;

    public Investor(String username, double initialBalance) {
        super(username);
        this.balance = initialBalance;
        this.portfolio = new HashMap<>();
        this.transactions = new ArrayList<>();
    }

    @Override
    public void displayRole() {
        System.out.println("Rol utilizator: Investitor (cu portofoliu si balanta)");
    }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public Map<String, PortfolioItem> getPortfolio() { return portfolio; }
    public List<Transaction> getTransactions() { return transactions; }

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }
}
