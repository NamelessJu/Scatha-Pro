package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.miscellaneous.data.IDisplayable;
import namelessju.scathapro.util.TimeUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Locale;
import java.util.function.Supplier;

public enum TimeFormat implements IDisplayable
{
    SYSTEM("Automatic", new SupplierTimeFormatterContainer(
        () -> getSystemTimeFormat().formatterContainer.hoursMinutesFormatter(),
        () -> getSystemTimeFormat().formatterContainer.suffixFormatter()
    )),
    FORMAT_24_HOURS("24 Hours", new SimpleTimeFormatterContainer(
        DateTimeFormatter.ofPattern("HH:mm"), null
    )),
    FORMAT_12_HOURS("12 Hours", new SimpleTimeFormatterContainer(
        DateTimeFormatter.ofPattern("h:mm"), DateTimeFormatter.ofPattern(" a")
    ));

    private static final boolean SYSTEM_IS_12_HOURS_FORMAT
        = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, Locale.getDefault())
            .format(new Calendar.Builder().setFields(Calendar.HOUR_OF_DAY, 23, Calendar.MINUTE, 0).build().getTime())
            .contains("11");
    private static final DateTimeFormatter SECONDS_FORMATTER = DateTimeFormatter.ofPattern(":ss");

    private static @NonNull TimeFormat getSystemTimeFormat()
    {
        return SYSTEM_IS_12_HOURS_FORMAT ? FORMAT_12_HOURS : FORMAT_24_HOURS;
    }

    private final @NonNull String displayName;
    private final @NonNull TimeFormatterContainer formatterContainer;

    TimeFormat(@NonNull String displayName, @NonNull TimeFormatterContainer formatterContainer)
    {
        this.displayName = displayName;
        this.formatterContainer = formatterContainer;
    }

    public @NonNull String format(@NonNull TemporalAccessor time, boolean withSeconds)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(formatterContainer.hoursMinutesFormatter().format(time));
        if (withSeconds) stringBuilder.append(SECONDS_FORMATTER.format(time));
        DateTimeFormatter suffixFormatter = formatterContainer.suffixFormatter();
        if (suffixFormatter != null) stringBuilder.append(suffixFormatter.format(time));
        return stringBuilder.toString();
    }

    public @NonNull String formatHoursMinutes(@NonNull TemporalAccessor time)
    {
        return formatterContainer.hoursMinutesFormatter().format(time);
    }

    public @NonNull String formatSeconds(@NonNull TemporalAccessor time)
    {
        return SECONDS_FORMATTER.format(time);
    }

    public @Nullable String formatSuffix(@NonNull TemporalAccessor time)
    {
        DateTimeFormatter suffixFormatter = formatterContainer.suffixFormatter();
        if (suffixFormatter == null) return null;
        return suffixFormatter.format(time);
    }

    public @NonNull String format(long epochMilliseconds, boolean withSeconds)
    {
        return format(TimeUtil.epochMillisToLocalDateTime(epochMilliseconds), withSeconds);
    }

    @Override
    public @NonNull String getDisplayName()
    {
        return displayName;
    }

    private interface TimeFormatterContainer
    {
        @NonNull DateTimeFormatter hoursMinutesFormatter();
        @Nullable DateTimeFormatter suffixFormatter();
    }

    private record SimpleTimeFormatterContainer(
            @NonNull DateTimeFormatter hoursMinutesFormatter,
            @Nullable DateTimeFormatter suffixFormatter
        ) implements TimeFormatterContainer {}

    private record SupplierTimeFormatterContainer(
            @NonNull Supplier<@NonNull DateTimeFormatter> hoursMinutesFormatterSupplier,
            @NonNull Supplier<@Nullable DateTimeFormatter> suffixFormatterSupplier
        ) implements TimeFormatterContainer
    {
        @Override
        public @NonNull DateTimeFormatter hoursMinutesFormatter()
        {
            return hoursMinutesFormatterSupplier.get();
        }

        @Override
        public @Nullable DateTimeFormatter suffixFormatter()
        {
            return suffixFormatterSupplier.get();
        }
    }
}