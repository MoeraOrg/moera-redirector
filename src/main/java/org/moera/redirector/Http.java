package org.moera.redirector;

public class Http {

    public static final int OK = 200;
    public static final int TEMPORARY_REDIRECT = 307;
    public static final int BAD_REQUEST = 400;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int PAYLOAD_TOO_LARGE = 413;

    public static final String COOKIE = "cookie";
    public static final String SET_COOKIE = "set-cookie";
    public static final String USER_AGENT = "user-agent";

    public static final String COOKIE_CLIENT = "client";

}
