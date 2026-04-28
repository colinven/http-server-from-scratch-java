package com.myhttpserver.app.dto;

import java.util.List;
import java.util.Map;

public record HttpRequest(
        RequestLine line,
        Map<String, List<String>> headers,
        byte[] body
) { }
