package com.myhttpserver.app.io;

import com.myhttpserver.app.exception.HttpParseException;
import com.myhttpserver.app.parser.RequestParser;
import com.myhttpserver.app.request.HttpRequest;
import com.myhttpserver.app.response.HttpResponse;
import com.myhttpserver.app.router.RouteHandler;
import com.myhttpserver.app.router.Router;

import java.io.*;

public class HttpConnectionHandler implements ConnectionHandler {
    private final RequestParser parser;
    private final Router router;

    public HttpConnectionHandler(RequestParser parser, Router router) {
        this.parser = parser;
        this.router = router;
    }

    @Override
    public void handleConnection(InputStream in, OutputStream out) {
        try (
                BufferedInputStream input = new BufferedInputStream(in);
                BufferedOutputStream output = new BufferedOutputStream(out);
                ) {
            try {
                HttpRequest request = parser.parseRequest(input);
                RouteHandler handler = router.getHandler(request);
                HttpResponse response = handler == null
                        ? HttpResponse.notFound()
                        : handler.handle(request);
                response.write(output);
            } catch (HttpParseException e) {
                HttpResponse.ofString(e.getStatusCode(), e.getMessage()).write(output);
            } catch (Exception e) {
                HttpResponse.internalServerError().write(output);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
