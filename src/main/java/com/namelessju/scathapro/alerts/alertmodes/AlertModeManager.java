package com.namelessju.scathapro.alerts.alertmodes;
import java.util.LinkedHashMap;

import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertMode;
import com.namelessju.scathapro.managers.Config;

public class AlertModeManager
{
    private final Config config;
    
    private LinkedHashMap<String, AlertMode> modes = new LinkedHashMap<String, AlertMode>();
    
    public AlertModeManager(Config config)
    {
        this.config = config;
        
        registerMode(AlertMode.DEFAULT_MODE);
        registerMode(new PresetAlertMode("meme", "Meme"));
        registerMode(new PresetAlertMode("anime", "Anime"));
        registerMode(new CustomAlertMode("custom", "Custom"));
    }
    
    public void registerMode(AlertMode mode)
    {
        modes.put(mode.id, mode);
    }
    
    public AlertMode[] getAllModes()
    {
        return (AlertMode[]) modes.values().toArray(new AlertMode[0]);
    }
    
    public AlertMode getModeByID(String id)
    {
        return modes.get(id);
    }
    
    public AlertMode getCurrentMode()
    {
        String currentModeId = config.getString(Config.Key.mode);
        if (currentModeId.isEmpty()) return AlertMode.DEFAULT_MODE;
        AlertMode mode = getModeByID(currentModeId);
        return mode != null ? mode : AlertMode.DEFAULT_MODE;
    }
}
