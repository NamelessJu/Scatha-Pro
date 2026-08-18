package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.miscellaneous.data.IDisplayable;
import org.jspecify.annotations.NonNull;

public enum SecondaryWormStatsType implements IDisplayable
{
    PER_LOBBY("Lobby"),
    PER_SESSION("Game Session"),
    PER_DAY("IRL Day");

    private final @NonNull String displayName;

    SecondaryWormStatsType(@NonNull String displayName)
    {
        this.displayName = displayName;
    }

    @Override
    public @NonNull String getDisplayName()
    {
        return displayName;
    }
}