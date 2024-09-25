package com.namelessju.scathapro.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
    
}
