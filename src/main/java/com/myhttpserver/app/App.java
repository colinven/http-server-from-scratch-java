package com.myhttpserver.app;

import com.myhttpserver.app.io.HttpConnectionHandler;
import com.myhttpserver.app.parser.RequestParser;
import com.myhttpserver.app.router.Router;
import com.myhttpserver.app.server.HttpServer;

public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        new HttpServer(new HttpConnectionHandler(new RequestParser(), new Router())).start(PORT);
    }
}
