package com.myhttpserver.app.config;

import com.myhttpserver.app.response.HttpResponse;
import com.myhttpserver.app.router.Router;

public class AppConfig {
    public static Router buildRouter() {
        Router router = new Router();

        router.get("/hello", request ->
                HttpResponse.okText("Hello World!"));

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
