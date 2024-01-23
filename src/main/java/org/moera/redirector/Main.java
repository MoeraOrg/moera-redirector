package org.moera.redirector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final int MAX_THREADS = 64;

    private static final Config CONFIG = new Config();

    public static void main(String[] args) throws IOException {
        CONFIG.parseCommandLine(args);

        HttpServer server = HttpServer.create(new InetSocketAddress(CONFIG.getPort()), 0);
        server.createContext("/", new LocationHandler());
        server.createContext("/set-client", new SetClientHandler());
        server.setExecutor(Executors.newFixedThreadPool(MAX_THREADS));
        server.start();

        log.info("Redirector service started at port {}", CONFIG.getPort());
    }

}
