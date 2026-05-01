package com.myhttpserver.app.server;

import com.myhttpserver.app.exception.HttpParseException;
import com.myhttpserver.app.io.ConnectionHandler;
import com.myhttpserver.app.request.HttpRequest;
import com.myhttpserver.app.parser.RequestParser;
import com.myhttpserver.app.response.HttpResponse;
import com.myhttpserver.app.router.Router;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpServer {

    private volatile boolean running = false;
    private final ExecutorService pool = Executors.newFixedThreadPool(50);
    private final ConnectionHandler connectionHandler;
    private ServerSocket serverSocket;

    public HttpServer(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public void start(final int port) {
        registerShutdownHook();
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("(http-server): Listening on port " + port);
            while (running) {
                try {
                    Socket client = serverSocket.accept(); // block main thread - await client connection
                    pool.submit(() -> handle(client)); // upon client connection, create new worker thread
                } catch (SocketException e) {
                    if (!running) {
                        System.err.println("(http-server): Server stopped");
                    } else {
                        throw e;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handle(Socket client) {
        try {
            connectionHandler.handleConnection(client.getInputStream(), client.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        pool.shutdown();
        try {
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n(http-server): Shutting down...");
            try {
                shutdown();
            } catch (IOException e) {
                System.err.println("(http-server): Error shutting down: " + e.getMessage());
            }
        }));
    }

    public int getActiveThreadCount() {
        return ((ThreadPoolExecutor) pool).getPoolSize();
    }

}
