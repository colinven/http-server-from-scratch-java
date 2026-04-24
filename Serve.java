import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.*;

public class Serve {
    public static void main(String[] args) throws IOException {
        int port = 5500;
        Path file = Path.of("index.html");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {
            byte[] body = Files.readAllBytes(file);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (var os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.start();
        String url = "http://localhost:" + port;
        System.out.println("Serving " + url);
    }
}