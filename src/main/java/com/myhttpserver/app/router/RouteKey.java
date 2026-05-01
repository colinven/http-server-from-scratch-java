package com.myhttpserver.app.router;

import com.myhttpserver.app.request.HttpMethod;

public record RouteKey(HttpMethod method, String path) {
}
