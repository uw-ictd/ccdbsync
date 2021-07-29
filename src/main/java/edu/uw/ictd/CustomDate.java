package edu.uw.ictd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class CustomDate {

  private Date date;
  private SimpleDateFormat sdf;
  private Calendar cal;
  private TimeZone tz;
  private static TimeZone defaultTz = TimeZone.getDefault();

  public CustomDate() {
    this.date = new Date();
    this.sdf = new SimpleDateFormat();
    this.tz = defaultTz;
    this.sdf.setTimeZone(this.tz);
    this.cal = GregorianCalendar.getInstance(this.tz);
    this.sdf.setCalendar(this.cal);
  }

  public CustomDate(long ms) {
    this.date = new Date(ms);
    this.sdf = new SimpleDateFormat();
    this.tz = defaultTz;
    this.sdf.setTimeZone(this.tz);
    this.cal = GregorianCalendar.getInstance(this.tz);
    this.sdf.setCalendar(this.cal);
  }

  public CustomDate(Date dateToUse) {
    this.date = dateToUse;
    this.sdf = new SimpleDateFormat();
    this.tz = defaultTz;
    this.sdf.setTimeZone(this.tz);
    this.cal = GregorianCalendar.getInstance(this.tz);
    this.sdf.setCalendar(this.cal);
  }

  public CustomDate(String dateFormat) throws NullPointerException, IllegalArgumentException {
    this.date = new Date();
    this.sdf = new SimpleDateFormat(dateFormat);
    this.tz = defaultTz;
    this.sdf.setTimeZone(this.tz);
    this.cal = GregorianCalendar.getInstance(this.tz);
    this.sdf.setCalendar(this.cal);
  }

  public CustomDate(String dateFormat, Date dataToUse)
      throws NullPointerException, IllegalArgumentException {
    this.date = dataToUse;
    this.sdf = new SimpleDateFormat(dateFormat);
    this.tz = defaultTz;
    this.sdf.setTimeZone(this.tz);
    this.cal = GregorianCalendar.getInstance(this.tz);
    this.sdf.setCalendar(this.cal);
  }

  public CustomDate(String dateFormat, TimeZone tzToUse)
      throws NullPointerException, IllegalArgumentException {
    this.date = new Date();
    this.sdf = new SimpleDateFormat(dateFormat);
    this.tz = tzToUse;
    this.sdf.setTimeZone(this.tz);
    this.cal = GregorianCalendar.getInstance(this.tz);
    this.sdf.setCalendar(this.cal);
  }

  public CustomDate(String dateFormat, String dateToUse)
      throws NullPointerException, IllegalArgumentException, ParseException {
    this.sdf = new SimpleDateFormat(dateFormat);
    this.tz = defaultTz;
    this.sdf.setTimeZone(this.tz);
    this.date = sdf.parse(dateToUse);
    this.cal = GregorianCalendar.getInstance(this.tz);
    this.sdf.setCalendar(this.cal);
  }

  public CustomDate(String dateFormat, String dateToUse, TimeZone tzToUse)
      throws NullPointerException, IllegalArgumentException, ParseException {
    this.sdf = new SimpleDateFormat(dateFormat);
    this.tz = tzToUse;
    this.sdf.setTimeZone(this.tz);
    this.cal = GregorianCalendar.getInstance(this.tz);
    this.sdf.setCalendar(this.cal);
    this.date = sdf.parse(dateToUse);
  }

  public CustomDate(String dateFormat, Date dateToUse, TimeZone tzToUse)
      throws NullPointerException, IllegalArgumentException, ParseException {
    this.sdf = new SimpleDateFormat(dateFormat);
    this.tz = tzToUse;
    this.sdf.setTimeZone(this.tz);
    this.cal = GregorianCalendar.getInstance(this.tz);
    this.sdf.setCalendar(this.cal);
    this.date = dateToUse;
  }

  static void setDefaultTZ(TimeZone tz) {
    if (tz != null) {
      defaultTz = tz;
    }
  }

  public String getUtilString() {
    if (this.date != null && this.sdf != null) {
      return this.sdf.format(this.date);
    }
    return null;
  }

  public Date getDate() {
    return this.date;
  }

  public long getMSTime() {
    if (this.date != null) {
      return this.date.getTime();
    }
    return 0;
  }

  public Calendar getCalendar() {
    return this.cal;
  }

}

