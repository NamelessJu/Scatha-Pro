package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.util.TimeUtil;
import org.jetbrains.annotations.NotNull;

import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

public enum DateFormat
{
    SYSTEM("Automatic", DateTimeFormatter.ofPattern(
        DateTimeFormatterBuilder.getLocalizedDateTimePattern(
            FormatStyle.SHORT,
            null,
            IsoChronology.INSTANCE,
            Locale.getDefault()
        ).replaceAll("\\byy\\b", "yyyy"), // force 4 digit year
        Locale.getDefault()
    )),
    DD_MM_YYYY("DD.MM.YYYY", DateTimeFormatter.ofPattern("dd.MM.uuuu")),
    MM_DD_YYYY("M/D/YYYY", DateTimeFormatter.ofPattern("M/d/uuuu"));
    
    private final String displayName;
    private final DateTimeFormatter formatter;
    
    DateFormat(@NotNull String displayName, @NotNull DateTimeFormatter formatter)
    {
        this.displayName = displayName;
        this.formatter = formatter;
    }
    
    public @NotNull String format(@NotNull TemporalAccessor date)
    {
        return formatter.format(date);
    }
    
    public @NotNull String format(long epochMilliseconds)
    {
        return format(TimeUtil.epochMillisToLocalDateTime(epochMilliseconds));
    }
    
    @Override
    public String toString()
    {
        return displayName;
    }
}
