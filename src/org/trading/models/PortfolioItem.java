package org.trading.models;

public class PortfolioItem {
    private Asset asset;
    private double quantity;
    private double averageBuyPrice;

    public PortfolioItem(Asset asset, double quantity, double averageBuyPrice) {
        this.asset = asset;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
    }

    public Asset getAsset() { return asset; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getAverageBuyPrice() { return averageBuyPrice; }
    public void setAverageBuyPrice(double averageBuyPrice) { this.averageBuyPrice = averageBuyPrice; }
}
