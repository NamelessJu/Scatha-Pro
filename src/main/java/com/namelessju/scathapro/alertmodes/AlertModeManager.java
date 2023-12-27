package com.namelessju.scathapro.alertmodes;
import java.util.LinkedHashMap;

import com.namelessju.scathapro.Config;

public class AlertModeManager {
	
	public static final AlertModeManager instance = new AlertModeManager();
	
	private LinkedHashMap<String, AlertMode> modes = new LinkedHashMap<String, AlertMode>();
	
	private AlertModeManager() {
		registerMode(AlertMode.DEFAULT_MODE);
		registerMode(new CustomAlertMode("custom", "Custom"));
		registerMode(new PresetAlertMode("meme", "Meme"));
		registerMode(new PresetAlertMode("anime", "Anime"));
	}
	
	private void registerMode(AlertMode mode) {
		modes.put(mode.id, mode);
	}
	
	public static AlertMode[] getAllModes() {
		return (AlertMode[]) instance.modes.values().toArray(new AlertMode[0]);
	}
	
    public static AlertMode getModeByID(String id) {
        return instance.modes.get(id);
    }
    
    public static AlertMode getCurrentMode() {
    	String currentModeId = Config.instance.getString(Config.Key.mode);
    	if (currentModeId.isEmpty()) return AlertMode.DEFAULT_MODE;
    	AlertMode mode = getModeByID(currentModeId);
        return mode != null ? mode : AlertMode.DEFAULT_MODE;
    }
	
}
