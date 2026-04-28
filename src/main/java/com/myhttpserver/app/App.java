package com.myhttpserver.app;

import com.myhttpserver.app.server.HttpServer;

public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        new HttpServer().start(PORT);
    }
}
