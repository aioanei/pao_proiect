package org.trading.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private long id;
    private String username;
    private TransactionType type;
    private String assetSymbol;
    private int quantity;
    private double pricePerUnit;
    private LocalDateTime timestamp;

    public Transaction(TransactionType type, String assetSymbol, int quantity, double pricePerUnit) {
        this(0, null, type, assetSymbol, quantity, pricePerUnit, LocalDateTime.now());
    }

    public Transaction(long id, String username, TransactionType type, String assetSymbol, int quantity, double pricePerUnit, LocalDateTime timestamp) {
        this.id = id;
        this.username = username;
        this.type = type;
        this.assetSymbol = assetSymbol;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public String getAssetSymbol() { return assetSymbol; }
    public void setAssetSymbol(String assetSymbol) { this.assetSymbol = assetSymbol; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] %s: %d units of %s at $%.2f", timestamp.format(formatter), type, quantity, assetSymbol, pricePerUnit);
    }
}
