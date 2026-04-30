package com.myhttpserver.app.parser;

import com.myhttpserver.app.exception.HttpParseException;
import com.myhttpserver.app.request.HttpMethod;
import com.myhttpserver.app.request.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class RequestParserTest {

    private RequestParser parser;

    @BeforeEach
    public void setUp() {
        parser = new RequestParser();
    }

    private BufferedInputStream streamOf(String raw) {
        return new BufferedInputStream(
                new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8))
        );
    }

    @Test
    public void givenValidGetRequest_whenParseRequest_thenParsesRequestSuccessfully() throws Exception {
        String raw = "GET /hello HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: */*\r\n" +
                "\r\n";

        HttpRequest request = parser.parseRequest(streamOf(raw));

        assertThat(request.requestLine().method()).isEqualTo(HttpMethod.GET);
        assertThat(request.requestLine().target()).isEqualTo("/hello");
        assertThat(request.requestLine().version()).isEqualTo("HTTP/1.1");
        assertThat(request.headers().containsKey("host"));
        assertThat(request.headers().get("host").get(0)).isEqualTo("localhost:8080");
        assertThat(request.body()).isNull();
    }
    @Test
    public void givenRequestWithIncorrectHttpVersion_whenParseRequest_thenThrowHttpParseExceptionWith505() {
        String raw = "GET /hello HTTP/1.0\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: */*\r\n" +
                "\r\n";

        assertThatThrownBy(() -> parser.parseRequest(streamOf(raw)))
                .isInstanceOf(HttpParseException.class)
                .satisfies(e -> {
                    assertThat(((HttpParseException)e).getStatusCode()).isEqualTo(505);
                });
    }
    @Test
    public void givenRequestWithNoHostHeader_whenParseRequest_thenThrowHttpParseExceptionWith400() {
        String raw = "GET /hello HTTP/1.1\r\n" +
                "Accept: */*\r\n" +
                "\r\n";

        assertThatThrownBy(() -> parser.parseRequest(streamOf(raw)))
                .isInstanceOf(HttpParseException.class)
                .satisfies(e -> {
                    assertThat(((HttpParseException)e).getStatusCode()).isEqualTo(400);
                });
    }
    @Test
    public void givenRequestWithMultipleHeaders_whenParseRequest_thenAllHeadersPresent() throws Exception {
        String raw = "GET /hello HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: */*\r\n" +
                "AnotherHeader: val\r\n" +
                "AnotherOne: val\r\n" +
                "YetAnotherHeader: val\r\n" +
                "\r\n";

        HttpRequest request = parser.parseRequest(streamOf(raw));

        assertThat(request.headers().containsKey("host"));
        assertThat(request.headers().get("host").get(0)).isEqualTo("localhost:8080");
        assertThat(request.headers().containsKey("accept"));
        assertThat(request.headers().get("accept").get(0)).isEqualTo("*/*");
        assertThat(request.headers().containsKey("anotherheader"));
        assertThat(request.headers().get("anotherheader").get(0)).isEqualTo("val");
        assertThat(request.headers().containsKey("anotherone"));
        assertThat(request.headers().get("anotherone").get(0)).isEqualTo("val");
        assertThat(request.headers().containsKey("yetanotherheader"));
        assertThat(request.headers().get("yetanotherheader").get(0)).isEqualTo("val");
    }
    @Test
    public void givenHeaderValueWithExtraWhiteSpace_whenParseRequest_thenValueIsTrimmed() throws Exception {
        String raw = "GET /hello HTTP/1.1\r\n" +
                "Host:    localhost:8080   \r\n" +
                "\r\n";

        HttpRequest request = parser.parseRequest(streamOf(raw));

        assertThat(request.headers().get("host").get(0)).isEqualTo("localhost:8080");
    }
    @Test
    public void givenRequestWithBareLF_whenParseRequest_thenThrowHttpParseExceptionWith400() {
        String raw = "GET /hello HTTP/1.0\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        assertThatThrownBy(() -> parser.parseRequest(streamOf(raw)))
                .isInstanceOf(HttpParseException.class)
                .satisfies(e -> {
                    assertThat(((HttpParseException)e).getStatusCode()).isEqualTo(400);
                });

    }
    @Test
    public void givenRequestWithMalformedRequestLine_whenParseRequest_thenThrowHttpParseExceptionWith400() {
        String raw = "GET \r\n\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        assertThatThrownBy(() -> parser.parseRequest(streamOf(raw)))
                .isInstanceOf(HttpParseException.class)
                .satisfies(e -> {
                    assertThat(((HttpParseException)e).getStatusCode()).isEqualTo(400);
                });
    }
}
