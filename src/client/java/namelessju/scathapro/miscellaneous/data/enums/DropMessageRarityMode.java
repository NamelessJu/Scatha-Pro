package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.miscellaneous.data.IDisplayable;
import org.jspecify.annotations.NonNull;

public enum DropMessageRarityMode implements IDisplayable
{
    SUFFIX("Suffix", false, true),
    PREFIX("Prefix", true, true),
    PREFIX_NO_BRACKETS("Prefix (No Brackets)", true, false);

    public final boolean hasBrackets;
    public final boolean isPrefix;

    private final @NonNull String displayName;

    DropMessageRarityMode(@NonNull String displayName, boolean isPrefix, boolean hasBrackets)
    {
        this.displayName = displayName;
        this.isPrefix = isPrefix;
        this.hasBrackets = hasBrackets;
    }

    @Override
    public @NonNull String getDisplayName()
    {
        return displayName;
    }
}