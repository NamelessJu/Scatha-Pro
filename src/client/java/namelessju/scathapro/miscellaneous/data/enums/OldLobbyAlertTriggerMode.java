package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.files.Config;
import namelessju.scathapro.miscellaneous.data.IDisplayable;
import org.jspecify.annotations.NonNull;

public enum OldLobbyAlertTriggerMode implements IDisplayable
{
    ALWAYS("Always"), ON_JOIN("Lobby Join"), ON_NEW_DAY("New Day Start");

    private final @NonNull String displayName;

    OldLobbyAlertTriggerMode(@NonNull String displayName)
    {
        this.displayName = displayName;
    }

    @Override
    public @NonNull String getDisplayName()
    {
        return displayName;
    }

    public boolean isActive(Config config)
    {
        OldLobbyAlertTriggerMode triggerMode = config.alerts.oldLobbyAlertTriggerMode.get();
        return triggerMode == ALWAYS || triggerMode == this;
    }
}