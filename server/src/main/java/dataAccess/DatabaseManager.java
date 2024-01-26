package dataAccess;

import java.sql.*;
import java.util.Properties;
import java.util.function.Consumer;

public class DatabaseManager {
    private static final String databaseName;
    private static final String user;
    private static final String password;
    private static final String connectionUrl;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) throw new Exception("Unable to load db.properties");
                Properties props = new Properties();
                props.load(propStream);
                databaseName = props.getProperty("db.name");
                user = props.getProperty("db.user");
                password = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch(SQLException e){
            throw new RuntimeException("connection failed, "+e.getMessage());
        }catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    public static int execStatement(String statement, SQLSafeConsumer<PreparedStatement> argAdder)
            throws DataAccessException {
        var connection = getConnection();

        try {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                argAdder.accept(preparedStatement);
                return preparedStatement.executeUpdate();
            }
        }catch(SQLException e){
            throw new DataAccessException(e);
        }
    }
    public static int execStatement(String statement) throws DataAccessException {
        return execStatement(statement, r->{});
    }
    public static void execQuery(String query, SQLSafeConsumer<PreparedStatement> argAdder,
                                 SQLSafeConsumer<ResultSet> processor)
            throws DataAccessException {
        var connection = getConnection();

        try {
            try (var preparedStatement = connection.prepareStatement(query)) {
                argAdder.accept(preparedStatement);
                processor.accept(preparedStatement.executeQuery());
            }
        }catch(SQLException e){
            throw new DataAccessException(e);
        }
    }
    public static void execQuery(String query, SQLSafeConsumer<ResultSet> processor) throws DataAccessException {
        execQuery(query, r->{}, processor);
    }

    /**
     * Creates the database if it does not already exist.
     */
    public static void createDatabase() throws DataAccessException {
        execStatement("CREATE DATABASE IF NOT EXISTS " + databaseName);
        execStatement("CREATE TABLE IF NOT EXISTS "+ """
                `users` (
                  `username` VARCHAR(16) NOT NULL,
                  `password` VARCHAR(128) NOT NULL,
                  `email` VARCHAR(320) NOT NULL,
                  PRIMARY KEY (`username`),
                  UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE);""");
        execStatement("CREATE TABLE IF NOT EXISTS "+ """
                `auth` (
                 `token` VARCHAR(32) NOT NULL,
                 `username` VARCHAR(16) NOT NULL,
                 PRIMARY KEY (`token`),
                 UNIQUE INDEX `token_UNIQUE` (`token` ASC) VISIBLE);""");
        execStatement("CREATE TABLE IF NOT EXISTS "+ """
                `games` (
                 `gameID` INT NOT NULL,
                 `game` JSON NOT NULL,
                 PRIMARY KEY (`gameID`),
                 UNIQUE INDEX `gameID_UNIQUE` (`gameID` ASC) VISIBLE);""");
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DbInfo.getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     */
    public static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(connectionUrl, user, password);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
