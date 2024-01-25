package com.namelessju.scathapro.alertmodes;
import java.util.LinkedHashMap;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;

public class AlertModeManager {
	
	private LinkedHashMap<String, AlertMode> modes = new LinkedHashMap<String, AlertMode>();
	
	public AlertModeManager() {
		registerMode(AlertMode.DEFAULT_MODE);
		registerMode(new CustomAlertMode("custom", "Custom"));
		registerMode(new PresetAlertMode("meme", "Meme"));
		registerMode(new PresetAlertMode("anime", "Anime"));
	}
	
	private void registerMode(AlertMode mode) {
		modes.put(mode.id, mode);
	}
	
	public AlertMode[] getAllModes() {
		return (AlertMode[]) modes.values().toArray(new AlertMode[0]);
	}
	
    public AlertMode getModeByID(String id) {
        return modes.get(id);
    }
    
    public AlertMode getCurrentMode() {
    	String currentModeId = ScathaPro.getInstance().config.getString(Config.Key.mode);
    	if (currentModeId.isEmpty()) return AlertMode.DEFAULT_MODE;
    	AlertMode mode = getModeByID(currentModeId);
        return mode != null ? mode : AlertMode.DEFAULT_MODE;
    }
	
}
