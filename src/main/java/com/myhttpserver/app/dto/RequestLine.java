package com.myhttpserver.app.dto;

public record RequestLine(
        String method,
        String target,
        String version
) { }
