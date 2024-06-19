package com.namelessju.scathapro.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

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
    
    public static String getDateString()
    {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) + "-" + (today.get(Calendar.MONTH) + 1) + "-" + today.get(Calendar.DAY_OF_MONTH);
    }
    
    public static String formatDate(LocalDate date)
    {
        return date.format(dateFormat);
    }
    
    public static LocalDate today()
    {
        // return LocalDate.parse("2024-06-21");
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
