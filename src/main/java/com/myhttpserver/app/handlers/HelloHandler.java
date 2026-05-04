package com.myhttpserver.app.handlers;

import com.myhttpserver.app.request.HttpRequest;
import com.myhttpserver.app.response.HttpResponse;

import java.util.Map;

public class HelloHandler {

    public HttpResponse getGreeting(HttpRequest request) {
        Map<String, String> params = request.queryParams();
        String name = (params.get("name") != null) ? params.get("name") : "World";
        return HttpResponse.okText("Hello, " + name + "!");
    }

    public HttpResponse getGoodbye(HttpRequest request) {
        Map<String, String> params = request.queryParams();
        String name = (params.get("name") != null) ? params.get("name") : "World";
        return HttpResponse.okText("Goodbye, " + name + "!");
    }
}
