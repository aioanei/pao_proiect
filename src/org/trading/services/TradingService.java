package org.trading.services;

import org.trading.audit.AuditService;
import org.trading.db.DatabaseInitializer;
import org.trading.models.*;
import org.trading.repositories.CryptoRepository;
import org.trading.repositories.InvestorRepository;
import org.trading.repositories.PortfolioItemRepository;
import org.trading.repositories.StockRepository;
import org.trading.repositories.TransactionRepository;

public class TradingService {
    private Market market;
    private Investor activeInvestor;
    private final AuditService auditService;
    private final StockRepository stockRepository;
    private final CryptoRepository cryptoRepository;
    private final InvestorRepository investorRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final TransactionRepository transactionRepository;

    public TradingService() {
        DatabaseInitializer.getInstance().initialize();
        this.market = new Market();
        this.auditService = AuditService.getInstance();
        this.stockRepository = StockRepository.getInstance();
        this.cryptoRepository = CryptoRepository.getInstance();
        this.investorRepository = InvestorRepository.getInstance();
        this.portfolioItemRepository = PortfolioItemRepository.getInstance();
        this.transactionRepository = TransactionRepository.getInstance();
    }

    public void seedMarket() {
        auditService.logAction("seed_market");
        seedDefaultAssetsIfMissing();
        loadMarketFromDatabase();
    }

    public void registerInvestor(String username, double initialBalance) {
        auditService.logAction("register_investor");
        this.activeInvestor = investorRepository.findById(username).orElseGet(() -> {
            Investor investor = new Investor(username, initialBalance);
            investorRepository.create(investor);
            return investor;
        });
        loadInvestorState();
        System.out.println("Investitor activ: " + activeInvestor.getUsername() + " (Balanta: $" + activeInvestor.getBalance() + ")");
    }

    public void depositFunds(double amount) {
        auditService.logAction("deposit_funds");
        if (activeInvestor == null) return;
        if (amount <= 0) {
            System.out.println("Suma trebuie sa fie pozitiva.");
            return;
        }
        activeInvestor.setBalance(activeInvestor.getBalance() + amount);
        investorRepository.update(activeInvestor);
        System.out.printf("S-au adaugat $%.2f. Noua balanta: $%.2f%n", amount, activeInvestor.getBalance());
    }

    public void viewMarket() {
        auditService.logAction("view_market");
        System.out.println("\n--- Piata curenta (Sortata crescator dupa pret) ---");
        for (Asset asset : market.getAssets()) {
            System.out.println(asset);
        }
    }

    public void startLiveMarket() {
        auditService.logAction("start_live_market");
        Thread liveThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000); // Preturile se schimba automat la fiecare 3 secunde
                    market.tick();
                    persistMarketPrices();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        liveThread.setDaemon(true); // Thread-ul se inchide odata cu inchiderea aplicatiei
        liveThread.start();
    }

    public void buyAsset(String symbol, int quantity) {
        auditService.logAction("buy_asset");
        if (activeInvestor == null) return;
        if (quantity <= 0) {
            System.out.println("Cantitatea trebuie sa fie pozitiva.");
            return;
        }
        
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
        t.setUsername(activeInvestor.getUsername());
        activeInvestor.addTransaction(t);
        investorRepository.update(activeInvestor);
        portfolioItemRepository.upsertForInvestor(activeInvestor.getUsername(), pItem);
        transactionRepository.create(t);

        System.out.println("Achizitie reusita: " + quantity + " x " + symbol + " | Total: $" + totalCost);
    }

    public void sellAsset(String symbol, int quantity) {
        auditService.logAction("sell_asset");
        if (activeInvestor == null) return;
        if (quantity <= 0) {
            System.out.println("Cantitatea trebuie sa fie pozitiva.");
            return;
        }
        
        PortfolioItem pItem = activeInvestor.getPortfolio().get(symbol);
        if (pItem == null || pItem.getQuantity() < quantity) {
            System.out.println("Nu detii suficient " + symbol + " pentru a vinde.");
            return;
        }

        Asset assetOnMarket = market.getAssetBySymbol(symbol);
        if (assetOnMarket == null) {
            System.out.println("Activul " + symbol + " nu mai exista pe piata.");
            return;
        }
        double totalRevenue = assetOnMarket.getPrice() * quantity;

        activeInvestor.setBalance(activeInvestor.getBalance() + totalRevenue);
        pItem.setQuantity(pItem.getQuantity() - quantity);

        if (pItem.getQuantity() == 0) {
            activeInvestor.getPortfolio().remove(symbol);
            portfolioItemRepository.deleteForInvestor(activeInvestor.getUsername(), symbol);
        } else {
            portfolioItemRepository.updateForInvestor(activeInvestor.getUsername(), pItem);
        }

        Transaction t = new Transaction(TransactionType.SELL, symbol, quantity, assetOnMarket.getPrice());
        t.setUsername(activeInvestor.getUsername());
        activeInvestor.addTransaction(t);
        investorRepository.update(activeInvestor);
        transactionRepository.create(t);

        System.out.println("Vanzare reusita: " + quantity + " x " + symbol + " | Total obtinut: $" + totalRevenue);
    }

    public void viewPortfolio() {
        auditService.logAction("view_portfolio");
        if (activeInvestor == null) return;
        System.out.println("\n--- Portofoliul lui " + activeInvestor.getUsername() + " ---");
        System.out.printf("Balanta curenta: $%.2f%n", activeInvestor.getBalance());
        int totalOwnedQuantity = portfolioItemRepository.totalQuantityForInvestor(activeInvestor.getUsername());
        System.out.println("Numar total de unitati detinute: " + totalOwnedQuantity);
        
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
        auditService.logAction("view_transaction_history");
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
        auditService.logAction("get_most_expensive_asset");
        if (market.getAssets().isEmpty()) {
            System.out.println("Piata este goala.");
            return;
        }
        Asset mostExpensive = market.getAssets().last(); // get the highest price due to Comparable
        System.out.println("\nCel mai scump activ curent este: " + mostExpensive);
    }

    public void viewAssetPriceHistory(String symbol) {
        auditService.logAction("view_asset_price_history");
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
        auditService.logAction("search_asset");
        Asset asset = market.getAssetBySymbol(symbol);
        if (asset == null) {
            System.out.println("Activul " + symbol + " nu a fost gasit pe piata.");
        } else {
            System.out.println("Rezultat cautare: " + asset.toString());
        }
    }

    public void viewStocksOnly() {
        auditService.logAction("view_stocks_only");
        System.out.println("\n--- Actiuni (Stocks) disponibile ---");
        for (Asset asset : market.getAssets()) {
            if (asset instanceof Stock) {
                System.out.println(asset);
            }
        }
    }

    public void viewCryptosOnly() {
        auditService.logAction("view_cryptos_only");
        System.out.println("\n--- Criptomonede disponibile ---");
        for (Asset asset : market.getAssets()) {
            if (asset instanceof Crypto) {
                System.out.println(asset);
            }
        }
    }

    public void withdrawFunds(double amount) {
        auditService.logAction("withdraw_funds");
        if (activeInvestor == null) return;
        if (amount <= 0) {
            System.out.println("Suma trebuie sa fie pozitiva.");
            return;
        }
        if (amount > activeInvestor.getBalance()) {
            System.out.println("Fonduri insuficiente pentru retragere! Balanta curenta: $" + activeInvestor.getBalance());
            return;
        }
        activeInvestor.setBalance(activeInvestor.getBalance() - amount);
        investorRepository.update(activeInvestor);
        System.out.printf("S-au retras $%.2f. Noua balanta: $%.2f%n", amount, activeInvestor.getBalance());
    }

    public void exitApplication() {
        auditService.logAction("exit_application");
    }

    private void seedDefaultAssetsIfMissing() {
        createStockIfMissing(new Stock("AAPL", 150.0, "Apple Inc.", 0.6));
        createStockIfMissing(new Stock("TSLA", 200.0, "Tesla Inc.", 0.0));
        createStockIfMissing(new Stock("MSFT", 300.0, "Microsoft Corp", 0.9));
        createCryptoIfMissing(new Crypto("BTC", 45000.0, "Bitcoin"));
        createCryptoIfMissing(new Crypto("ETH", 3000.0, "Ethereum"));
    }

    private void createStockIfMissing(Stock stock) {
        if (stockRepository.findById(stock.getSymbol()).isEmpty()) {
            stockRepository.create(stock);
        }
    }

    private void createCryptoIfMissing(Crypto crypto) {
        if (cryptoRepository.findById(crypto.getSymbol()).isEmpty()) {
            cryptoRepository.create(crypto);
        }
    }

    private void loadMarketFromDatabase() {
        this.market = new Market();
        for (Stock stock : stockRepository.findAll()) {
            market.addAsset(stock);
        }
        for (Crypto crypto : cryptoRepository.findAll()) {
            market.addAsset(crypto);
        }
    }

    private void loadInvestorState() {
        activeInvestor.getPortfolio().clear();
        activeInvestor.getPortfolio().putAll(portfolioItemRepository.findByInvestor(activeInvestor.getUsername()));

        activeInvestor.getTransactions().clear();
        activeInvestor.getTransactions().addAll(transactionRepository.findByInvestor(activeInvestor.getUsername()));
    }

    private void persistMarketPrices() {
        for (Asset asset : market.getAssets()) {
            if (asset instanceof Stock stock) {
                stockRepository.update(stock);
            } else if (asset instanceof Crypto crypto) {
                cryptoRepository.update(crypto);
            }
        }
    }
}
