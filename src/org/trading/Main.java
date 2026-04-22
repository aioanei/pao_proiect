package org.trading;

import org.trading.services.TradingService;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TradingService service = new TradingService();
        service.seedMarket();
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== TERMINAL BURSA ===");
        
        service.registerInvestor("Investitor", 10000.0);
        service.startLiveMarket(); // Pornim simularea dinamica in fundal DOAR DUPA initializare
        
        boolean running = true;
        while (running) {
            System.out.println("\n=== MENIU PRINCIPAL ===");
            System.out.println("1. Afiseaza activele de pe piata (Sortate dupa pret)");
            System.out.println("2. Afiseaza portofoliul meu");
            System.out.println("3. Cumpara un activ");
            System.out.println("4. Vinde un activ");
            System.out.println("5. Depune fonduri");
            System.out.println("6. Afiseaza istoricul tranzactiilor");
            System.out.println("7. Vezi cel mai scump activ de pe piata");
            System.out.println("8. Vezi istoricul pretului pentru un activ");
            System.out.println("9. Iesire");
            System.out.print("Alegeti o actiune: ");

            int choice = -1;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Input invalid. Va rugam introduceti un numar.");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1: service.viewMarket(); break;
                case 2: service.viewPortfolio(); break;
                case 3:
                    System.out.print("Simbolul activului dorit (ex: AAPL, BTC): ");
                    String buySymbol = scanner.nextLine().toUpperCase();
                    System.out.print("Cantitate: ");
                    int buyQty = scanner.nextInt();
                    service.buyAsset(buySymbol, buyQty);
                    break;
                case 4:
                    System.out.print("Simbolul activului de vandut: ");
                    String sellSymbol = scanner.nextLine().toUpperCase();
                    System.out.print("Cantitate: ");
                    int sellQty = scanner.nextInt();
                    service.sellAsset(sellSymbol, sellQty);
                    break;
                case 5:
                    System.out.print("Suma de depus ($): ");
                    double amount = scanner.nextDouble();
                    service.depositFunds(amount);
                    break;
                case 6: service.viewTransactionHistory(); break;
                case 7: service.getMostExpensiveAsset(); break;
                case 8:
                    System.out.print("Simbolul activului pentru istoric: ");
                    String historySymbol = scanner.nextLine().toUpperCase();
                    service.viewAssetPriceHistory(historySymbol);
                    break;
                case 9:
                    running = false;
                    System.out.println("La revedere!");
                    break;
                default: System.out.println("Optiune invalida.");
            }
        }
        scanner.close();
    }
}