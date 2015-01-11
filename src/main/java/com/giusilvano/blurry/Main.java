package com.giusilvano.blurry;

import org.apache.commons.cli.*;

import java.io.IOException;

public class Main {

  // For images of 5 or more megapixels, analyzing the 1% of the pixels is enough
  private static final float DEFAULT_SAMPLE_COVERAGE = 0.01f;

  /**
   * Prints in the standard out an explanation of how to use this app.
   * @param options
   */
  private static void printUsageInfo(Options options) {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("blurry [options] [target dir]", options);
  }

  public static void main(String[] args) throws IOException {
    System.out.println(
            "______  _                            _ \n" +
            "| ___ \\| |                          | |\n" +
            "| |_/ /| | _   _  _ __  _ __  _   _ | |\n" +
            "| ___ \\| || | | || '__|| '__|| | | || |\n" +
            "| |_/ /| || |_| || |   | |   | |_| ||_|\n" +
            "\\____/ |_| \\__,_||_|   |_|    \\__, |(_)   by Giuseppe Silvano\n" +
            "                               __/ |   \n" +
            "                              |___/    \n");

    final CommandLineParser parser = new BasicParser();

    final Options options = new Options();
    options.addOption("c", "cur-dir", false, "process the images in the current directory");
    options.addOption("r", "restore", false, "remove from filenames the suffixes previously added by Blurry");
    options.addOption(OptionBuilder
            .hasArg()
            .withArgName("float")
            .withDescription("set the sample coverage parameter; must be >0 and <=1 (default = 0.01)")
            .withLongOpt("sample-coverage")
            .create("s"));

    final String path;
    final float sampleCoverage;

    try {
        final CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("cur-dir")) {
            path = System.getProperty("user.dir");
        } else {
            final String[] dir = cmd.getArgs();
            if (dir.length == 0) {
                printUsageInfo(options);
                return;
            } else {
                path = dir[0];
            }
        }

        if (cmd.hasOption("sample-coverage")) {
            final String sampleCoverageStr = cmd.getOptionValue("sample-coverage");
            try {
                sampleCoverage = Float.parseFloat(sampleCoverageStr);
                if (sampleCoverage <= 0 || sampleCoverage > 1) {
                    System.out.printf("Error in sample coverage parameter: value \"%f\" is not >0 or not <=1.", sampleCoverage);
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.printf("Error in sample coverage parameter: string \"%s\" is not a parsable float number.", sampleCoverageStr);
                return;
            }
        } else {
            sampleCoverage = DEFAULT_SAMPLE_COVERAGE;
        }

        if (cmd.hasOption("restore")) {
            DirectoryProcessor.restoreFilenames(path);
        } else {
            DirectoryProcessor.process(path, sampleCoverage);
        }

    } catch (UnrecognizedOptionException e) {
        printUsageInfo(options);

    } catch (Exception e) {
        e.printStackTrace();
    }
  }

}
