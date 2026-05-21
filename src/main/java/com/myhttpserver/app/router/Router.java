package com.myhttpserver.app.router;


import com.myhttpserver.app.request.HttpMethod;
import com.myhttpserver.app.request.HttpRequest;
import com.myhttpserver.app.response.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<RouteKey, RouteHandler> routes = new HashMap<>();

    public void get(String path, RouteHandler handler) {
        routes.put(new RouteKey(HttpMethod.GET, path), handler);
    }
    public void post(String path, RouteHandler handler) {
        routes.put(new RouteKey(HttpMethod.POST, path), handler);
    }
    public void put(String path, RouteHandler handler) {
        routes.put(new RouteKey(HttpMethod.PUT, path), handler);
    }
    public void delete(String path, RouteHandler handler) {
        routes.put(new RouteKey(HttpMethod.DELETE, path), handler);
    }

    public RouteHandler getHandler(HttpRequest request) {
        HttpMethod method = request.requestLine().method();
        String target = request.requestLine().target();
        String path = target.contains("?") ? target.substring(0, target.indexOf("?")) : target;

        return routes.get(new RouteKey(method, path));
    }

}
