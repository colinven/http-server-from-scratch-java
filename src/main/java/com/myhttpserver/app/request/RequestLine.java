package com.myhttpserver.app.request;

public record RequestLine(
        HttpMethod method,
        String target,
        String version
) { }
