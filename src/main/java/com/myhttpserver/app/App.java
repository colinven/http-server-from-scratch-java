package com.myhttpserver.app;

import com.myhttpserver.app.io.HttpConnectionHandler;
import com.myhttpserver.app.parser.RequestParser;
import com.myhttpserver.app.response.HttpResponse;
import com.myhttpserver.app.router.Router;
import com.myhttpserver.app.server.HttpServer;

import java.io.IOException;

public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        Router router = new Router();

        router.get("/hello", request ->
                HttpResponse.okText("Hello World!"));

        router.get("/slow", request -> {
            Thread.sleep(2000);
            return HttpResponse.okText("Slow response");
        });

        router.get("/explode", request -> {
            throw new RuntimeException("boom");
        });

        RequestParser requestParser = new RequestParser();
        HttpConnectionHandler handler = new HttpConnectionHandler(requestParser, router);
        HttpServer server = new HttpServer(handler);
        server.start(PORT);
    }
}
