package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.util.TimeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;

public enum TimeFormat
{
    SYSTEM("Automatic", DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT), null),
    FORMAT_24_HOURS("24 Hours", DateTimeFormatter.ofPattern("HH:mm"), null),
    FORMAT_12_HOURS("12 Hours (AM/PM)", DateTimeFormatter.ofPattern("h:mm"), DateTimeFormatter.ofPattern(" a"));
    
    private final DateTimeFormatter SECONDS_FORMATTER = DateTimeFormatter.ofPattern(":ss");
    
    private final @NotNull String displayName;
    private final @NotNull DateTimeFormatter hoursMinutesFormatter;
    private final @Nullable DateTimeFormatter suffixFormatter;
    
    TimeFormat(@NotNull String displayName, @NotNull DateTimeFormatter hoursMinutesFormatter, @Nullable DateTimeFormatter suffixFormatter)
    {
        this.displayName = displayName;
        this.hoursMinutesFormatter = hoursMinutesFormatter;
        this.suffixFormatter = suffixFormatter;
    }
    
    public @NotNull String format(@NotNull TemporalAccessor time, boolean withSeconds)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hoursMinutesFormatter.format(time));
        if (withSeconds) stringBuilder.append(SECONDS_FORMATTER.format(time));
        if (suffixFormatter != null) stringBuilder.append(suffixFormatter.format(time));
        return stringBuilder.toString();
    }
    
    public @NotNull String formatHoursMinutes(@NotNull TemporalAccessor time)
    {
        return hoursMinutesFormatter.format(time);
    }
    
    public @NotNull String formatSeconds(@NotNull TemporalAccessor time)
    {
        return SECONDS_FORMATTER.format(time);
    }
    
    public @Nullable String formatSuffix(@NotNull TemporalAccessor time)
    {
        if (suffixFormatter == null) return null;
        return suffixFormatter.format(time);
    }
    
    public @NotNull String format(long epochMilliseconds, boolean withSeconds)
    {
        return format(TimeUtil.epochMillisToLocalDateTime(epochMilliseconds), withSeconds);
    }
    
    @Override
    public String toString()
    {
        return displayName;
    }
}
