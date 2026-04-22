package org.trading.services;

import org.trading.models.*;

public class TradingService {
    private Market market;
    private Investor activeInvestor;

    public TradingService() {
        this.market = new Market();
    }

    public void seedMarket() {
        market.addAsset(new Stock("AAPL", 150.0, "Apple Inc.", 0.6));
        market.addAsset(new Stock("TSLA", 200.0, "Tesla Inc.", 0.0));
        market.addAsset(new Stock("MSFT", 300.0, "Microsoft Corp", 0.9));
        market.addAsset(new Crypto("BTC", 45000.0, "Bitcoin"));
        market.addAsset(new Crypto("ETH", 3000.0, "Ethereum"));
    }

    public void registerInvestor(String username, double initialBalance) {
        this.activeInvestor = new Investor(username, initialBalance);
        System.out.println("Investitor inregistrat cu succes: " + username + " (Balanta: $" + initialBalance + ")");
    }

    public void depositFunds(double amount) {
        if (activeInvestor == null) return;
        activeInvestor.setBalance(activeInvestor.getBalance() + amount);
        System.out.printf("S-au adaugat $%.2f. Noua balanta: $%.2f%n", amount, activeInvestor.getBalance());
    }

    public void viewMarket() {
        System.out.println("\n--- Piata curenta (Sortata crescator dupa pret) ---");
        for (Asset asset : market.getAssets()) {
            System.out.println(asset);
        }
    }

    public void startLiveMarket() {
        Thread liveThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000); // Preturile se schimba automat la fiecare 3 secunde
                    market.tick();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        liveThread.setDaemon(true); // Thread-ul se inchide odata cu inchiderea aplicatiei
        liveThread.start();
    }

    public void buyAsset(String symbol, int quantity) {
        if (activeInvestor == null) return;
        
        Asset asset = market.getAssetBySymbol(symbol);
        if (asset == null) {
            System.out.println("Activul " + symbol + " nu exista pe piata.");
            return;
        }

        double totalCost = asset.getPrice() * quantity;
        if (activeInvestor.getBalance() < totalCost) {
            System.out.println("Fonduri insuficiente! Cost: $" + totalCost + ", Balanta: $" + activeInvestor.getBalance());
            return;
        }

        activeInvestor.setBalance(activeInvestor.getBalance() - totalCost);
        
        PortfolioItem pItem = activeInvestor.getPortfolio().getOrDefault(symbol, new PortfolioItem(asset, 0, 0));
        double previousTotalCost = pItem.getQuantity() * pItem.getAverageBuyPrice();
        int newQuantity = pItem.getQuantity() + quantity;
        double newAvgPrice = (previousTotalCost + totalCost) / newQuantity;
        
        pItem.setQuantity(newQuantity);
        pItem.setAverageBuyPrice(newAvgPrice);
        activeInvestor.getPortfolio().put(symbol, pItem);

        Transaction t = new Transaction(TransactionType.BUY, symbol, quantity, asset.getPrice());
        activeInvestor.addTransaction(t);

        System.out.println("Achizitie reusita: " + quantity + " x " + symbol + " | Total: $" + totalCost);
    }

    public void sellAsset(String symbol, int quantity) {
        if (activeInvestor == null) return;
        
        PortfolioItem pItem = activeInvestor.getPortfolio().get(symbol);
        if (pItem == null || pItem.getQuantity() < quantity) {
            System.out.println("Nu detii suficient " + symbol + " pentru a vinde.");
            return;
        }

        Asset assetOnMarket = market.getAssetBySymbol(symbol);
        double totalRevenue = assetOnMarket.getPrice() * quantity;

        activeInvestor.setBalance(activeInvestor.getBalance() + totalRevenue);
        pItem.setQuantity(pItem.getQuantity() - quantity);

        if (pItem.getQuantity() == 0) {
            activeInvestor.getPortfolio().remove(symbol);
        }

        Transaction t = new Transaction(TransactionType.SELL, symbol, quantity, assetOnMarket.getPrice());
        activeInvestor.addTransaction(t);

        System.out.println("Vanzare reusita: " + quantity + " x " + symbol + " | Total obtinut: $" + totalRevenue);
    }

    public void viewPortfolio() {
        if (activeInvestor == null) return;
        System.out.println("\n--- Portofoliul lui " + activeInvestor.getUsername() + " ---");
        System.out.printf("Balanta curenta: $%.2f%n", activeInvestor.getBalance());
        
        double totalValue = activeInvestor.getBalance();
        
        if (activeInvestor.getPortfolio().isEmpty()) {
            System.out.println("Portofoliul este gol.");
        } else {
            for (PortfolioItem item : activeInvestor.getPortfolio().values()) {
                Asset assetOnMarket = market.getAssetBySymbol(item.getAsset().getSymbol());
                double currentPrice = assetOnMarket.getPrice();
                double itemTotalValue = currentPrice * item.getQuantity();
                totalValue += itemTotalValue;

                System.out.printf("%s : %d buc | Pret mediu cumparare: $%.2f | Pret curent: $%.2f | Valoare totala: $%.2f%n",
                        item.getAsset().getSymbol(), item.getQuantity(), item.getAverageBuyPrice(), currentPrice, itemTotalValue);
            }
        }
        System.out.printf("-> Valoare Neta totala estimata: $%.2f%n", totalValue);
    }

    public void viewTransactionHistory() {
        if (activeInvestor == null) return;
        System.out.println("\n--- Istoric Tranzactii ---");
        if (activeInvestor.getTransactions().isEmpty()) {
            System.out.println("Nicio tranzactie efectuata.");
        } else {
            for (Transaction t : activeInvestor.getTransactions()) {
                System.out.println(t);
            }
        }
    }

    public void getMostExpensiveAsset() {
        if (market.getAssets().isEmpty()) {
            System.out.println("Piata este goala.");
            return;
        }
        Asset mostExpensive = market.getAssets().last(); // get the highest price due to Comparable
        System.out.println("\nCel mai scump activ curent este: " + mostExpensive);
    }

    public void viewAssetPriceHistory(String symbol) {
        Asset asset = market.getAssetBySymbol(symbol);
        if (asset == null) {
            System.out.println("Activul " + symbol + " nu exista pe piata.");
            return;
        }
        System.out.println("\n--- Istoric preturi pentru " + symbol + " ---");
        java.util.List<Double> history = asset.getPriceHistory();
        
        // Afisam ultimele max 15 instante pentru a nu aglomera terminalul
        int start = Math.max(0, history.size() - 15);
        for (int i = start; i < history.size(); i++) {
            System.out.printf("Trecut %d tick(-uri) de la inceput: $%.2f%n", i, history.get(i));
        }
    }

    public void searchAsset(String symbol) {
        Asset asset = market.getAssetBySymbol(symbol);
        if (asset == null) {
            System.out.println("Activul " + symbol + " nu a fost gasit pe piata.");
        } else {
            System.out.println("Rezultat cautare: " + asset.toString());
        }
    }

    public void viewStocksOnly() {
        System.out.println("\n--- Actiuni (Stocks) disponibile ---");
        for (Asset asset : market.getAssets()) {
            if (asset instanceof Stock) {
                System.out.println(asset);
            }
        }
    }

    public void viewCryptosOnly() {
        System.out.println("\n--- Criptomonede disponibile ---");
        for (Asset asset : market.getAssets()) {
            if (asset instanceof Crypto) {
                System.out.println(asset);
            }
        }
    }

    public void withdrawFunds(double amount) {
        if (activeInvestor == null) return;
        if (amount > activeInvestor.getBalance()) {
            System.out.println("Fonduri insuficiente pentru retragere! Balanta curenta: $" + activeInvestor.getBalance());
            return;
        }
        activeInvestor.setBalance(activeInvestor.getBalance() - amount);
        System.out.printf("S-au retras $%.2f. Noua balanta: $%.2f%n", amount, activeInvestor.getBalance());
    }
}
