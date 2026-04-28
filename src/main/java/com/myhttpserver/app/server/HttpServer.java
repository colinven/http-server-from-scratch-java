package com.myhttpserver.app.server;

import com.myhttpserver.app.dto.HttpRequest;
import com.myhttpserver.app.parser.RequestParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    ExecutorService pool = Executors.newFixedThreadPool(50);
    RequestParser parser = new RequestParser();

    public void start(final int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port: " + port);
            while (true) {
                Socket client = serverSocket.accept(); // block main thread - await client connection
                pool.submit(() -> handle(client)); // upon client connection is made, create new worker thread
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handle(Socket client) {
        try (
                client;
                BufferedInputStream in = new BufferedInputStream(client.getInputStream());
                BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
        ) {
            HttpRequest request = parser.parseRequest(in);
            String requestString = request.toString() + "\n";
            byte[] bodyBytes = requestString.getBytes(StandardCharsets.UTF_8);

            String headers = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n"
                    + "Content-Length: " + bodyBytes.length + "\r\n" + "\r\n";
            out.write(headers.getBytes(StandardCharsets.UTF_8));
            out.write(bodyBytes);
            out.flush();
        } catch (IOException e) {
        throw new RuntimeException(e);
        }
    }

}
