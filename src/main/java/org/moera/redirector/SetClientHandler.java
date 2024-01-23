package org.moera.redirector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class SetClientHandler implements HttpHandler {

    public static final int MAX_LENGTH = 256;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (!httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
            httpExchange.sendResponseHeaders(Http.METHOD_NOT_ALLOWED, 0);
            httpExchange.close();
        }

        String clientDomain = new String(httpExchange.getRequestBody().readNBytes(MAX_LENGTH), StandardCharsets.UTF_8);
        if (httpExchange.getRequestBody().available() > 0) {
            httpExchange.sendResponseHeaders(Http.PAYLOAD_TOO_LARGE, 0);
            httpExchange.close();
        }
        if (!clientDomain.matches("[a-zA-Z0-9-.]+")) {
            httpExchange.sendResponseHeaders(Http.BAD_REQUEST, 0);
            httpExchange.close();
        }

        httpExchange.getResponseHeaders().add(Http.SET_COOKIE,
                String.format(
                        "%s=%s; HttpOnly; Max-Age=%d; Path=/; Secure",
                        Http.COOKIE_CLIENT,
                        clientDomain,
                        100L * 365 * 24 * 60 * 60
                )
        );
        httpExchange.sendResponseHeaders(Http.OK, 0);
        httpExchange.close();
    }

}
