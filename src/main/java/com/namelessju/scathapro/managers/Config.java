package com.namelessju.scathapro.managers;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.util.FileUtil;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config
{
    public static enum Key
    {
        // Overlay
        overlayEnabled("overlay", "enabled", true),
        overlayX("overlay", "x", -1D),
        overlayY("overlay", "y", -1D),
        overlayScale("overlay", "scale", 1D),
        statsType("overlay", "statsType", ""),
        scathaPercentageDecimalDigits("overlay", "scathaPercentageDecimalPlaces", 1),
        overlayElementStates("overlay", "overlayElementStates", ""),
        overlayBackgroundEnabled("overlay", "backgroundEnabled", true),
        
        // Sounds
        soundsVolume("sounds", "volume", 1D),
        muteCrystalHollowsSounds("sounds", "muteCrystalHollowsSounds", false),
        
        // Alerts
        mode("alerts", "mode", ""),
        customModeSubmode("alerts", "customModeSubmode", ""),

        bedrockWallAlert("alerts", "wall", true),
        wormSpawnCooldownEndAlert("alerts", "wormSpawnCooldownEnd", false),
        wormPrespawnAlert("alerts", "wormsPre", true),
        regularWormSpawnAlert("alerts", "worms", true),
        scathaSpawnAlert("alerts", "scathas", true),
        scathaPetDropAlert("alerts", "pet", true),
        goblinSpawnAlert("alerts", "goblinSpawn", true),
        jerrySpawnAlert("alerts", "jerrySpawn", true),
        
        // Achievements
        playAchievementAlerts("achievements", "playAchievementAlerts", 1D),
        bonusAchievementsShown("achievements", "bonusAchievementsShown", false),
        
        // Other
        shortChatPrefix("other", "shortChatPrefix", false),
        showRotationAngles("other", "showRotationAngles", false),
        rotationAnglesDecimalDigits("other", "rotationAnglesDecimalPlaces", 1),
        wormSpawnTimer("other", "wormSpawnTimer", false),
        dryStreakMessage("other", "dryStreakMessage", true),
        chatCopy("other", "chatCopy", false),
        automaticBackups("other", "automaticBackups", true),
        automaticUpdateChecks("other", "automaticUpdateChecks", true),
        automaticStatsParsing("other", "automaticStatsParsing", true),
        dailyScathaFarmingStreakMessage("other", "dailyScathaFarmingStreakMessage", false),
        
        // Accessibility
        highContrastColors("accessibility", "highContrastColors", false),
        
        // Dev
        devMode("dev", "devMode", false),
        debugLogs("dev", "debugLogs", false);
        
        
        private String category;
        private String key;
        private Object defaultValue;
        
        Key(String category, String key, Object defaultValue)
        {
            this.category = category;
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public Object getDefaultValue()
        {
            return defaultValue;
        }
    }
    
    
    private Configuration config;
    
    public void init()
    {
        loadFile();
        convertOldConfigEntries();
        
        ScathaPro.getInstance().logDebug("Config loaded");
    }
    
    private void convertOldConfigEntries()
    {
        // convert old integer-based mode ID to new string-based mode ID
        if (config.get(Key.mode.category, Key.mode.key, "").getString().isEmpty())
        {
            int oldMode = config.get("other", "mode", -1).getInt();
            if (oldMode >= 0)
            {
                String newMode;
                switch (oldMode)
                {
                    case 1:
                        newMode = "meme";
                        break;
                    case 2:
                        newMode = "anime";
                        break;
                    default:
                        newMode = "normal";
                }
                
                set(Key.mode, newMode);
                config.save();
            }
        }

        // convert sounds volume key
        if (config.get(Key.soundsVolume.category, Key.soundsVolume.key, -1D).getDouble() < 0D)
        {
            double oldVolume = config.get("alerts", "volume", -1D).getDouble();
            if (oldVolume >= 0)
            {
                set(Key.soundsVolume, oldVolume);
                config.save();
            }
        }
    }
    
    public void loadFile()
    {
        config = new Configuration(FileUtil.getModFile("config.cfg"));
        config.load();
    }
    
    
    private Property getIntProperty(Key key)
    {
        return config.get(key.category, key.key, (Integer) key.getDefaultValue());
    }
    
    private Property getDoubleProperty(Key key)
    {
        return config.get(key.category, key.key, (Double) key.getDefaultValue());
    }
    
    private Property getStringProperty(Key key)
    {
        return config.get(key.category, key.key, (String) key.getDefaultValue());
    }
    
    private Property getBooleanProperty(Key key)
    {
        return config.get(key.category, key.key, (Boolean) key.getDefaultValue());
    }
    
    
    public int getInt(Key key)
    {
        return getIntProperty(key).getInt();
    }
    
    public double getDouble(Key key)
    {
        return getDoubleProperty(key).getDouble();
    }
    
    public String getString(Key key)
    {
        return getStringProperty(key).getString();
    }
    
    public boolean getBoolean(Key key)
    {
        return getBooleanProperty(key).getBoolean();
    }
    
    public void set(Key key, int value)
    {
        getIntProperty(key).set(value);
    }
    
    public void set(Key key, double value)
    {
        getDoubleProperty(key).set(value);
    }
    
    public void set(Key key, String value)
    {
        getStringProperty(key).set(value);
    }
    
    public void set(Key key, boolean value)
    {
        getBooleanProperty(key).set(value);
    }
    
    
    public void reset(Key key)
    {
        if (key.getDefaultValue() instanceof Integer) set(key, (Integer) key.getDefaultValue());
        else if (key.getDefaultValue() instanceof Double) set(key, (Double) key.getDefaultValue());
        else if (key.getDefaultValue() instanceof String) set(key, (String) key.getDefaultValue());
        else if (key.getDefaultValue() instanceof Boolean) set(key, (Boolean) key.getDefaultValue());
    }
    
    public void save()
    {
        config.save();
    }
}
