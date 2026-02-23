package namelessju.scathapro.miscellaneous.data.enums;

import namelessju.scathapro.files.Config;

public enum OldLobbyAlertTriggerMode
{
    ALWAYS("Always"), ON_JOIN("Lobby Join"), ON_NEW_DAY("New Day Start");
    
    private final String displayName;
    
    OldLobbyAlertTriggerMode(String displayName)
    {
        this.displayName = displayName;
    }
    
    @Override
    public String toString()
    {
        return displayName;
    }
    
    public boolean isActive(Config config)
    {
        OldLobbyAlertTriggerMode triggerMode = config.alerts.oldLobbyAlertTriggerMode.get();
        return triggerMode == ALWAYS || triggerMode == this;
    }
}
