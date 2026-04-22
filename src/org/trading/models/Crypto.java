package org.trading.models;

public class Crypto extends Asset {
    private String blockchain;

    public Crypto(String symbol, double price, String blockchain) {
        super(symbol, price);
        this.blockchain = blockchain;
    }

    public String getBlockchain() { return blockchain; }
    public void setBlockchain(String blockchain) { this.blockchain = blockchain; }

    @Override
    public String toString() {
        return String.format("[Crypto] %s on %s - $%.2f", symbol, blockchain, price);
    }
}
