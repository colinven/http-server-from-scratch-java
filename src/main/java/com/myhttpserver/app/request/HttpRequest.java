package com.myhttpserver.app.request;

import java.util.List;
import java.util.Map;

public record HttpRequest(
        RequestLine requestLine,
        Map<String, List<String>> headers,
        byte[] body
) { }
