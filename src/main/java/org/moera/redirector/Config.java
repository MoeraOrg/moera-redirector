package org.moera.redirector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Config {

    private static String PROGRAM_NAME = Config.class.getPackage().getImplementationTitle();
    private static String PROGRAM_VERSION = Config.class.getPackage().getImplementationVersion();

    private int port = 8080;

    static {
        if (PROGRAM_NAME == null) {
            PROGRAM_NAME = "moera-redirector";
        }
        if (PROGRAM_VERSION == null) {
            PROGRAM_VERSION = "SNAPSHOT";
        }
    }

    public int getPort() {
        return port;
    }

    public void parseCommandLine(String[] args) {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption("h", "help", false, "print this message");
        options.addOption("V", "version", false, "print the version information and exit");
        options.addOption(Option.builder()
                .option("P")
                .longOpt("port")
                .desc("HTTP port to listen on")
                .hasArg()
                .argName("PORT")
                .build());

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp(PROGRAM_NAME, options);
                System.exit(0);
            }
            if (line.hasOption("version")) {
                System.out.println(PROGRAM_NAME + " " + PROGRAM_VERSION);
                System.exit(0);
            }
            if (line.hasOption("port")) {
                try {
                    port = Integer.parseInt(line.getOptionValue("port"));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid value of the option 'port'");
                    System.exit(1);
                }
            }
        } catch (ParseException | NumberFormatException e) {
            System.err.println("Error parsing command-line arguments: " + e.getMessage());
            System.exit(1);
        }
    }

}
