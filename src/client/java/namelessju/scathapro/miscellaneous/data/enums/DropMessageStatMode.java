package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.miscellaneous.data.IDisplayable;
import org.jspecify.annotations.NonNull;

public enum DropMessageStatMode implements IDisplayable
{
    NUMBER_ONLY("Number"),
    SHORT_NAME("Abbreviated"),
    FULL_NAME("Full Name");

    private final @NonNull String displayName;

    DropMessageStatMode(@NonNull String displayName)
    {
        this.displayName = displayName;
    }

    @Override
    public @NonNull String getDisplayName()
    {
        return displayName;
    }
}