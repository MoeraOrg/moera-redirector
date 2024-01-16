package org.moera.redirector;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RequestHandler implements HttpHandler {

    private static final String DEFAULT_CLIENT = "web.moera.org";

    public static final int TEMPORARY_REDIRECT = 307;
    public static final int NOT_FOUND = 404;

    public static final String USER_AGENT = "user-agent";

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        URI uri = httpExchange.getRequestURI();

        if (!uri.getPath().startsWith("/@")) {
            httpExchange.sendResponseHeaders(NOT_FOUND, 0);
            httpExchange.close();
        }

        URI target;
        try {
            if (isModernBrowser(httpExchange.getRequestHeaders().getFirst(USER_AGENT))) {
                target = new URI("https", DEFAULT_CLIENT, uri.getPath(), uri.getQuery(), uri.getFragment());
            } else {
                UniversalLocation uni = new UniversalLocation(uri);
                target = new URI(uni.getScheme(), uni.getAuthority(), uni.getPath(), uni.getQuery(), uni.getFragment());
            }
            httpExchange.getResponseHeaders().add("Location", target.toASCIIString());
            httpExchange.sendResponseHeaders(TEMPORARY_REDIRECT, 0);
        } catch (URISyntaxException e) {
            httpExchange.sendResponseHeaders(NOT_FOUND, 0);
        }
        httpExchange.close();
    }

    private boolean isModernBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return false;
        }

        if (userAgent.contains("Firefox")) {
            return true;
        } else if (userAgent.contains("Opera")) {
            return false;
        } else if (userAgent.contains("Googlebot")) {
            return false;
        } else if (userAgent.contains("PetalBot")) {
            return false;
        } else if (userAgent.contains("Chrome")) {
            if (userAgent.contains("YaBrowser")) {
                return true;
            } else if (userAgent.contains("Brave")) {
                return true;
            } else if (userAgent.contains("Vivaldi")) {
                return false;
            } else if (userAgent.contains("Edge")) {
                return true;
            } else {
                return true;
            }
        } else if (userAgent.contains("Safari")) {
            return true;
        } else if (userAgent.contains("MSIE")) {
            return false;
        } else if (userAgent.contains("Dolphin")) {
            return false;
        }

        return false;
    }

}
