package com.myhttpserver.app.config;

import com.myhttpserver.app.handlers.HelloHandler;
import com.myhttpserver.app.response.HttpResponse;
import com.myhttpserver.app.router.Router;

public class AppConfig {
    public static Router buildRouter() {
        Router router = new Router();
        HelloHandler helloHandler = new HelloHandler();

        // params: name={name} default="World" -> "Hello, {name}!"
        router.get("/hello", helloHandler::getGreeting);

        // params: name={name} default="World" -> "Goodbye, {name}!"
        router.get("/hello/goodbye", helloHandler::getGoodbye);

        router.get("/slow", request -> {
            Thread.sleep(2000);
            return HttpResponse.okText("Slow response");
        });

        router.get("/explode", request -> {
            throw new RuntimeException("boom");
        });

        return router;
    }
}
