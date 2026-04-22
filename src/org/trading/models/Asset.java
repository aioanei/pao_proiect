package org.trading.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Asset implements Comparable<Asset> {
    protected String symbol;
    protected double price;
    protected List<Double> priceHistory;

    public Asset(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
        this.priceHistory = new ArrayList<>();
        this.priceHistory.add(price);
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getPrice() { return price; }
    public void setPrice(double price) { 
        this.price = price; 
        this.priceHistory.add(price); // Salvam automat noul pret la istoric
    }

    public List<Double> getPriceHistory() {
        return priceHistory;
    }

    @Override
    public int compareTo(Asset other) {
        int priceCompare = Double.compare(this.price, other.price);
        if (priceCompare != 0) {
            return priceCompare;
        }
        return this.symbol.compareTo(other.symbol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return Objects.equals(symbol, asset.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return String.format("%s: $%.2f", symbol, price);
    }
}
