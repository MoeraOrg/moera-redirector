package org.moera.redirector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

public class Main {

    private static final int MAX_THREADS = 64;

    private static final Config CONFIG = new Config();

    public static void main(String[] args) throws IOException {
        CONFIG.parseCommandLine(args);

        HttpServer server = HttpServer.create(new InetSocketAddress(CONFIG.getPort()), 0);
        server.createContext("/", new RequestHandler());
        server.setExecutor(Executors.newFixedThreadPool(MAX_THREADS));
        server.start();
    }

}
