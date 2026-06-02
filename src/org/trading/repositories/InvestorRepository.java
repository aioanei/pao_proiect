package org.trading.repositories;

import org.trading.models.Investor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class InvestorRepository extends AbstractJdbcRepository<Investor, String> {
    private static InvestorRepository instance;

    private InvestorRepository() {
    }

    public static synchronized InvestorRepository getInstance() {
        if (instance == null) {
            instance = new InvestorRepository();
        }
        return instance;
    }

    @Override
    public void create(Investor investor) {
        String sql = "INSERT INTO investors(username, balance) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, investor.getUsername());
            statement.setDouble(2, investor.getBalance());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("create investor", e);
        }
    }

    @Override
    public Optional<Investor> findById(String username) {
        String sql = "SELECT username, balance FROM investors WHERE username = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw databaseError("read investor", e);
        }
    }

    @Override
    public List<Investor> findAll() {
        String sql = "SELECT username, balance FROM investors";
        List<Investor> investors = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                investors.add(mapRow(resultSet));
            }
            return investors;
        } catch (SQLException e) {
            throw databaseError("read all investors", e);
        }
    }

    @Override
    public void update(Investor investor) {
        String sql = "UPDATE investors SET balance = ? WHERE username = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, investor.getBalance());
            statement.setString(2, investor.getUsername());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("update investor", e);
        }
    }

    @Override
    public void delete(String username) {
        String sql = "DELETE FROM investors WHERE username = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw databaseError("delete investor", e);
        }
    }

    private Investor mapRow(ResultSet resultSet) throws SQLException {
        return new Investor(
                resultSet.getString("username"),
                resultSet.getDouble("balance")
        );
    }
}
