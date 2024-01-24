package org.moera.redirector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Config {

    private static String programName = Config.class.getPackage().getImplementationTitle();
    private static String programVersion = Config.class.getPackage().getImplementationVersion();

    private int port = 8080;

    static {
        if (programName == null) {
            programName = "moera-redirector";
        }
        if (programVersion == null) {
            programVersion = "SNAPSHOT";
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
                helpFormatter.printHelp(programName, options);
                System.exit(0);
            }
            if (line.hasOption("version")) {
                System.out.println(programName + " " + programVersion);
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
