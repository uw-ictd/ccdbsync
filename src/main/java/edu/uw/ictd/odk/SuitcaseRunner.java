package edu.uw.ictd.odk;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SuitcaseRunner {

  private static final String SUITCASE_DOWNLOAD_ENDED = "Suitcase download ended!";
  private static final String SUITCASE_STARTED = "Suitcase started!";
  private static final String SUITCASE_STREAM_S = "Suitcase stream: %s";
  private static final String SUITCASE_RETURNED_WITH_RET_CODE_S = "Suitcase returned with retCode: %s";
  private static final String SUITCASE_RETURNED_WITH_EXIT_CODE_S = "Suitcase returned with exitCode: %s";
  private static final String SUITCASE_COMMAND_FAILED = "Suitcase Command failed";
  private static String dataVersion = "2";
  private static String javaCmd = "java";
  private static String jarOption = "-jar";
  private static String depsFolder = "dependencies";
  private static String suitcaseJarFile = "suitcase-2.1.6-jar-with-dependencies.jar";
  private static String cloudEndpointUrlOption = "-cloudEndpointUrl";
  private static String odkTablesStr = "odktables";
  private static String downloadOption = "-download";
  private static String pathOption = "-path";
  private static String extraMetadataOption = "-e";
  private static String appIdOption = "-appId";
  private static String tableIdOption = "-tableId";
  private static String dataVersionOption = "-dataVersion";
  private static String userNameOption = "-username";
  private static String pwdOption = "-password";

  public static void runSuitcaseDownloadCommand(String aggUrl, String csvFilePath, String appId,
      String tableId, String aggUsername, String aggPassword, Logger logger) throws Exception {

    ArrayList<String> commands = new ArrayList<>();
    addCommonParametersToSuitcaseCommand(commands, aggUrl, appId, aggUsername, aggPassword);

    commands.add(downloadOption);
    commands.add(extraMetadataOption);
    commands.add(pathOption);
    commands.add(csvFilePath);
    commands.add(tableIdOption);
    commands.add(tableId);

    runSuitcaseCommand(commands, logger);
    logger.info(SUITCASE_DOWNLOAD_ENDED);
  }


  private static void runSuitcaseCommand(ArrayList<String> commands, Logger logger) throws Exception {
    // Create the ProcessBuilder
    ProcessBuilder pb = new ProcessBuilder(commands);
    pb.redirectErrorStream(true);

    // Start the process
    Process proc = pb.start();
    logger.info(SUITCASE_STARTED);

    BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));

    String line;
    while ((line = input.readLine()) != null) {
      logger.info(String.format(SUITCASE_STREAM_S, line));
    }

    boolean retCode = proc.waitFor(120, TimeUnit.SECONDS);
    int exitCode = proc.exitValue();
    logger.info(String.format(SUITCASE_RETURNED_WITH_RET_CODE_S, retCode));
    logger.info(String.format(SUITCASE_RETURNED_WITH_EXIT_CODE_S, exitCode));

    if (input != null) {
      input.close();
    }

    // Clean-up
    proc.destroy();


    if (exitCode != 0) {
      throw new RuntimeException(SUITCASE_COMMAND_FAILED);
    }
  }

  private static void addCommonParametersToSuitcaseCommand(ArrayList<String> cmds, String aggUrl,
      String appId, String aggUsername, String aggPassword) throws IOException {

    String jarFilePath = depsFolder + File.separator + suitcaseJarFile;
    ensureSuitcaseJarExists(jarFilePath);

    cmds.add(javaCmd);
    cmds.add(jarOption);
    cmds.add(jarFilePath);

    cmds.add(cloudEndpointUrlOption);
    String aggUrlForSuitcase = aggUrl;
    String odkTblStr = odkTablesStr;
    if (aggUrlForSuitcase.endsWith(odkTblStr)) {
      aggUrlForSuitcase = aggUrlForSuitcase.substring(0,
          aggUrlForSuitcase.length() - odkTblStr.length());
    }
    cmds.add(aggUrlForSuitcase);

    cmds.add(appIdOption);
    cmds.add(appId);

    cmds.add(dataVersionOption);
    cmds.add(dataVersion);

    if (aggUsername != null) {
      cmds.add(userNameOption);
      cmds.add(aggUsername);
    }

    if (aggPassword != null) {
      cmds.add(pwdOption);
      cmds.add(aggPassword);
    }
  }

  private static void ensureSuitcaseJarExists(String jarFilePath) throws IOException {
    File depDir = new File(depsFolder);
    if (!depDir.exists() || !depDir.isDirectory()) {
      depDir.mkdir();
    }

    File jarFile = new File(jarFilePath);
    if (!jarFile.exists()) {
      InputStream inStream = SuitcaseRunner.class.getClassLoader().getResourceAsStream(jarFilePath);
      FileUtils.copyInputStreamToFile(inStream, jarFile);
    }
  }
}

