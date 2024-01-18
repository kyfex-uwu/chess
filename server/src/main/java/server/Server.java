package server;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.nio.file.Paths;

public class Server {
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        var webDir = Paths.get(Server.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1)+"web");
        Spark.externalStaticFileLocation(webDir.toString());

        // Register your endpoints and handle exceptions here.
        Spark.get("/", (request, response) -> "<html><head></head><body>hi</body></html>");

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }
}