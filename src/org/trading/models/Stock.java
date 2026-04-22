package org.trading.models;

public class Stock extends Asset {
    private String companyName;
    private double dividendYield;

    public Stock(String symbol, double price, String companyName, double dividendYield) {
        super(symbol, price);
        this.companyName = companyName;
        this.dividendYield = dividendYield;
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public double getDividendYield() { return dividendYield; }
    public void setDividendYield(double dividendYield) { this.dividendYield = dividendYield; }

    @Override
    public String toString() {
        return String.format("[Stock] %s (%s) - $%.2f | Div: %.2f%%", symbol, companyName, price, dividendYield);
    }
}
