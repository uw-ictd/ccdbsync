package edu.uw.ictd;

public class AggregateUrlSettings {
  public final static int NUM_OF_SETTINGS = 4;

  private String aggUrl = null;
  private String appId = null;
  private String aggUsername = null;
  private String aggPassword = null;

  public AggregateUrlSettings() {
  }

  public AggregateUrlSettings(String aggUrl, String appId, String aggUsername, String aggPassword) {
    this.aggUrl = aggUrl;
    this.appId = appId;
    this.aggUsername = aggUsername;
    this.aggPassword = aggPassword;
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    AggregateUrlSettings that = (AggregateUrlSettings) o;

    if (aggUrl != null ? !aggUrl.equals(that.aggUrl) : that.aggUrl != null)
      return false;
    if (appId != null ? !appId.equals(that.appId) : that.appId != null)
      return false;
    if (aggUsername != null ? !aggUsername.equals(that.aggUsername) : that.aggUsername != null)
      return false;
    return aggPassword != null ? aggPassword.equals(that.aggPassword) : that.aggPassword == null;
  }

  @Override
  public int hashCode() {
    int result = aggUrl != null ? aggUrl.hashCode() : 0;
    result = 31 * result + (appId != null ? appId.hashCode() : 0);
    result = 31 * result + (aggUsername != null ? aggUsername.hashCode() : 0);
    result = 31 * result + (aggPassword != null ? aggPassword.hashCode() : 0);
    return result;
  }
}
