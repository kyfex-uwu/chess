import dataAccess.DataAccessException;
import dataAccess.DatabaseManager;
import server.Server;

public class Main {
    public static void main(String[] args) throws DataAccessException {
        var server = new Server();
        var port = server.run(6000);
        System.out.println("Started test HTTP server on " + port);

        DatabaseManager.createDatabase();
    }
}