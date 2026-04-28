package com.myhttpserver.app.parser;

import com.myhttpserver.app.dto.HttpRequest;
import com.myhttpserver.app.dto.RequestLine;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {

    public HttpRequest parseRequest(BufferedInputStream in) throws IOException {

        ParserState state = ParserState.REQUEST_LINE;
        StringBuilder buffer = new StringBuilder();

        RequestLine requestLine = null;
        Map<String, List<String>> headers = new HashMap<>();
        String currHeaderName = null;
        byte[] body = new byte[0];

        int prev = -1;
        int curr;

        while ((curr = in.read()) != -1) {
            switch (state) {
                case REQUEST_LINE -> {
                    if (prev == '\r' && curr == '\n') { // CRLF -> signifies end of request line, move to headers
                        requestLine = parseRequestLine(buffer.toString());
                        buffer.setLength(0);
                        state = ParserState.HEADER_NAME;
                    } else if (curr == '\n') { // bare '\n' without '\r' -> reject (malformed)
                        state = ParserState.ERROR;
                    } else if (curr != '\r') { // iterate through bytes until CRLF
                        buffer.append((char) curr);
                    }
                }
                case HEADER_NAME -> {
                    if (curr == ':') {
                        currHeaderName = buffer.toString().trim().toLowerCase();
                        buffer.setLength(0);
                        state = ParserState.HEADER_VALUE;
                    } else if (prev == '\r' && curr == '\n') {
                        if (buffer.isEmpty()) {
                            state = ParserState.BODY;
                        } else {
                            state = ParserState.ERROR; // malformed
                        }
                    } else if (curr != '\r') {
                        buffer.append((char) curr);
                    }
                }
                case HEADER_VALUE -> {
                    if (prev == '\r' && curr == '\n') {
                        headers.computeIfAbsent(currHeaderName, k -> new ArrayList<>())
                                .add(buffer.toString().trim());
                        buffer.setLength(0);
                        state = ParserState.HEADER_NAME;
                    } else if (curr != '\r') {
                        buffer.append((char) curr);
                    }
                }
            }
            if (state == ParserState.BODY) {
                if (headers.containsKey("content-length")) {
                    int len = Integer.parseInt(headers.get("content-length").get(0));
                    body = in.readNBytes(len);
                }
                state = ParserState.COMPLETE;
            }
            //TODO: Need to handle malformed request errors
            if (state == ParserState.COMPLETE || state == ParserState.ERROR) break;
            prev = curr;
        }
        return new HttpRequest(requestLine, headers, body.length > 0 ? body : null);

    }

    private RequestLine parseRequestLine(String requestLine) {
        String[] parts = requestLine.split(" ");
        String method = parts[0], target = parts[1], version = parts[2];
        return new RequestLine(method, target, version);
    }
}
