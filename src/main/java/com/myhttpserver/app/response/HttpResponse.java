package com.myhttpserver.app.response;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private final int statusCode;
    private final String reasonPhrase;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpResponse(int statusCode, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.reasonPhrase = getReasonPhrase(statusCode);
        this.headers = headers;
        this.body = body;
    }

    public void write(BufferedOutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(reasonPhrase).append("\r\n");
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\r\n"));
        sb.append("\r\n");
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        if (body != null && body.length > 0) {
            out.write(body);
        }
        out.flush();
    }

    public static HttpResponse ofString(int statusCode, String body) {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        headers.put("Content-Length", String.valueOf(bodyBytes.length));
        return new HttpResponse(statusCode, headers, bodyBytes);
    }

    public static HttpResponse okText(String body) {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        headers.put("Content-Length", String.valueOf(bodyBytes.length));
        return new HttpResponse(200, headers, bodyBytes);
    }

    public static HttpResponse badRequest(String message) {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        headers.put("Content-Length", String.valueOf(body.length));
        return new HttpResponse(400, headers, body);
    }

    public static HttpResponse httpVersionNotSupported(String message) {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        headers.put("Content-Length", String.valueOf(body.length));
        return new HttpResponse(505, headers, body);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    private static String getReasonPhrase(int code) {
        return switch (code) {
            case 100 -> "Continue";
            case 101 -> "Switching Protocols";
            case 102 -> "Processing";
            case 103 -> "Early Hints";
            case 200 -> "OK";
            case 201 -> "Created";
            case 202 -> "Accepted";
            case 203 -> "Non-Authoritative Information";
            case 204 -> "No Content";
            case 205 -> "Reset Content";
            case 206 -> "Partial Content";
            case 207 -> "Multi-Status";
            case 208 -> "Already Reported";
            case 226 -> "IM Used";
            case 300 -> "Multiple Choices";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 303 -> "See Other";
            case 304 -> "Not Modified";
            case 305 -> "Use Proxy";
            case 307 -> "Temporary Redirect";
            case 308 -> "Permanent Redirect";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 402 -> "Payment Required";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 406 -> "Not Acceptable";
            case 407 -> "Proxy Authentication Required";
            case 408 -> "Request Timeout";
            case 409 -> "Conflict";
            case 410 -> "Gone";
            case 411 -> "Length Required";
            case 412 -> "Precondition Failed";
            case 413 -> "Content Too Large";
            case 414 -> "URI Too Long";
            case 415 -> "Unsupported Media Type";
            case 416 -> "Range Not Satisfiable";
            case 417 -> "Expectation Failed";
            case 421 -> "Misdirected Request";
            case 422 -> "Unprocessable Content";
            case 423 -> "Locked";
            case 424 -> "Failed Dependency";
            case 425 -> "Too Early";
            case 426 -> "Upgrade Required";
            case 428 -> "Precondition Required";
            case 429 -> "Too Many Requests";
            case 431 -> "Request Header Fields Too Large";
            case 451 -> "Unavailable For Legal Reasons";
            case 500 -> "Internal Server Error";
            case 501 -> "Not Implemented";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            case 505 -> "HTTP Version Not Supported";
            case 506 -> "Variant Also Negotiates";
            case 507 -> "Insufficient Storage";
            case 508 -> "Loop Detected";
            case 511 -> "Network Authentication Required";
            default -> "Unknown";
        };
    }
}
