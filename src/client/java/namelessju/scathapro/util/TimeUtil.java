package namelessju.scathapro.util;

import namelessju.scathapro.files.Config;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

public class TimeUtil
{
    /**
     * Returns the current time in milliseconds since midnight, January 1, 1970 UTC
     */
    public static long getEpochMilliseconds()
    {
        return System.currentTimeMillis();
    }
    
    public static @NonNull LocalDateTime epochMillisToLocalDateTime(long epochMilliseconds)
    {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilliseconds), ZoneId.systemDefault());
    }
    
    public static boolean getAnimationState(int trueDurationMs, int falseDurationMs)
    {
        return getEpochMilliseconds() % (trueDurationMs + falseDurationMs) < trueDurationMs;
    }
    
    public static @NonNull LocalDate today()
    {
        return LocalDate.now();
    }
    
    public static short getCurrentYear()
    {
        return (short) today().getYear();
    }
    
    public static @NonNull String formatDateTime(@NonNull Config config, long epochMilliseconds)
    {
        return config.accessibility.timeFormat.get().format(epochMilliseconds, false)
            + " " +
            config.accessibility.dateFormat.get().format(epochMilliseconds);
    }
    
    /**
     * Generates a time string of seconds, minutes and hours, showing only the applicable fields<br>
     * E.g.: 1h 2m 3s
     * @param countDown When set to true rounds the seconds up instead of down
     */
    public static @NonNull String getHMSTimeString(long milliseconds, boolean countDown)
    {
        int seconds;
        {
            double secondsD = milliseconds / 1000D;
            seconds = (int) (countDown ? Math.ceil(secondsD) : secondsD);
        }
        int minutes = 0;
        if (seconds >= 60)
        {
            minutes = seconds / 60;
            seconds %= 60;
        }
        int hours = 0;
        if (minutes >= 60)
        {
            hours = minutes / 60;
            minutes %= 60;
        }
        return (hours >= 1 ? hours + "h " : "") + (minutes >= 1 ? minutes + "m " : "") + seconds + "s";
    }
    
    public static boolean isDate(int month, int day)
    {
        return isDateBetween(month, day, month, day);
    }
    
    /**
     * Both <code>from<code> and <code>to</code> dates are inclusive
     */
    public static boolean isDateBetween(int monthFrom, int dayFrom, int monthTo, int dayTo)
    {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        return (
            ((monthFrom == month && dayFrom <= day) || monthFrom < month)
            &&
            ((month == monthTo && day <= dayTo) || month < monthTo)
        );
    }
    
    public static boolean isAprilFools()
    {
        return isDate(4, 1);
    }
}
