package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.miscellaneous.data.IDisplayable;
import namelessju.scathapro.util.TimeUtil;
import org.jspecify.annotations.NonNull;

import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

public enum DateFormat implements IDisplayable
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

    private final @NonNull String displayName;
    private final @NonNull DateTimeFormatter formatter;

    DateFormat(@NonNull String displayName, @NonNull DateTimeFormatter formatter)
    {
        this.displayName = displayName;
        this.formatter = formatter;
    }

    public @NonNull String format(@NonNull TemporalAccessor date)
    {
        return formatter.format(date);
    }

    public @NonNull String format(long epochMilliseconds)
    {
        return format(TimeUtil.epochMillisToLocalDateTime(epochMilliseconds));
    }

    @Override
    public @NonNull String getDisplayName()
    {
        return displayName;
    }
}