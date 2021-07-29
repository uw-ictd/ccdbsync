package edu.uw.ictd;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConfigSettings {
  private static final int CONFIG_SIZE = 10;

  // Variables for Config File Reader
  private String aggUrl = null;
  private String appId = null;
  private String aggUsername = null;
  private String aggPassword = null;
  private String dbUsername = null;
  private String dbPassword = null;
  private String dbUrl = null;
  private String csvDir = null;
  private String defaultTZ = null;
  private String logTZ = null;

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

    if (userConfig.size() == CONFIG_SIZE) {
      this.dbUrl = userConfig.get(0);
      this.dbUsername = userConfig.get(1);
      this.dbPassword = userConfig.get(2);
      this.aggUrl = userConfig.get(3);
      this.appId = userConfig.get(4);
      this.aggUsername = userConfig.get(5);
      this.aggPassword = userConfig.get(6);
      this.csvDir = userConfig.get(7);
      this.defaultTZ = userConfig.get(8);
      this.logTZ = userConfig.get(9);
    } else {
      throw new IllegalArgumentException(
          "Config file " + filePath + " does not have the right number of parameters");
    }
  }

  public ConfigSettings(String aggUrl, String appId, String aggUsername, String aggPassword,
      String dbUsername, String dbPassword, String dbUrl, String csvDir, String defaultTZ,
      String logTZ) {
    this.aggUrl = aggUrl;
    this.appId = appId;
    this.aggUsername = aggUsername;
    this.aggPassword = aggPassword;
    this.dbUsername = dbUsername;
    this.dbPassword = dbPassword;
    this.dbUrl = dbUrl;
    this.csvDir = csvDir;
    this.defaultTZ = defaultTZ;
    this.logTZ = logTZ;
  }

  public String getAggUrl() {
    return aggUrl;
  }

  public void setAggUrl(String aggUrl) {
    this.aggUrl = aggUrl;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getAggUsername() {
    return aggUsername;
  }

  public void setAggUsername(String aggUsername) {
    this.aggUsername = aggUsername;
  }

  public String getAggPassword() {
    return aggPassword;
  }

  public void setAggPassword(String aggPassword) {
    this.aggPassword = aggPassword;
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ConfigSettings that = (ConfigSettings) o;

    if (getAggUrl() != null ? !getAggUrl().equals(that.getAggUrl()) : that.getAggUrl() != null)
      return false;
    if (getAppId() != null ? !getAppId().equals(that.getAppId()) : that.getAppId() != null)
      return false;
    if (getAggUsername() != null ?
        !getAggUsername().equals(that.getAggUsername()) :
        that.getAggUsername() != null)
      return false;
    if (getAggPassword() != null ?
        !getAggPassword().equals(that.getAggPassword()) :
        that.getAggPassword() != null)
      return false;
    if (getDbUsername() != null ?
        !getDbUsername().equals(that.getDbUsername()) :
        that.getDbUsername() != null)
      return false;
    if (getDbPassword() != null ?
        !getDbPassword().equals(that.getDbPassword()) :
        that.getDbPassword() != null)
      return false;
    if (getDbUrl() != null ? !getDbUrl().equals(that.getDbUrl()) : that.getDbUrl() != null)
      return false;
    if (getCsvDir() != null ? !getCsvDir().equals(that.getCsvDir()) : that.getCsvDir() != null)
      return false;
    if (getDefaultTZ() != null ?
        !getDefaultTZ().equals(that.getDefaultTZ()) :
        that.getDefaultTZ() != null)
      return false;
    return getLogTZ() != null ? getLogTZ().equals(that.getLogTZ()) : that.getLogTZ() == null;
  }

  @Override
  public int hashCode() {
    int result = getAggUrl() != null ? getAggUrl().hashCode() : 0;
    result = 31 * result + (getAppId() != null ? getAppId().hashCode() : 0);
    result = 31 * result + (getAggUsername() != null ? getAggUsername().hashCode() : 0);
    result = 31 * result + (getAggPassword() != null ? getAggPassword().hashCode() : 0);
    result = 31 * result + (getDbUsername() != null ? getDbUsername().hashCode() : 0);
    result = 31 * result + (getDbPassword() != null ? getDbPassword().hashCode() : 0);
    result = 31 * result + (getDbUrl() != null ? getDbUrl().hashCode() : 0);
    result = 31 * result + (getCsvDir() != null ? getCsvDir().hashCode() : 0);
    result = 31 * result + (getDefaultTZ() != null ? getDefaultTZ().hashCode() : 0);
    result = 31 * result + (getLogTZ() != null ? getLogTZ().hashCode() : 0);
    return result;
  }
}
