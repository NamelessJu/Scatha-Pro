package com.namelessju.scathapro;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config {

    private static File getFile() {
    	return SaveManager.getModFile("config.cfg");
    }
    
    private Configuration config;
    
    public enum Key {
    	
        // Overlay
        overlay("overlay", "enabled", true),
        overlayX("overlay", "x", -1D), overlayY("overlay", "y", -1D),
        overlayScale("overlay", "scale", 1D),
        
        // Sounds
        soundsVolume("sounds", "volume", 1D),
        muteOtherSounds("sounds", "muteOtherSounds", false),
        
        // Alerts
        mode("alerts", "mode", ""),
        customModeSubmode("alerts", "customModeSubmode", ""),
        
        wormAlert("alerts", "worms", true),
        scathaAlert("alerts", "scathas", true),
        wormPreAlert("alerts", "wormsPre", true),
        petAlert("alerts", "pet", true),
        wallAlert("alerts", "wall", true),
        goblinAlert("alerts", "goblin", true),
        
        // Other
        showRotationAngles("other", "showRotationAngles", false),
        chatCopy("other", "chatCopy", false),
        automaticBackups("other", "automaticBackups", true),
        automaticUpdateChecks("other", "automaticUpdateChecks", true),
        automaticStatsParsing("other", "automaticStatsParsing", true),
        wormSpawnTimer("other", "wormSpawnTimer", false),
        
        devMode("other", "devMode", false);
        
        
        private String category;
        private String key;
        private Object defaultValue;
        
        Key(String category, String key, Object defaultValue) {
            this.category = category;
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }
    
    public void init() {
        loadFile();
    	
    	// convert old integer-based mode ID to new string-based mode ID
    	// (only if no string ID is saved yet)
    	if (getString(Key.mode).isEmpty()) {
    		
    		int oldMode = config.get("other", "mode", -1).getInt();
    		if (oldMode >= 0) {
    			
    			String newMode;
    			
    			switch (oldMode) {
	    			case 1:
	    				newMode = "meme";
	    				break;
	    			case 2:
	    				newMode = "anime";
	    				break;
	    			case 0:
	    			default:
	    				newMode = "normal";
    			}
    			
    			set(Key.mode, newMode);
    			config.save();
    		}
    	}
    	
    	
    	if (config.get(Key.soundsVolume.category, Key.soundsVolume.key, -1D).getDouble() < 0D) {
    		
    		double oldVolume = config.get("alerts", "volume", -1D).getDouble();
    		if (oldVolume >= 0) {
    			set(Key.soundsVolume, oldVolume);
    			config.save();
    		}
    	}
    }
    
    public void loadFile() {
        config = new Configuration(getFile());
        config.load();
    }
    
    
    private Property getIntProperty(Key key) {
        return config.get(key.category, key.key, (Integer) key.getDefaultValue());
    }
    private Property getDoubleProperty(Key key) {
        return config.get(key.category, key.key, (Double) key.getDefaultValue());
    }
    private Property getStringProperty(Key key) {
        return config.get(key.category, key.key, (String) key.getDefaultValue());
    }
    private Property getBooleanProperty(Key key) {
        return config.get(key.category, key.key, (Boolean) key.getDefaultValue());
    }
    
    
    public int getInt(Key key) {
        return getIntProperty(key).getInt();
    }
    public double getDouble(Key key) {
        return getDoubleProperty(key).getDouble();
    }
    public String getString(Key key) {
        return getStringProperty(key).getString();
    }
    public boolean getBoolean(Key key) {
        return getBooleanProperty(key).getBoolean();
    }
    
    public void set(Key key, int value) {
        getIntProperty(key).set(value);
    }
    public void set(Key key, double value) {
        getDoubleProperty(key).set(value);
    }
    public void set(Key key, String value) {
        getStringProperty(key).set(value);
    }
    public void set(Key key, boolean value) {
        getBooleanProperty(key).set(value);
    }
    
    public void reset(Key key) {
        if (key.getDefaultValue() instanceof Integer) set(key, (Integer) key.getDefaultValue());
        else if (key.getDefaultValue() instanceof Double) set(key, (Double) key.getDefaultValue());
        else if (key.getDefaultValue() instanceof String) set(key, (String) key.getDefaultValue());
        else if (key.getDefaultValue() instanceof Boolean) set(key, (Boolean) key.getDefaultValue());
    }
    
    public void save() {
        config.save();
    }
}
