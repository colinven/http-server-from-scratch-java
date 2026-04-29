package com.myhttpserver.app.parser;

import com.myhttpserver.app.exception.HttpParseException;
import com.myhttpserver.app.request.HttpMethod;
import com.myhttpserver.app.request.HttpRequest;
import com.myhttpserver.app.request.RequestLine;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {

    public HttpRequest parseRequest(BufferedInputStream in) throws IOException, HttpParseException {

        ParserState state = ParserState.REQUEST_LINE;
        StringBuilder buffer = new StringBuilder();

        RequestLine requestLine = null;
        Map<String, List<String>> headers = new HashMap<>();
        String currHeaderName = null;
        byte[] body = new byte[0];

        int prev = -1;
        int curr;
        String errorMsg = null;

        while ((curr = in.read()) != -1) {
            boolean isCRLF = prev == '\r' && curr == '\n';
            boolean isBareLF = curr == '\n';

            switch (state) {
                case REQUEST_LINE -> {
                    if (isCRLF) { // CRLF -> signifies end of request line, move to headers
                        requestLine = parseRequestLine(buffer.toString());
                        buffer.setLength(0);
                        state = ParserState.HEADER_NAME;
                    } else if (isBareLF) { // bare '\n' without '\r' -> reject (malformed)
                        state = ParserState.ERROR;
                        errorMsg = "Malformed request line";
                    } else if (curr != '\r') { // iterate through bytes until CRLF
                        buffer.append((char) curr);
                    }
                }
                case HEADER_NAME -> {
                    if (curr == ':') {
                        currHeaderName = buffer.toString().trim().toLowerCase();
                        buffer.setLength(0);
                        state = ParserState.HEADER_VALUE;
                    } else if (isCRLF) {
                        if (buffer.isEmpty()) {
                            state = ParserState.BODY;
                        } else {
                            state = ParserState.ERROR;
                            errorMsg = "Malformed request header name";
                        }
                    } else if (curr != '\r') {
                        buffer.append((char) curr);
                    }
                }
                case HEADER_VALUE -> {
                    if (isCRLF) {
                        headers.computeIfAbsent(currHeaderName, k -> new ArrayList<>())
                                .add(buffer.toString().trim());
                        buffer.setLength(0);
                        state = ParserState.HEADER_NAME;
                    } else if (isBareLF) {
                        state = ParserState.ERROR;
                        errorMsg = "Malformed request header value";
                    } else if (curr != '\r') {
                        buffer.append((char) curr);

                    }
                }
                case ERROR -> {
                    throw new HttpParseException(400, errorMsg);
                }
            }
            if (state == ParserState.BODY) {
                // if 'content-length' is present, read bytes equal to content-length value
                if (headers.containsKey("content-length")) {
                    int len = Integer.parseInt(headers.get("content-length").get(0));
                    body = in.readNBytes(len);
                }
                state = ParserState.COMPLETE;
            }
            if (state == ParserState.COMPLETE) break;
            prev = curr;
        }
        // 'Host' header required. throw if absent
        if (!headers.containsKey("host")) {
            throw new HttpParseException(400, "Missing host");
        }
        return new HttpRequest(requestLine, headers, body.length > 0 ? body : null);

    }

    private RequestLine parseRequestLine(String requestLine) throws HttpParseException{
        String[] parts = requestLine.split(" ");
        String method = parts[0], target = parts[1], version = parts[2];
        // throw if http version is not 1.1
        if (!version.equals("HTTP/1.1")) {
            throw new HttpParseException(505, version + " Not Supported");
        }
        // throw if http method is invalid
        HttpMethod httpMethod;
        try {
            httpMethod = HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new HttpParseException(400, "Invalid HTTP method: " + method);
        }
        return new RequestLine(httpMethod, target, version);
    }
}
