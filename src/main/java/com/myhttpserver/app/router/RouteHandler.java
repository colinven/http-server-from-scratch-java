package com.myhttpserver.app.router;

import com.myhttpserver.app.request.HttpRequest;
import com.myhttpserver.app.response.HttpResponse;

@FunctionalInterface
public interface RouteHandler {
    HttpResponse handle(HttpRequest request) throws Exception;
}
