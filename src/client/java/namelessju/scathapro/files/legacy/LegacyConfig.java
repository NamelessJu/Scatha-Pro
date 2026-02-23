package namelessju.scathapro.files.legacy;

import com.google.gson.JsonObject;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.overlay.elements.GuiElement;
import namelessju.scathapro.miscellaneous.data.enums.*;
import namelessju.scathapro.files.Config;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.files.framework.ScathaProFile;
import namelessju.scathapro.util.JsonUtil;
import namelessju.scathapro.util.TextUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class for reading the old v1 config file that used the Forge config system<br>
 * When loaded, overrides all found values in the config of the supplied ScathaPro instance
 */
public class LegacyConfig extends ScathaProFile
{
    private HashMap<String, ValueMapper> mappings = null;
    private final DropMessageStatModesMapper dropMessageStatModesMapper = new DropMessageStatModesMapper();
    
    public LegacyConfig(ScathaPro scathaPro)
    {
        super(scathaPro, Path.of("config.cfg"));
    }
    
    private void setupMappings()
    {
        if (mappings != null) return;
        mappings = new HashMap<>();
        
        Config config = scathaPro.config;
        
        Consumer<HashMap<String, GuiElement.Alignment>> guiAlignmentMappingsBuilder = enumMappings -> {
            enumMappings.put("LEFT", GuiElement.Alignment.LEFT);
            enumMappings.put("CENTER", GuiElement.Alignment.CENTER);
            enumMappings.put("RIGHT", GuiElement.Alignment.RIGHT);
        };
        
        mappings.put("overlay/enabled", new BooleanMapper(config.overlay.enabled));
        mappings.put("overlay/x", new FloatMapper(config.overlay.positionX));
        mappings.put("overlay/y", new FloatMapper(config.overlay.positionY));
        mappings.put("overlay/scale", new FloatMapper(config.overlay.scale));
        mappings.put("overlay/alignment", new EnumMapper<>(config.overlay.alignmentOverride, guiAlignmentMappingsBuilder));
        mappings.put("overlay/statsType", new EnumMapper<>(config.overlay.statsType, enumMappings -> {
            enumMappings.put("PER_LOBBY", SecondaryWormStatsType.PER_LOBBY);
            enumMappings.put("PER_SESSION", SecondaryWormStatsType.PER_SESSION);
            enumMappings.put("PER_DAY", SecondaryWormStatsType.PER_DAY);
        }));
        mappings.put("overlay/scathaPercentageDecimalPlaces", new IntMapper(config.overlay.scathaPercentageDecimalPlaces));
        mappings.put("overlay/scathaPercentageCycleAmountDuration", new IntMapper(config.overlay.scathaPercentageCycleAmountDuration));
        mappings.put("overlay/scathaPercentageCyclePercentageDuration", new IntMapper(config.overlay.scathaPercentageCyclePercentageDuration));
        mappings.put("overlay/scathaPercentageAlternativePosition", new BooleanMapper(config.overlay.scathaPercentageAlternativePositionEnabled));
        mappings.put("overlay/overlayElementStates", oldValueString -> {
            if (JsonUtil.parseJson(oldValueString) instanceof JsonObject jsonObject)
            {
                BiConsumer<String, JsonFile.BooleanValue> valueUpdater = (oldValueKey, configValue) -> {
                    if (JsonUtil.getBoolean(jsonObject, oldValueKey) instanceof Boolean value)
                        configValue.set(value);
                };
                valueUpdater.accept("header", config.overlay.elementStates.headerShown);
                valueUpdater.accept("petDrops", config.overlay.elementStates.petDropCountersShown);
                valueUpdater.accept("wormStats", config.overlay.elementStates.wormStatsShown);
                valueUpdater.accept("scathaKillsSinceLastPetDrop", config.overlay.elementStates.scathaKillsSinceLastPetDropShown);
                valueUpdater.accept("spawnCooldownTimer", config.overlay.elementStates.wormSpawnCooldownTimerShown);
                valueUpdater.accept("anomalousDesireStatusText", config.overlay.elementStates.tunnelVisionStatusTextShown);
                valueUpdater.accept("timeSinceWormSpawn", config.overlay.elementStates.timeSinceWormSpawnShown);
                valueUpdater.accept("time", config.overlay.elementStates.lobbyTimeShown);
                valueUpdater.accept("coords", config.overlay.elementStates.coordinatesShown);
                valueUpdater.accept("profileStats", config.overlay.elementStates.profileStatsShown);
                valueUpdater.accept("realTimeClock", config.overlay.elementStates.realTimeClockShown);
            }
        });
        mappings.put("overlay/backgroundEnabled", new BooleanMapper(config.overlay.backgroundEnabled));
        
        mappings.put("sounds/volume", new FloatMapper(config.sounds.volume));
        mappings.put("sounds/muteCrystalHollowsSounds", new BooleanMapper(config.sounds.muteCrystalHollowsSounds));
        mappings.put("sounds/muteCrystalHollowsSounds.keepDragonLairSounds", new BooleanMapper(config.sounds.keepDragonLairSounds));
        
        mappings.put("alerts/mode", new StringMapper(config.alerts.mode));
        mappings.put("alerts/customModeSubmode", new StringMapper(config.alerts.customModeSubmode));
        mappings.put("alerts/title/scale", new FloatMapper(config.alerts.titleScale));
        mappings.put("alerts/title/positionX", new FloatMapper(config.alerts.titlePositionX));
        mappings.put("alerts/title/positionY", new FloatMapper(config.alerts.titlePositionY));
        mappings.put("alerts/title/alignment", new EnumMapper<>(config.alerts.titleAlignmentOverride, guiAlignmentMappingsBuilder));
        mappings.put("alerts/wall", new BooleanMapper(config.alerts.bedrockWallAlertEnabled));
        mappings.put("alerts/wall.triggerDistance", new IntMapper(config.alerts.bedrockWallAlertTriggerDistance));
        mappings.put("alerts/oldlobby/enabled", new BooleanMapper(config.alerts.oldLobbyAlertEnabled));
        mappings.put("alerts/oldlobby/triggerDay", new IntMapper(config.alerts.oldLobbyAlertTriggerDay));
        mappings.put("alerts/oldlobby/triggerMode", new EnumMapper<>(config.alerts.oldLobbyAlertTriggerMode, enumMappings -> {
            enumMappings.put("ALWAYS", OldLobbyAlertTriggerMode.ALWAYS);
            enumMappings.put("ON_JOIN", OldLobbyAlertTriggerMode.ON_JOIN);
            enumMappings.put("ON_NEW_DAY", OldLobbyAlertTriggerMode.ON_NEW_DAY);
        }));
        mappings.put("alerts/wormSpawnCooldownEnd", new BooleanMapper(config.alerts.wormSpawnCooldownEndAlertEnabled));
        mappings.put("alerts/wormsPre", new BooleanMapper(config.alerts.wormPreSpawnAlertEnabled));
        mappings.put("alerts/worms", new BooleanMapper(config.alerts.regularWormSpawnAlertEnabled));
        mappings.put("alerts/scathas", new BooleanMapper(config.alerts.scathaSpawnAlertEnabled));
        mappings.put("alerts/pet", new BooleanMapper(config.alerts.scathaPetDropAlertEnabled));
        mappings.put("alerts/highHeat", new BooleanMapper(config.alerts.highHeatAlertEnabled));
        mappings.put("alerts/highHeat.triggerValue", new IntMapper(config.alerts.highHeatAlertTriggerValue));
        mappings.put("alerts/pickaxeAbilityReadyAlert", new BooleanMapper(config.alerts.pickaxeAbilityReadyAlertEnabled));
        mappings.put("alerts/goblinSpawn", new BooleanMapper(config.alerts.goblinSpawnAlertEnabled));
        mappings.put("alerts/jerrySpawn", new BooleanMapper(config.alerts.jerrySpawnAlertEnabled));
        mappings.put("alerts/antisleep/enabled", new BooleanMapper(config.alerts.antiSleepAlertEnabled));
        mappings.put("alerts/antisleep/intervalMin", new IntMapper(config.alerts.antiSleepAlertIntervalMin));
        mappings.put("alerts/antisleep/intervalMax", new IntMapper(config.alerts.antiSleepAlertIntervalMax));
        
        mappings.put("achievements/listPreOpenCategories", new BooleanMapper(config.achievements.listPreOpenCategories));
        mappings.put("achievements/playAchievementAlerts", new BooleanMapper(config.achievements.playAlerts));
        mappings.put("achievements/playRepeatAchievementAlerts", new BooleanMapper(config.achievements.playRepeatAlerts));
        mappings.put("achievements/bonusAchievementsShown", new BooleanMapper(config.achievements.listShowBonusAchievements));
        mappings.put("achievements/hideUnlockedAchievements", new BooleanMapper(config.achievements.listHideUnlockedAchievements));
        mappings.put("achievements/repeatCountsShown", new BooleanMapper(config.achievements.listShowRepeatCounts));
        
        mappings.put("other/shortChatPrefix", new BooleanMapper(config.miscellaneous.shortChatPrefixEnabled));
        mappings.put("other/hideWormSpawnMessage", new BooleanMapper(config.miscellaneous.hideWormSpawnMessage));
        mappings.put("other/dryStreakMessage", new BooleanMapper(config.miscellaneous.dryStreakMessageEnabled));
        mappings.put("other/dailyScathaFarmingStreakMessage", new BooleanMapper(config.miscellaneous.dailyStreakMessagesEnabled));
        mappings.put("other/chatCopy", new EnumMapper<>(config.miscellaneous.chatCopyButtonMode, enumMappings -> {
            enumMappings.put("true", ChatCopyButtonMode.SUGGEST_MESSAGE);
        }));
        mappings.put("other/wormSpawnTimer", new BooleanMapper(config.miscellaneous.wormSpawnTimerMessageEnabled));
        mappings.put("other/showRotationAngles", new BooleanMapper(config.miscellaneous.rotationAnglesEnabled));
        mappings.put("other/rotationAnglesYawOnly", new BooleanMapper(config.miscellaneous.rotationAnglesYawOnly));
        mappings.put("other/rotationAnglesDecimalPlaces", new IntMapper(config.miscellaneous.rotationAnglesDecimalPlaces));
        mappings.put("other/rotationAnglesMinimalYaw", new BooleanMapper(config.miscellaneous.rotationAnglesMinimalYawEnabled));
        mappings.put("other/alternativeSensitivity", new FloatMapper(config.miscellaneous.alternativeSensitivity));
        mappings.put("other/automaticBackups", new BooleanMapper(config.miscellaneous.automaticBackupsEnabled));
        mappings.put("other/automaticUpdateChecks", new BooleanMapper(config.miscellaneous.automaticUpdateCheckEnabled));
        mappings.put("other/automaticStatsParsing", new BooleanMapper(config.miscellaneous.automaticStatsParsingEnabled));
        mappings.put("other/automaticPetDropScreenshot", new BooleanMapper(config.miscellaneous.automaticPetDropScreenshotEnabled));
        mappings.put("other/dropMessageRarityMode", new EnumMapper<>(config.miscellaneous.dropMessageRarityMode, enumMappings -> {
            enumMappings.put("SUFFIX", DropMessageRarityMode.SUFFIX);
            enumMappings.put("PREFIX", DropMessageRarityMode.PREFIX);
            enumMappings.put("PREFIX_NO_BRACKETS", DropMessageRarityMode.PREFIX_NO_BRACKETS);
        }));
        mappings.put("other/dropMessageRarityColored", new BooleanMapper(config.miscellaneous.dropMessageRarityColored));
        mappings.put("other/dropMessageRarityUppercase", new BooleanMapper(config.miscellaneous.dropMessageRarityUppercase));
        mappings.put("other/dropMessageStatsMode", oldValueString -> {
            dropMessageStatModesMapper.statsMode = oldValueString;
            dropMessageStatModesMapper.applyIfFullyLoaded();
        });
        mappings.put("other/dropMessageStatAbbreviations", oldValueString -> {
            dropMessageStatModesMapper.abbreviatedNames = Boolean.parseBoolean(oldValueString);
            dropMessageStatModesMapper.applyIfFullyLoaded();
        });
        
        mappings.put("other/aprilFoolsFakeDropEnabled", new BooleanMapper(config.miscellaneous.aprilFoolsFakeDropEnabled));
        
        mappings.put("other/scappaMode", new BooleanMapper(config.unlockables.scappaModeEnabled));
        mappings.put("other/overlayIconGooglyEyes", new BooleanMapper(config.unlockables.overlayIconGooglyEyesEnabled));
        
        mappings.put("accessibility/highContrastColors", new BooleanMapper(config.accessibility.useHighContrastColors));
        
        mappings.put("dev/devMode", new BooleanMapper(config.dev.devModeEnabled));
    }
    
    @Override
    protected void deserialize(@Nullable String content)
    {
        if (content == null) return;
        
        ScathaPro.LOGGER.info("Parsing legacy config");
        
        setupMappings();
        
        ArrayList<String> categories = new ArrayList<>();
        for (String line : content.split(TextUtil.NEW_LINE_REGEX))
        {
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            
            if (line.endsWith("{"))
            {
                categories.add(line.substring(0, line.length() - 2).strip());
            }
            else if (line.equals("}"))
            {
                if (!categories.isEmpty()) categories.removeLast();
            }
            else if (!categories.isEmpty())
            {
                String keyValueString = line.substring(2); // remove data type flag
                String[] keyValuePair = keyValueString.split("=", 2);
                String key = keyValuePair[0];
                String stringValue = keyValuePair.length > 1 ? keyValuePair[1] : null;
                if (stringValue == null || stringValue.isBlank()) continue;
                
                StringBuilder oldPath = new StringBuilder();
                for (String category : categories)
                {
                    if (!oldPath.isEmpty()) oldPath.append("/");
                    oldPath.append(category);
                }
                oldPath.append("/").append(key);
                
                ValueMapper valueMapper = mappings.get(oldPath.toString());
                if (valueMapper == null) continue;
                valueMapper.load(stringValue);
            }
        }
        
        
        dropMessageStatModesMapper.apply();
    }
    
    @Override
    protected @NonNull String serialize()
    {
        throw new NotImplementedException("This file cannot be saved");
    }
    
    private static class StringMapper extends SingleValueMapper<JsonFile.PrimitiveValue<String>>
    {
        public StringMapper(JsonFile.PrimitiveValue<String> configValue)
        {
            super(configValue);
        }
        
        @Override
        public void load(String oldValueString)
        {
            configValue.set(oldValueString);
        }
    }
    
    private interface ValueMapper
    {
        void load(String oldValueString);
    }
    
    private static abstract class SingleValueMapper<T extends JsonFile.JsonValue> implements ValueMapper
    {
        protected final @NonNull T configValue;
        
        public SingleValueMapper(@NonNull T configValue)
        {
            this.configValue = Objects.requireNonNull(configValue);
        }
    }
    
    private static abstract class PrimitiveValueMapper<T> extends SingleValueMapper<JsonFile.PrimitiveValue<T>>
    {
        private final @NonNull Function<String, T> valueParser;
        
        public PrimitiveValueMapper(JsonFile.@NonNull PrimitiveValue<T> configValue, @NonNull Function<String, T> valueParser)
        {
            super(configValue);
            this.valueParser = Objects.requireNonNull(valueParser);
        }
        
        @Override
        public void load(String oldValueString)
        {
            try
            {
                configValue.set(valueParser.apply(oldValueString));
            }
            catch (Exception ignored) {}
        }
    }
    
    private static class BooleanMapper extends PrimitiveValueMapper<Boolean>
    {
        public BooleanMapper(JsonFile.PrimitiveValue<Boolean> configValue)
        {
            super(configValue, Boolean::parseBoolean);
        }
    }
    
    private static class FloatMapper extends PrimitiveValueMapper<Float>
    {
        public FloatMapper(JsonFile.PrimitiveValue<Float> configValue)
        {
            super(configValue, Float::parseFloat);
        }
    }
    
    private static class IntMapper extends PrimitiveValueMapper<Integer>
    {
        public IntMapper(JsonFile.PrimitiveValue<Integer> configValue)
        {
            super(configValue, Integer::parseInt);
        }
    }
    
    private static class EnumMapper<T extends Enum<T>> extends SingleValueMapper<JsonFile.PrimitiveValue<T>>
    {
        // Explicit mappings ensure that this still works
        // even when the current enums get renamed
        private final HashMap<String, T> enumMappings = new HashMap<>();
        
        public EnumMapper(JsonFile.PrimitiveValue<T> configValue, Consumer<HashMap<String, T>> valueMappingsBuilder)
        {
            super(configValue);
            valueMappingsBuilder.accept(enumMappings);
        }
        
        @Override
        public void load(String oldValueString)
        {
            configValue.set(enumMappings.get(oldValueString));
        }
    }
    
    private class DropMessageStatModesMapper
    {
        public String statsMode = null;
        public Boolean abbreviatedNames = null;
        
        private boolean applied = false;
        
        public void applyIfFullyLoaded()
        {
            if (statsMode != null && abbreviatedNames != null)
            {
                apply();
            }
        }
        
        public void apply()
        {
            if (applied) return;
            
            switch (statsMode)
            {
                case "ADD_PET_LUCK":
                    scathaPro.config.miscellaneous.dropMessageMagicFindMode.set(getAbbreviatableMode());
                    scathaPro.config.miscellaneous.dropMessagePetLuckMode.set(getAbbreviatableMode());
                    scathaPro.config.miscellaneous.dropMessageEmfMode.set(null);
                    break;
                case "ADD_PET_LUCK_AND_EMF":
                    scathaPro.config.miscellaneous.dropMessageMagicFindMode.set(getAbbreviatableMode());
                    scathaPro.config.miscellaneous.dropMessagePetLuckMode.set(getAbbreviatableMode());
                    scathaPro.config.miscellaneous.dropMessageEmfMode.set(DropMessageStatMode.SHORT_NAME);
                    break;
                case "EMF_ONLY_FULL_NAME":
                    scathaPro.config.miscellaneous.dropMessageMagicFindMode.set(null);
                    scathaPro.config.miscellaneous.dropMessagePetLuckMode.set(null);
                    scathaPro.config.miscellaneous.dropMessageEmfMode.set(DropMessageStatMode.FULL_NAME);
                    break;
                case "EMF_ONLY":
                    scathaPro.config.miscellaneous.dropMessageMagicFindMode.set(null);
                    scathaPro.config.miscellaneous.dropMessagePetLuckMode.set(null);
                    scathaPro.config.miscellaneous.dropMessageEmfMode.set(DropMessageStatMode.SHORT_NAME);
                    break;
                case null, default:
                    scathaPro.config.miscellaneous.dropMessageMagicFindMode.set(getAbbreviatableMode());
                    scathaPro.config.miscellaneous.dropMessagePetLuckMode.set(null);
                    scathaPro.config.miscellaneous.dropMessageEmfMode.set(null);
                    break;
            }
            
            applied = true;
        }
        
        private DropMessageStatMode getAbbreviatableMode()
        {
            if (abbreviatedNames != null && abbreviatedNames) return DropMessageStatMode.SHORT_NAME;
            return DropMessageStatMode.FULL_NAME;
        }
    }
}
