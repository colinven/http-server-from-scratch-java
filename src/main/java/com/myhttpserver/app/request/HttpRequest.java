package com.myhttpserver.app.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record HttpRequest(
        RequestLine requestLine,
        Map<String, List<String>> headers,
        byte[] body
) {
    public Map<String, String> queryParams() {

        String target = requestLine.target();
        int index = target.indexOf('?');
        if (index == -1) return Map.of();
        Map<String, String> params = new HashMap<>();
        String[] pairs = target.substring(index + 1).split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                params.put(kv[0], kv[1]);
            }
        }
        return params;
    }
}
