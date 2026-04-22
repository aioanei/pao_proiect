package org.trading.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private TransactionType type;
    private String assetSymbol;
    private int quantity;
    private double pricePerUnit;
    private LocalDateTime timestamp;

    public Transaction(TransactionType type, String assetSymbol, int quantity, double pricePerUnit) {
        this.type = type;
        this.assetSymbol = assetSymbol;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] %s: %d units of %s at $%.2f", timestamp.format(formatter), type, quantity, assetSymbol, pricePerUnit);
    }
}
