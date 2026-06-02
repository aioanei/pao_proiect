package org.trading.repositories;

import org.trading.models.Stock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class StockRepository extends AbstractJdbcRepository<Stock, String> {
    private static StockRepository instance;

    private StockRepository() {
    }

    public static synchronized StockRepository getInstance() {
        if (instance == null) {
            instance = new StockRepository();
        }
        return instance;
    }

    @Override
    public void create(Stock stock) {
        String sql = "INSERT INTO stocks(symbol, price, company_name, dividend_yield) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, stock.getSymbol());
            statement.setDouble(2, stock.getPrice());
            statement.setString(3, stock.getCompanyName());
            statement.setDouble(4, stock.getDividendYield());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("create stock", e);
        }
    }

    @Override
    public Optional<Stock> findById(String symbol) {
        String sql = "SELECT symbol, price, company_name, dividend_yield FROM stocks WHERE symbol = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, symbol);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw databaseError("read stock", e);
        }
    }

    @Override
    public List<Stock> findAll() {
        String sql = "SELECT symbol, price, company_name, dividend_yield FROM stocks";
        List<Stock> stocks = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                stocks.add(mapRow(resultSet));
            }
            return stocks;
        } catch (SQLException e) {
            throw databaseError("read all stocks", e);
        }
    }

    @Override
    public void update(Stock stock) {
        String sql = "UPDATE stocks SET price = ?, company_name = ?, dividend_yield = ? WHERE symbol = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, stock.getPrice());
            statement.setString(2, stock.getCompanyName());
            statement.setDouble(3, stock.getDividendYield());
            statement.setString(4, stock.getSymbol());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("update stock", e);
        }
    }

    @Override
    public void delete(String symbol) {
        String sql = "DELETE FROM stocks WHERE symbol = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, symbol);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("delete stock", e);
        }
    }

    private Stock mapRow(ResultSet resultSet) throws SQLException {
        return new Stock(
                resultSet.getString("symbol"),
                resultSet.getDouble("price"),
                resultSet.getString("company_name"),
                resultSet.getDouble("dividend_yield")
        );
    }
}
