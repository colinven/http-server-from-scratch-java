package com.myhttpserver.app.parser;

public enum ParserState {
    REQUEST_LINE,
    HEADER_NAME,
    HEADER_VALUE,
    BODY,
    COMPLETE,
    ERROR
}
