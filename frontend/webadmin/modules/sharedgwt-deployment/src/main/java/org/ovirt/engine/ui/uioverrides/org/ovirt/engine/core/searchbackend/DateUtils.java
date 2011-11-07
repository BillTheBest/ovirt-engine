
package org.ovirt.engine.core.searchbackend;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.DateFormatCompat;

import java.util.Calendar;
import java.util.Date;

class DateUtils {
    
    static Date parse(String str) {        
        try {
            return DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).parse(str);
        } catch (IllegalArgumentException iae) {            
        }
        return null;
    }
    
    public static DateTimeFormat getFormat(int dateStyle) {
        switch (dateStyle)
        {
            case DateFormatCompat.FULL:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_FULL);               
            case DateFormatCompat.LONG:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG);
            case DateFormatCompat.SHORT:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);
            default:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);
        }
    }

    public static DateTimeFormat getFormat(int dateStyle, int timeStyle) {
        switch (timeStyle)
        {
            case DateFormatCompat.FULL:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL);               
            case DateFormatCompat.LONG:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_LONG);
            case DateFormatCompat.SHORT:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
            default:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
        }
    }
    
    public static String getDayOfWeek(int addDays) {
        Date date = new Date();
        return DateTime.getDayOfTheWeekAsString(date.getDay()).toUpperCase();
    }
}
