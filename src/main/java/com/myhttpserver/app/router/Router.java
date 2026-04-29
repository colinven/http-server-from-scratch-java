package com.myhttpserver.app.router;


import com.myhttpserver.app.request.HttpRequest;
import com.myhttpserver.app.response.HttpResponse;

public class Router {

    public HttpResponse dispatch(HttpRequest request) {
        return HttpResponse.okText("Hello World!\n");
    }

}
