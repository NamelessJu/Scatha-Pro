package namelessju.scathapro.util;

import namelessju.scathapro.ScathaPro;
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
        return config.accessibility.dateFormat.get().format(epochMilliseconds)
                + ", " +
                config.accessibility.timeFormat.get().format(epochMilliseconds, false);
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

    private static boolean isAprilFools = false;
    private static boolean isHalloween = false;
    private static boolean isChristmas = false;

    public static void updateSeasons()
    {
        isAprilFools = isDate(4, 1);
        isHalloween = isDateBetween(10, 25, 10, 31);
        isChristmas = isDateBetween(12, 24, 12, 31);
    }

    public static boolean isAprilFools()
    {
        return getSeason(isAprilFools, Config.DeveloperSettings.SeasonOverride.APRIL_FOOLS);
    }

    public static boolean isHalloween()
    {
        return getSeason(isHalloween, Config.DeveloperSettings.SeasonOverride.HALLOWEEN);
    }

    public static boolean isChristmas()
    {
        return getSeason(isChristmas, Config.DeveloperSettings.SeasonOverride.CHRISTMAS);
    }

    private static boolean getSeason(boolean original, Config.DeveloperSettings.SeasonOverride allowedOverride)
    {
        Config.DeveloperSettings.SeasonOverride override = ScathaPro.instance().config.dev.seasonOverride.get();
        if (override != null) return override == allowedOverride;
        return original;
    }
}