package org.moera.redirector;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class Http {

    public static final int OK = 200;
    public static final int SEE_OTHER = 303;
    public static final int TEMPORARY_REDIRECT = 307;
    public static final int BAD_REQUEST = 400;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int PAYLOAD_TOO_LARGE = 413;
    public static final int INTERNAL_SERVER_ERROR = 500;

    public static final String COOKIE = "cookie";
    public static final String SET_COOKIE = "set-cookie";
    public static final String USER_AGENT = "user-agent";

    public static final String COOKIE_CLIENT = "client";

    public static Map<String, String> parseQuery(String query) {
        if (query == null) {
            return Collections.emptyMap();
        }
        if (query.startsWith("?")) {
            query = query.substring(1);
        }
        if (query.isEmpty()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(query.split("&"))
                .map(p -> p.split("="))
                .collect(Collectors.toMap(
                        p -> p[0],
                        p -> URLDecoder.decode(p[1], StandardCharsets.UTF_8),
                        (v1, v2) -> v2
                ));
    }

}
