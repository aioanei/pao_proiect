package org.trading.models;

import java.util.Random;
import java.util.TreeSet;

public class Market {
    private TreeSet<Asset> assets;
    private Random random;

    public Market() {
        assets = new TreeSet<>();
        random = new Random();
    }

    public void addAsset(Asset asset) {
        assets.add(asset);
    }

    public TreeSet<Asset> getAssets() {
        return assets;
    }

    public Asset getAssetBySymbol(String symbol) {
        for (Asset asset : assets) {
            if (asset.getSymbol().equalsIgnoreCase(symbol)) {
                return asset;
            }
        }
        return null;
    }

    public void tick() {
        TreeSet<Asset> updatedAssets = new TreeSet<>();
        
        for (Asset asset : assets) {
            double changePercent = -0.05 + (0.1 * random.nextDouble());
            double newPrice = asset.getPrice() * (1 + changePercent);
            if (newPrice < 0.01) newPrice = 0.01;
            
            asset.setPrice(newPrice);
            updatedAssets.add(asset);
        }
        
        this.assets = updatedAssets;
    }
}
