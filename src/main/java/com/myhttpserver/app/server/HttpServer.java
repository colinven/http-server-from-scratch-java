package com.myhttpserver.app.server;

import com.myhttpserver.app.exception.HttpParseException;
import com.myhttpserver.app.request.HttpRequest;
import com.myhttpserver.app.parser.RequestParser;
import com.myhttpserver.app.response.HttpResponse;
import com.myhttpserver.app.router.Router;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    ExecutorService pool = Executors.newFixedThreadPool(50);
    RequestParser parser = new RequestParser();
    Router router = new Router();

    public void start(final int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port: " + port);
            while (true) {
                Socket client = serverSocket.accept(); // block main thread - await client connection
                pool.submit(() -> handle(client)); // upon client connection, create new worker thread
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handle(Socket client) {
        try {
            BufferedInputStream in = new BufferedInputStream(client.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
            try {
                HttpRequest request = parser.parseRequest(in);
                HttpResponse response = router.dispatch(request);
                response.write(out);
            } catch (HttpParseException e) {
                HttpResponse.ofString(e.getStatusCode(), e.getMessage()).write(out);
            }
            in.close();
            out.close();
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
