package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;
import net.minecraftforge.common.config.ConfigCategory;
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
        overlayAlignment("overlay", "alignment", ""),
        statsType("overlay", "statsType", ""),
        scathaPercentageAlternativePosition("overlay", "scathaPercentageAlternativePosition", false),
        scathaPercentageCycleAmountDuration("overlay", "scathaPercentageCycleAmountDuration", 3),
        scathaPercentageCyclePercentageDuration("overlay", "scathaPercentageCyclePercentageDuration", 2),
        scathaPercentageDecimalDigits("overlay", "scathaPercentageDecimalPlaces", 2),
        overlayElementStates("overlay", "overlayElementStates", ""),
        overlayBackgroundEnabled("overlay", "backgroundEnabled", true),
        
        // Sounds
        soundsVolume("sounds", "volume", 1D),
        muteCrystalHollowsSounds("sounds", "muteCrystalHollowsSounds", false),
        keepDragonLairSounds("sounds", "muteCrystalHollowsSounds.keepDragonLairSounds", false),
        
        // Alerts
        mode("alerts", "mode", ""),
        customModeSubmode("alerts", "customModeSubmode", ""),
        
        alertTitleScale("alerts.title", "scale", 1D),
        alertTitlePositionX("alerts.title", "positionX", 0.5D),
        alertTitlePositionY("alerts.title", "positionY", 0.5D),
        alertTitleAlignment("alerts.title", "alignment", ""),
        
        bedrockWallAlert("alerts", "wall", true),
        bedrockWallAlertTriggerDistance("alerts", "wall.triggerDistance", 15),
        oldLobbyAlert("alerts.oldLobby", "enabled", false),
        oldLobbyAlertTriggerDay("alerts.oldLobby", "triggerDay", 12),
        oldLobbyAlertTriggerMode("alerts.oldLobby", "triggerMode", ""),
        wormSpawnCooldownEndAlert("alerts", "wormSpawnCooldownEnd", false),
        wormPrespawnAlert("alerts", "wormsPre", true),
        regularWormSpawnAlert("alerts", "worms", true),
        scathaSpawnAlert("alerts", "scathas", true),
        scathaPetDropAlert("alerts", "pet", true),
        highHeatAlert("alerts", "highHeat", false),
        highHeatAlertTriggerValue("alerts", "highHeat.triggerValue", 98),
        pickaxeAbilityReadyAlert("alerts", "pickaxeAbilityReadyAlert", true),
        goblinSpawnAlert("alerts", "goblinSpawn", true),
        jerrySpawnAlert("alerts", "jerrySpawn", true),
        antiSleepAlert("alerts.antiSleep", "enabled", false),
        antiSleepAlertIntervalMin("alerts.antiSleep", "intervalMin", 3),
        antiSleepAlertIntervalMax("alerts.antiSleep", "intervalMax", 10),
        
        // Achievements
        achievementListPreOpenCategories("achievements", "listPreOpenCategories", false),
        playAchievementAlerts("achievements", "playAchievementAlerts", true),
        playRepeatAchievementAlerts("achievements", "playRepeatAchievementAlerts", true),
        bonusAchievementsShown("achievements", "bonusAchievementsShown", false),
        hideUnlockedAchievements("achievements", "hideUnlockedAchievements", false),
        repeatCountsShown("achievements", "repeatCountsShown", true),
        
        // Other
        
        // Chat stuff
        shortChatPrefix("other", "shortChatPrefix", false),
        hideWormSpawnMessage("other", "hideWormSpawnMessage", false),
        dryStreakMessage("other", "dryStreakMessage", true),
        dailyScathaFarmingStreakMessage("other", "dailyScathaFarmingStreakMessage", true),
        chatCopy("other", "chatCopy", false),
        wormSpawnTimer("other", "wormSpawnTimer", false),
        // Player Rotation
        showRotationAngles("other", "showRotationAngles", false),
        rotationAnglesYawOnly("other", "rotationAnglesYawOnly", false),
        rotationAnglesDecimalDigits("other", "rotationAnglesDecimalPlaces", 2),
        rotationAnglesMinimalYaw("other", "rotationAnglesMinimalYaw", false),
        alternativeSensitivity("other", "alternativeSensitivity", 0D),
        // Automatic stuff
        automaticBackups("other", "automaticBackups", true),
        automaticUpdateChecks("other", "automaticUpdateChecks", true),
        automaticWormStatsParsing("other", "automaticStatsParsing", true),
        automaticPetDropScreenshot("other", "automaticPetDropScreenshot", false),
        // Drop message extension
        dropMessageRarityMode("other", "dropMessageRarityMode", ""),
        dropMessageRarityColored("other", "dropMessageRarityColored", true),
        dropMessageRarityUppercase("other", "dropMessageRarityUppercase", false),
        dropMessageStatsMode("other", "dropMessageStatsMode", ""),
        dropMessageCleanMagicFind("other", "dropMessageCleanMagicFind", false),
        dropMessageStatAbbreviations("other", "dropMessageStatAbbreviations", false),
        
        aprilFoolsFakeDropEnabled("other", "aprilFoolsFakeDropEnabled", true),
        
        // Unlockables States
        scappaMode("other", "scappaMode", false),
        overlayIconGooglyEyes("other", "overlayIconGooglyEyes", false),
        
        
        // Accessibility
        highContrastColors("accessibility", "highContrastColors", false),
        
        // Dev
        devMode("dev", "devMode", false),
        debugLogs("dev", "debugLogs", false);
        
        
        public final Property.Type type; 
        public final String category;
        public final String key;
        public final String defaultValue;
        
        Key(String category, String key, String defaultValue)
        {
            this(category, key, defaultValue, Property.Type.STRING);
        }
        
        Key(String category, String key, boolean defaultValue)
        {
            this(category, key, Boolean.toString(defaultValue), Property.Type.BOOLEAN);
        }
        
        Key(String category, String key, double defaultValue)
        {
            this(category, key, Double.toString(defaultValue), Property.Type.DOUBLE);
        }
        
        Key(String category, String key, int defaultValue)
        {
            this(category, key, Integer.toString(defaultValue), Property.Type.INTEGER);
        }
        
        Key(String category, String key, String defaultValue, Property.Type type)
        {
            this.category = category;
            this.key = key;
            this.defaultValue = defaultValue;
            this.type = type;
        }
    }
    
    
    private Configuration config;
    private boolean isDirty = false;
    
    public void init()
    {
        loadFile();
        
        convertOldConfigEntries();
        
        // Fill config with all registered keys
        
        boolean changed = false;
        for (Key key : Key.values())
        {
            if (getProperty(key.category, key.key, key.type) != null) continue;
            getProperty(key); // auto-adds the key
            changed = true;
        }
        if (changed)
        {
            isDirty = true;
            save();
            ScathaPro.getInstance().logDebug("Config updated");
        }
    }
    
    private void convertOldConfigEntries()
    {
        // convert old integer-based mode ID to new string-based mode ID
        if (!config.hasKey(Key.mode.category, Key.mode.key))
        {
            Property oldModeProperty = getProperty("other", "mode", Property.Type.INTEGER);
            if (oldModeProperty != null)
            {
                String newMode;
                switch (oldModeProperty.getInt())
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
            }
        }
        
        // convert sounds volume key
        if (!config.hasKey(Key.soundsVolume.category, Key.soundsVolume.key))
        {
            Property oldVolumeProperty = getProperty("alerts", "volume", Property.Type.DOUBLE);
            if (oldVolumeProperty != null)
            {
                set(Key.soundsVolume, oldVolumeProperty.getDouble(-1));
            }
        }
        
        save();
    }
    
    public void loadFile()
    {
        config = new Configuration(SaveManager.getSaveFile("config.cfg"));
        config.load();
        isDirty = false;
        ScathaPro.getInstance().log("Config loaded");
    }
    
    
    /**
     * Gets a config property without automatically adding it to the file
     */
    private Property getProperty(String category, String key, Property.Type type)
    {
        ConfigCategory cat = config.hasCategory(category) ? config.getCategory(category) : null;
        if (cat != null)
        {
            Property property = cat.get(key);
            if (property != null && property.getType() == type) return property;
        }
        return null;
    }
    
    /**
     * Gets a config property for a key<br>
     * Automatically adds the key to the file if it doesn't exist yet (without saving)
     */
    private Property getProperty(Key key)
    {
        return config.get(key.category, key.key, key.defaultValue, null, key.type);
    }
    
    public int getInt(Key key)
    {
        return getProperty(key).getInt();
    }
    
    public double getDouble(Key key)
    {
        return getProperty(key).getDouble();
    }
    
    public String getString(Key key)
    {
        return getProperty(key).getString();
    }
    
    public boolean getBoolean(Key key)
    {
        return getProperty(key).getBoolean();
    }
    
    public <U extends Enum<U>> U getEnum(Config.Key configKey, Class<U> enumClass)
    {
        try
        {
            String stringValue = getString(configKey);
            if (stringValue.isEmpty()) return null;
            return Enum.valueOf(enumClass, stringValue);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    public void set(Key key, int value)
    {
        Property property = getProperty(key);
        if (value == property.getInt()) return;
        property.set(value);
        isDirty = true;
    }
    
    public void set(Key key, double value)
    {
        Property property = getProperty(key);
        if (Math.abs(value - property.getDouble()) < 0.00001D) return;
        property.set(value);
        isDirty = true;
    }
    
    public void set(Key key, String value)
    {
        Property property = getProperty(key);
        String currentValue = property.getString();
        if (currentValue == null) { if (value == null) return; }
        else if (currentValue.equals(value)) return;
        property.set(value);
        isDirty = true;
    }
    
    public void set(Key key, boolean value)
    {
        Property property = getProperty(key);
        if (value == property.getBoolean()) return;
        property.set(value);
        isDirty = true;
    }
    
    
    public void reset(Key key)
    {
        set(key, key.defaultValue);
    }
    
    public void save()
    {
        if (!isDirty) return;
        config.save();
        isDirty = false;
        ScathaPro.getInstance().logDebug("Config saved");
    }
}
