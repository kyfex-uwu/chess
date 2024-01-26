package dataAccess;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLSafeConsumer<T> {
    void accept(T resultSet) throws SQLException, DataAccessException;
}
