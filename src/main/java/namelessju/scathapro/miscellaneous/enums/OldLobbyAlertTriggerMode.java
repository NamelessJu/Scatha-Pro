package namelessju.scathapro.miscellaneous.enums;

import namelessju.scathapro.managers.Config;

public enum OldLobbyAlertTriggerMode
{
    ALWAYS("Always"), ON_JOIN("Lobby Join"), ON_NEW_DAY("New Day Start");
    
    private final String displayName;
    
    private OldLobbyAlertTriggerMode(String displayName)
    {
        this.displayName = displayName;
    }
    
    @Override
    public String toString()
    {
        return displayName;
    }
    
    public static boolean isActive(Config config, OldLobbyAlertTriggerMode mode)
    {
        OldLobbyAlertTriggerMode triggerMode = config.getEnum(Config.Key.oldLobbyAlertTriggerMode, OldLobbyAlertTriggerMode.class);
        if (triggerMode == null) return true; // nothing set => default to always => whatever is being checked is active
        return triggerMode == ALWAYS || triggerMode == mode;
    }
}
