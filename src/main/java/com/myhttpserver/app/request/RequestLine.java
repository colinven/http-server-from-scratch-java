package com.myhttpserver.app.request;

public record RequestLine(
        String method,
        String target,
        String version
) { }
