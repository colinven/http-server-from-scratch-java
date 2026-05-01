package com.myhttpserver.app.server;

import com.myhttpserver.app.io.HttpConnectionHandler;
import com.myhttpserver.app.parser.RequestParser;
import com.myhttpserver.app.router.Router;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HttpServerIntegrationTest {

    private HttpServer server;
    private Thread serverThread;
    private static final int PORT = 9090;

    @BeforeEach
    public void setup() throws Exception {
        RequestParser parser = new RequestParser();
        Router router = new Router();
        // register routes here
        var handler = new HttpConnectionHandler(parser, router);
        server = new HttpServer(handler);

        serverThread = new Thread(() -> {
            server.start(PORT);
        });
        serverThread.start();
        Thread.sleep(100);
    }

    @AfterEach
    public void teardown() throws Exception {
        server.shutdown();
        serverThread.join(5000);
    }

    private HttpClient client() {
        return HttpClient.newHttpClient();
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + PORT + path))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    @Test
    void concurrentClientsDoNotCorruptEachOthersResponses() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<String> response1 = new AtomicReference<>();
        AtomicReference<String> response2 = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        Thread thread1 = new Thread(() -> {
            try {
                response1.set(get("/hello").body());
            } catch (Exception e) {
                error.set(e);
            } finally {
                latch.countDown();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                response2.set(get("/hello").body());
            } catch (Exception e) {
                error.set(e);
            } finally {
                latch.countDown();
            }
        });

        thread1.start();
        thread2.start();
        latch.await(5, TimeUnit.SECONDS);

        assertThat(error.get()).isNull();
        assertThat(response1.get()).isEqualTo("Hello World!\n");
        assertThat(response2.get()).isEqualTo("Hello World!\n");
    }
    @Test
    void slowHandlerDoesNotBlockFastHandlerInConcurrentRequests() throws Exception {

        CountDownLatch latch = new CountDownLatch(2);
        AtomicLong fastResponseTime = new AtomicLong();
        AtomicLong slowResponseTime = new AtomicLong();

        Thread slowThread = new Thread(() -> {
            try {
                long start = System.currentTimeMillis();
                get("/slow");
                slowResponseTime.set(System.currentTimeMillis() - start);
            } catch (Exception e) {}
            finally {
                latch.countDown();
            }
        });

        Thread fastThread = new Thread(() -> {
            try {
                Thread.sleep(50);
                long start = System.currentTimeMillis();
                get("/fast");
                fastResponseTime.set(System.currentTimeMillis() - start);
            } catch (Exception e) {}
            finally {
                latch.countDown();
            }
        });
        slowThread.start();
        fastThread.start();
        latch.await(5, TimeUnit.SECONDS);

        assertThat(fastResponseTime.get()).isLessThan(500);
    }
    @Test
    void threadPoolDoesNotLeakUnderLoad() throws Exception {
        for (int i = 0; i < 1000; i++) {
            get("/hello");
        }
        Thread.sleep(500);
        int poolSize = server.getActiveThreadCount();

        assertThat(poolSize).isLessThanOrEqualTo(50);
    }
    @Test
    void gracefulShutdownDrainsInFlightResponse() throws Exception {
        AtomicReference<String> responseBody = new AtomicReference<>();
        CountDownLatch requestStarted = new CountDownLatch(1);
        CountDownLatch requestDone = new CountDownLatch(1);

        Thread requestThread = new Thread(() -> {
            try {
                requestStarted.countDown();
                responseBody.set(get("/hello").body());
            } catch (Exception e) { }
            finally {
                requestDone.countDown();
            }
        });
        requestThread.start();
        requestStarted.await();

        Thread.sleep(400);
        server.shutdown();

        boolean completed = requestDone.await(10, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(responseBody.get()).isNotNull();
    }
}
