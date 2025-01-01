package com.namelessju.scathapro.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import net.minecraft.client.Minecraft;

public class TimeUtil
{
    public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    
    /**
     * Returns the current time in milliseconds since midnight, January 1, 1970 UTC
     */
    public static long now()
    {
        return System.currentTimeMillis();
    }
    
    /**
     * Returns a timestamp in milliseconds that can be used for animations
     */
    public static long getAnimationTime()
    {
        return Minecraft.getSystemTime();
    }
    
    public static boolean getAnimationState(int trueDuration, int falseDuration)
    {
        return getAnimationTime() % (trueDuration + falseDuration) < trueDuration;
    }
    
    /**
     * Formats a timestamp the same way vanilla Minecraft does it
     * @param timestamp The Unix timestamp in milliseconds to format
     */
    public static String formatUnixDateTime(long timestamp)
    {
        return new SimpleDateFormat().format(new Date(timestamp));
    }
    
    public static String serializeDate(LocalDate date)
    {
        return date.format(dateFormat);
    }
    
    public static LocalDate today()
    {
        return LocalDate.now();
    }
    
    public static LocalDate parseDate(String dateString)
    {
        try
        {
            return LocalDate.parse(dateString, dateFormat);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /**
     * Ensures the number string is at least two digits long by adding a leading zero if required
     * @param number The number to format
     */
    public static String padZero(int number)
    {
        return String.format("%02d", number);
    }
    
    /**
     * Generates a time string of seconds, minutes and hours, showing only the applicable fields<br>
     * E.g.: 1h 2m 3s
     * @param countDown When set to true rounds the seconds up instead of down
     */
    public static String getHMSTimeString(long milliseconds, boolean countDown)
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
    
}
