package org.moera.redirector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetClientHandler implements HttpHandler {

    public static final int MAX_LENGTH = 1024;

    private static final Logger log = LoggerFactory.getLogger(SetClientHandler.class);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        switch (httpExchange.getRequestMethod().toUpperCase()) {
            case "GET":
                get(httpExchange);
                break;
            case "POST":
                post(httpExchange);
                break;
            default:
                httpExchange.sendResponseHeaders(Http.METHOD_NOT_ALLOWED, 0);
                httpExchange.close();
        }
    }

    private void get(HttpExchange httpExchange) throws IOException {
        Map<String, String> params = Http.parseQuery(httpExchange.getRequestURI().getQuery());
        if (!params.containsKey("client")) {
            httpExchange.sendResponseHeaders(Http.BAD_REQUEST, 0);
            httpExchange.close();
            return;
        }

        InputStream input = getClass().getClassLoader().getResourceAsStream("set-client.html");
        if (input == null) {
            log.error("Cannot load HTML template");
            httpExchange.sendResponseHeaders(Http.INTERNAL_SERVER_ERROR, 0);
            httpExchange.close();
            return;
        }

        String html = readAll(input);
        html = html.replace("{{client}}", params.get("client"));
        String returnHref = params.get("return");
        if (returnHref == null || returnHref.isEmpty()) {
            returnHref = "#";
        }
        html = html.replace("{{return}}", returnHref);

        httpExchange.sendResponseHeaders(Http.OK, 0);

        Writer writer = new OutputStreamWriter(httpExchange.getResponseBody());
        writer.write(html);
        writer.flush();
        writer.close();

        httpExchange.close();
    }

    private static String readAll(InputStream input) throws IOException {
        StringBuilder buf = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            buf.append(line);
        }
        return buf.toString();
    }

    private void post(HttpExchange httpExchange) throws IOException {
        Map<String, String> params = Http.parseQuery(
                new String(httpExchange.getRequestBody().readNBytes(MAX_LENGTH), StandardCharsets.UTF_8)
        );
        if (httpExchange.getRequestBody().available() > 0) {
            httpExchange.sendResponseHeaders(Http.PAYLOAD_TOO_LARGE, 0);
            httpExchange.close();
        }

        String clientDomain = params.get("client");
        if (clientDomain == null || !clientDomain.matches("[a-zA-Z0-9-.]+")) {
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

        String returnHref = params.get("return");
        if (returnHref != null && !returnHref.isEmpty()) {
            httpExchange.getResponseHeaders().add("Location", returnHref);
            httpExchange.sendResponseHeaders(Http.SEE_OTHER, 0);
        } else {
            httpExchange.sendResponseHeaders(Http.OK, 0);
        }
        httpExchange.close();
    }

}
