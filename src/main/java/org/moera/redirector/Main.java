package org.moera.redirector;

public class Main {

    private static final Config CONFIG = new Config();

    public static void main(String[] args) {
        CONFIG.parseCommandLine(args);
    }

}
