package org.trading.repositories;

import org.trading.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractJdbcRepository<T, K> implements CrudRepository<T, K> {
    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    protected RepositoryException databaseError(String operation, SQLException exception) {
        return new RepositoryException("Eroare JDBC la operatia: " + operation, exception);
    }
}
