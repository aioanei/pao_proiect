package org.trading.repositories;

import org.trading.models.Transaction;
import org.trading.models.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TransactionRepository extends AbstractJdbcRepository<Transaction, Long> {
    private static TransactionRepository instance;

    private TransactionRepository() {
    }

    public static synchronized TransactionRepository getInstance() {
        if (instance == null) {
            instance = new TransactionRepository();
        }
        return instance;
    }

    @Override
    public void create(Transaction transaction) {
        String sql = """
                INSERT INTO transactions(username, type, asset_symbol, quantity, price_per_unit, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, transaction.getUsername());
            statement.setString(2, transaction.getType().name());
            statement.setString(3, transaction.getAssetSymbol());
            statement.setDouble(4, transaction.getQuantity());
            statement.setDouble(5, transaction.getPricePerUnit());
            statement.setTimestamp(6, Timestamp.valueOf(transaction.getTimestamp()));
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw databaseError("create transaction", e);
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        String sql = """
                SELECT id, username, type, asset_symbol, quantity, price_per_unit, created_at
                FROM transactions
                WHERE id = ?
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw databaseError("read transaction", e);
        }
    }

    @Override
    public List<Transaction> findAll() {
        String sql = """
                SELECT id, username, type, asset_symbol, quantity, price_per_unit, created_at
                FROM transactions
                ORDER BY created_at
                """;
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                transactions.add(mapRow(resultSet));
            }
            return transactions;
        } catch (SQLException e) {
            throw databaseError("read all transactions", e);
        }
    }

    public List<Transaction> findByInvestor(String username) {
        String sql = """
                SELECT id, username, type, asset_symbol, quantity, price_per_unit, created_at
                FROM transactions
                WHERE username = ?
                ORDER BY created_at
                """;
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapRow(resultSet));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw databaseError("read investor transactions", e);
        }
    }

    @Override
    public void update(Transaction transaction) {
        String sql = """
                UPDATE transactions
                SET username = ?, type = ?, asset_symbol = ?, quantity = ?, price_per_unit = ?, created_at = ?
                WHERE id = ?
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transaction.getUsername());
            statement.setString(2, transaction.getType().name());
            statement.setString(3, transaction.getAssetSymbol());
            statement.setDouble(4, transaction.getQuantity());
            statement.setDouble(5, transaction.getPricePerUnit());
            statement.setTimestamp(6, Timestamp.valueOf(transaction.getTimestamp()));
            statement.setLong(7, transaction.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("update transaction", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("delete transaction", e);
        }
    }

    private Transaction mapRow(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("created_at");
        return new Transaction(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                TransactionType.valueOf(resultSet.getString("type")),
                resultSet.getString("asset_symbol"),
                resultSet.getDouble("quantity"),
                resultSet.getDouble("price_per_unit"),
                timestamp.toLocalDateTime()
        );
    }
}
