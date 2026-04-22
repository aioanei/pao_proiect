package org.trading.models;

public class PortfolioItem {
    private Asset asset;
    private int quantity;
    private double averageBuyPrice;

    public PortfolioItem(Asset asset, int quantity, double averageBuyPrice) {
        this.asset = asset;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
    }

    public Asset getAsset() { return asset; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getAverageBuyPrice() { return averageBuyPrice; }
    public void setAverageBuyPrice(double averageBuyPrice) { this.averageBuyPrice = averageBuyPrice; }
}
