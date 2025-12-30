package org.moera.redirector;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.moera.lib.UniversalLocation;

public class LocationHandler implements HttpHandler {

    private static final String DEFAULT_CLIENT = "web.moera.org";

    private final NamingCache namingCache;

    public LocationHandler() {
        namingCache = new NamingCache();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        URI uri = httpExchange.getRequestURI();

        if (!uri.getPath().startsWith("/@")) {
            httpExchange.sendResponseHeaders(Http.NOT_FOUND, 0);
            httpExchange.close();
        }

        try {
            UniversalLocation uni = new UniversalLocation(uri.toString());
            URI target;
            boolean modernBrowser = isModernBrowser(httpExchange.getRequestHeaders().getFirst(Http.USER_AGENT));
            boolean staticContent = isStaticContent(uni.getPath());
            if (!staticContent && modernBrowser) {
                if (uni.getNodeName() != null) {
                    NodeUrl root = namingCache.getFast(uni.getNodeName()).orElse(new NodeUrl(null));
                    if (root.getUrl() != null) {
                        uni.setSchemeAndAuthority(new URI(root.getUrl()));
                    }
                }
                String client = getUserClient(httpExchange.getRequestHeaders().getFirst(Http.COOKIE));
                client = client != null ? client : DEFAULT_CLIENT;
                target = new URI("https", client, uni.getLocation(), uni.getQuery(), uni.getFragment());
            } else {
                if (uni.getNodeName() != null) {
                    NodeUrl root = uni.getAuthority() != null
                        ? namingCache.getFast(uni.getNodeName()).orElse(new NodeUrl(null))
                        : namingCache.get(uni.getNodeName());
                    if (root.getUrl() != null) {
                        uni.setSchemeAndAuthority(new URI(root.getUrl()));
                    }
                }
                if (uni.getAuthority() == null) {
                    httpExchange.sendResponseHeaders(Http.NOT_FOUND, 0);
                    httpExchange.close();
                }
                target = new URI(
                    uni.getScheme(), uni.getAuthority(), "/moera" + uni.getPath(), uni.getQuery(), uni.getFragment()
                );
            }
            httpExchange.getResponseHeaders().add("Location", target.toASCIIString());
            httpExchange.sendResponseHeaders(Http.TEMPORARY_REDIRECT, 0);
        } catch (URISyntaxException e) {
            httpExchange.sendResponseHeaders(Http.NOT_FOUND, 0);
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
        } else if (userAgent.contains("bingbot")) {
            return false;
        } else if (userAgent.contains("yandex.com/bots")) {
            return false;
        } else if (userAgent.contains("PetalBot")) {
            return false;
        } else if (userAgent.contains("SemrushBot")) {
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

    private boolean isStaticContent(String path) {
        return path.startsWith("/media/");
    }

    private String getUserClient(String cookieHeader) {
        if (cookieHeader == null) {
            return null;
        }
        String[] cookies = cookieHeader.split("; *");
        for (String cookie : cookies) {
            if (cookie.startsWith(Http.COOKIE_CLIENT + "=")) {
                return cookie.substring(Http.COOKIE_CLIENT.length() + 1);
            }
        }
        return null;
    }

}
