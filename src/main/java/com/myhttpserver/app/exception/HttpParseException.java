package com.myhttpserver.app.exception;

public class HttpParseException extends Exception {
    private final int statusCode;

    public HttpParseException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
