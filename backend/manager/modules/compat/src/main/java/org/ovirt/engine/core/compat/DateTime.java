package org.ovirt.engine.core.compat;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTime extends Date {

    // public static Date Now = new DateTime();
    // public static DateTime Now2 = new DateTime();

    private static final String dayNames[] = new DateFormatSymbols().getWeekdays();

    public DateTime(int year, int month, int date) {
        this(new Date(year, month, date));
    }

    public DateTime() {
        this(getMinValue());
    }

    public DateTime(Date argvalue) {
        super(argvalue.getTime());
    }

    public DateTime(long millis) {
        super(millis);
    }

    /**
     * This method resets the datetime object to 00:00:00.000 on the same date
     * @return
     */
    public DateTime resetToMidnight() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        return new DateTime(cal.getTime());
    }

    public DayOfWeek getDayOfWeek() {
        return DayOfWeek.forValue(this.getDay());
    }

    public long getTicks() {
        return this.getTime();
    }

    public long getTotalMilliseconds() {
        return this.getTime();
    }

    public DateTime(int i) {
        super(i);
    }

    public String toString(String formatString) {
        // c# compatibility
        boolean compat = false;
        if (formatString.equals("yyyy-MM-ddTHH:mm:ss")) {
            formatString = "yyyy-MM-ddHH:mm:ss";
            compat = true;
        }
        SimpleDateFormat fmt = new SimpleDateFormat(formatString);
        String returnedValue = fmt.format(this);
        if (compat) {
            returnedValue = returnedValue.substring(0, 10) + "T" + returnedValue.substring(10);
        }
        return returnedValue;
    }

    public String toString(DateFormat dateFormat) {
        return dateFormat.format(this);
    }

    public Date AddSeconds(int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.SECOND, i);
        return cal.getTime();
    }

    public DateTime AddDays(int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.DATE, i);
        return new DateTime(cal.getTime());
    }

    public TimeSpan Subtract(Date date) {
        long span = this.getTime() - date.getTime();
        return new TimeSpan(span);
    }

    public DateTime AddSeconds(double secsSinceEpoch) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.SECOND, (int) secsSinceEpoch);
        return new DateTime(cal.getTime());
    }

    /**
     * The Min Date in java
     *
     * @return - a date representing - Thu Jan 01 00:00:00 IST 1970
     */
    public static Date getMinValue() {
        GregorianCalendar javaEpochTime = new GregorianCalendar();
        javaEpochTime.clear();
        return javaEpochTime.getTime();
    }

    public static DateTime getNow() {
        return new DateTime(System.currentTimeMillis());
    }

    public Date AddHours(int graceTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.HOUR, graceTime);
        return new DateTime(cal.getTime());
    }

    public DateTime AddMilliseconds(int interval) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.MILLISECOND, interval);
        return new DateTime(cal.getTime());
    }

    public Date AddMinutes(int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.MINUTE, i);
        return new DateTime(cal.getTime());
    }

    public Date AddMinutes(double i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.MINUTE, (int) i);
        return new DateTime(cal.getTime());
    }

    public static DateTime getUtcNow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, -cal.get(Calendar.DST_OFFSET) - cal.get(Calendar.ZONE_OFFSET));
        return new DateTime(cal.getTime());

    }

    public static String getDayOfTheWeekAsString(int dayOfTheWeek) {
        return dayNames[dayOfTheWeek];
    }

    public static boolean TryParseExact(String value, String string, Object dateTimeFormat, DateTimeStyles none,
            RefObject<Date> tempRefObject) {
        // TODO Auto-generated method stub
        throw new NotImplementedException(); // juicommon
    }

    public static Date ParseExact(String entity, String string, CultureInfo currentCulture) {
        // TODO Auto-generated method stub
        throw new NotImplementedException(); // juicommon
    }
}
