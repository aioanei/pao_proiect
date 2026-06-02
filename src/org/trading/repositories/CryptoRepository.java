package org.trading.repositories;

import org.trading.models.Crypto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CryptoRepository extends AbstractJdbcRepository<Crypto, String> {
    private static CryptoRepository instance;

    private CryptoRepository() {
    }

    public static synchronized CryptoRepository getInstance() {
        if (instance == null) {
            instance = new CryptoRepository();
        }
        return instance;
    }

    @Override
    public void create(Crypto crypto) {
        String sql = "INSERT INTO cryptos(symbol, price, blockchain) VALUES (?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, crypto.getSymbol());
            statement.setDouble(2, crypto.getPrice());
            statement.setString(3, crypto.getBlockchain());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("create crypto", e);
        }
    }

    @Override
    public Optional<Crypto> findById(String symbol) {
        String sql = "SELECT symbol, price, blockchain FROM cryptos WHERE symbol = ?";
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
            throw databaseError("read crypto", e);
        }
    }

    @Override
    public List<Crypto> findAll() {
        String sql = "SELECT symbol, price, blockchain FROM cryptos";
        List<Crypto> cryptos = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                cryptos.add(mapRow(resultSet));
            }
            return cryptos;
        } catch (SQLException e) {
            throw databaseError("read all cryptos", e);
        }
    }

    @Override
    public void update(Crypto crypto) {
        String sql = "UPDATE cryptos SET price = ?, blockchain = ? WHERE symbol = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, crypto.getPrice());
            statement.setString(2, crypto.getBlockchain());
            statement.setString(3, crypto.getSymbol());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("update crypto", e);
        }
    }

    @Override
    public void delete(String symbol) {
        String sql = "DELETE FROM cryptos WHERE symbol = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, symbol);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("delete crypto", e);
        }
    }

    private Crypto mapRow(ResultSet resultSet) throws SQLException {
        return new Crypto(
                resultSet.getString("symbol"),
                resultSet.getDouble("price"),
                resultSet.getString("blockchain")
        );
    }
}
