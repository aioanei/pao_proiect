package org.trading.repositories;

import org.trading.models.Asset;
import org.trading.models.PortfolioItem;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class PortfolioItemRepository extends AbstractJdbcRepository<PortfolioItem, String> {
    private static PortfolioItemRepository instance;

    private final StockRepository stockRepository;
    private final CryptoRepository cryptoRepository;

    private PortfolioItemRepository() {
        this.stockRepository = StockRepository.getInstance();
        this.cryptoRepository = CryptoRepository.getInstance();
    }

    public static synchronized PortfolioItemRepository getInstance() {
        if (instance == null) {
            instance = new PortfolioItemRepository();
        }
        return instance;
    }

    @Override
    public void create(PortfolioItem item) {
        throw new UnsupportedOperationException("PortfolioItem are nevoie de username. Folositi createForInvestor.");
    }

    public void createForInvestor(String username, PortfolioItem item) {
        String sql = """
                INSERT INTO portfolio_items(username, asset_symbol, quantity, average_buy_price)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, item.getAsset().getSymbol());
            statement.setInt(3, item.getQuantity());
            statement.setDouble(4, item.getAverageBuyPrice());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("create portfolio item", e);
        }
    }

    @Override
    public Optional<PortfolioItem> findById(String ignored) {
        throw new UnsupportedOperationException("PortfolioItem are cheie compusa. Folositi findByInvestorAndSymbol.");
    }

    public Optional<PortfolioItem> findByInvestorAndSymbol(String username, String assetSymbol) {
        String sql = """
                SELECT asset_symbol, quantity, average_buy_price
                FROM portfolio_items
                WHERE username = ? AND asset_symbol = ?
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, assetSymbol);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw databaseError("read portfolio item", e);
        }
    }

    @Override
    public java.util.List<PortfolioItem> findAll() {
        throw new UnsupportedOperationException("Folositi findByInvestor pentru portofoliul unui investitor.");
    }

    public Map<String, PortfolioItem> findByInvestor(String username) {
        String sql = """
                SELECT asset_symbol, quantity, average_buy_price
                FROM portfolio_items
                WHERE username = ?
                """;
        Map<String, PortfolioItem> portfolio = new HashMap<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PortfolioItem item = mapRow(resultSet);
                    portfolio.put(item.getAsset().getSymbol(), item);
                }
            }
            return portfolio;
        } catch (SQLException e) {
            throw databaseError("read investor portfolio", e);
        }
    }

    @Override
    public void update(PortfolioItem item) {
        throw new UnsupportedOperationException("PortfolioItem are nevoie de username. Folositi updateForInvestor.");
    }

    public void updateForInvestor(String username, PortfolioItem item) {
        String sql = """
                UPDATE portfolio_items
                SET quantity = ?, average_buy_price = ?
                WHERE username = ? AND asset_symbol = ?
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, item.getQuantity());
            statement.setDouble(2, item.getAverageBuyPrice());
            statement.setString(3, username);
            statement.setString(4, item.getAsset().getSymbol());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("update portfolio item", e);
        }
    }

    public void upsertForInvestor(String username, PortfolioItem item) {
        if (findByInvestorAndSymbol(username, item.getAsset().getSymbol()).isPresent()) {
            updateForInvestor(username, item);
        } else {
            createForInvestor(username, item);
        }
    }

    @Override
    public void delete(String ignored) {
        throw new UnsupportedOperationException("PortfolioItem are cheie compusa. Folositi deleteForInvestor.");
    }

    public void deleteForInvestor(String username, String assetSymbol) {
        String sql = "DELETE FROM portfolio_items WHERE username = ? AND asset_symbol = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, assetSymbol);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("delete portfolio item", e);
        }
    }

    public int totalQuantityForInvestor(String username) {
        String sql = "{ ? = call total_portfolio_quantity(?) }";
        try (Connection connection = getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {
            statement.registerOutParameter(1, Types.INTEGER);
            statement.setString(2, username);
            statement.execute();
            return statement.getInt(1);
        } catch (SQLException e) {
            throw databaseError("call total_portfolio_quantity", e);
        }
    }

    private PortfolioItem mapRow(ResultSet resultSet) throws SQLException {
        String symbol = resultSet.getString("asset_symbol");
        Asset asset = findAsset(symbol);
        return new PortfolioItem(
                asset,
                resultSet.getInt("quantity"),
                resultSet.getDouble("average_buy_price")
        );
    }

    private Asset findAsset(String symbol) {
        return stockRepository.findById(symbol)
                .<Asset>map(stock -> stock)
                .or(() -> cryptoRepository.findById(symbol).map(crypto -> crypto))
                .orElseThrow(() -> new RepositoryException("Nu exista activul " + symbol + " pentru portofoliu.", null));
    }
}
