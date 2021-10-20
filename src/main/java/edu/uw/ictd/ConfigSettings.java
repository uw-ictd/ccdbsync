package edu.uw.ictd;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConfigSettings {
  private static final int CONFIG_SIZE = 11;
  private static final int NUM_OF_AGG_URL_LINE = 6;

  // Variables for Config File Reader
  private String dbUsername = null;
  private String dbPassword = null;
  private String dbUrl = null;
  private String csvDir = null;
  private String defaultTZ = null;
  private String logTZ = null;
  private Integer numOfAggUrls = 0;
  private ArrayList<AggregateUrlSettings> aggSettings = new ArrayList<>();

  public ConfigSettings() {
  }

  public ConfigSettings(String filePath) throws FileNotFoundException {
    // Get the properties from the config file
    Scanner lineScan = new Scanner(new File(filePath));
    List<String> userConfig = new ArrayList<String>();

    while (lineScan.hasNext()) {
      userConfig.add(lineScan.next());
    }

    lineScan.close();

    if (userConfig.size() >= CONFIG_SIZE) {
      this.dbUrl = userConfig.get(0);
      this.dbUsername = userConfig.get(1);
      this.dbPassword = userConfig.get(2);
      this.csvDir = userConfig.get(3);
      this.defaultTZ = userConfig.get(4);
      this.logTZ = userConfig.get(5);
      this.numOfAggUrls = Integer.parseInt(userConfig.get(NUM_OF_AGG_URL_LINE));

      for (int i = 0; i < this.numOfAggUrls; i++) {
        this.aggSettings.add(new AggregateUrlSettings(
            userConfig.get(NUM_OF_AGG_URL_LINE + AggregateUrlSettings.NUM_OF_SETTINGS * i + 1),
            userConfig.get(NUM_OF_AGG_URL_LINE + AggregateUrlSettings.NUM_OF_SETTINGS * i + 2),
            userConfig.get(NUM_OF_AGG_URL_LINE + AggregateUrlSettings.NUM_OF_SETTINGS * i + 3),
            userConfig.get(NUM_OF_AGG_URL_LINE + AggregateUrlSettings.NUM_OF_SETTINGS * i + 4)));
      }

    } else {
      throw new IllegalArgumentException(
          "Config file " + filePath + " does not have the right number of parameters");
    }
  }

  public ConfigSettings(String dbUsername, String dbPassword, String dbUrl, String csvDir,
      String defaultTZ, String logTZ, Integer numOfAggUrls,
      ArrayList<AggregateUrlSettings> aggSettings) {
    this.dbUsername = dbUsername;
    this.dbPassword = dbPassword;
    this.dbUrl = dbUrl;
    this.csvDir = csvDir;
    this.defaultTZ = defaultTZ;
    this.logTZ = logTZ;
    this.numOfAggUrls = numOfAggUrls;
    this.aggSettings = aggSettings;
  }

  public static int getConfigSize() {
    return CONFIG_SIZE;
  }

  public static int getNumOfAggUrlLine() {
    return NUM_OF_AGG_URL_LINE;
  }

  public String getDbUsername() {
    return dbUsername;
  }

  public void setDbUsername(String dbUsername) {
    this.dbUsername = dbUsername;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public void setDbPassword(String dbPassword) {
    this.dbPassword = dbPassword;
  }

  public String getDbUrl() {
    return dbUrl;
  }

  public void setDbUrl(String dbUrl) {
    this.dbUrl = dbUrl;
  }

  public String getCsvDir() {
    return csvDir;
  }

  public void setCsvDir(String csvDir) {
    this.csvDir = csvDir;
  }

  public String getDefaultTZ() {
    return defaultTZ;
  }

  public void setDefaultTZ(String defaultTZ) {
    this.defaultTZ = defaultTZ;
  }

  public String getLogTZ() {
    return logTZ;
  }

  public void setLogTZ(String logTZ) {
    this.logTZ = logTZ;
  }

  public Integer getNumOfAggUrls() {
    return numOfAggUrls;
  }

  public void setNumOfAggUrls(Integer numOfAggUrls) {
    this.numOfAggUrls = numOfAggUrls;
  }

  public ArrayList<AggregateUrlSettings> getAggSettings() {
    return aggSettings;
  }

  public void setAggSettings(ArrayList<AggregateUrlSettings> aggSettings) {
    this.aggSettings = aggSettings;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ConfigSettings that = (ConfigSettings) o;

    if (dbUsername != null ? !dbUsername.equals(that.dbUsername) : that.dbUsername != null)
      return false;
    if (dbPassword != null ? !dbPassword.equals(that.dbPassword) : that.dbPassword != null)
      return false;
    if (dbUrl != null ? !dbUrl.equals(that.dbUrl) : that.dbUrl != null)
      return false;
    if (csvDir != null ? !csvDir.equals(that.csvDir) : that.csvDir != null)
      return false;
    if (defaultTZ != null ? !defaultTZ.equals(that.defaultTZ) : that.defaultTZ != null)
      return false;
    if (logTZ != null ? !logTZ.equals(that.logTZ) : that.logTZ != null)
      return false;
    if (numOfAggUrls != null ? !numOfAggUrls.equals(that.numOfAggUrls) : that.numOfAggUrls != null)
      return false;
    return aggSettings != null ? aggSettings.equals(that.aggSettings) : that.aggSettings == null;
  }

  @Override
  public int hashCode() {
    int result = dbUsername != null ? dbUsername.hashCode() : 0;
    result = 31 * result + (dbPassword != null ? dbPassword.hashCode() : 0);
    result = 31 * result + (dbUrl != null ? dbUrl.hashCode() : 0);
    result = 31 * result + (csvDir != null ? csvDir.hashCode() : 0);
    result = 31 * result + (defaultTZ != null ? defaultTZ.hashCode() : 0);
    result = 31 * result + (logTZ != null ? logTZ.hashCode() : 0);
    result = 31 * result + (numOfAggUrls != null ? numOfAggUrls.hashCode() : 0);
    result = 31 * result + (aggSettings != null ? aggSettings.hashCode() : 0);
    return result;
  }
}
