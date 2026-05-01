package com.myhttpserver.app;

import com.myhttpserver.app.config.AppConfig;
import com.myhttpserver.app.io.HttpConnectionHandler;
import com.myhttpserver.app.parser.RequestParser;
import com.myhttpserver.app.router.Router;
import com.myhttpserver.app.server.HttpServer;

public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        Router router = AppConfig.buildRouter();
        RequestParser requestParser = new RequestParser();
        HttpConnectionHandler handler = new HttpConnectionHandler(requestParser, router);
        HttpServer server = new HttpServer(handler);
        server.start(PORT);
    }
}
