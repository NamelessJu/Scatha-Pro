package namelessju.scathapro.files;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.overlay.elements.GuiElement;
import namelessju.scathapro.miscellaneous.data.enums.*;
import namelessju.scathapro.files.framework.JsonFile;

import java.nio.file.Path;

public class Config extends JsonFile
{
    public Config(ScathaPro scathaPro)
    {
        super(scathaPro, Path.of("configV2.json"), true);
        setShouldInitializeWithDefaultValues(true);
    }
    
    //=========//
    // Overlay //
    //=========//
    public final OverlaySettings overlay = root.addValue("overlay", new OverlaySettings());
    public static class OverlaySettings extends ObjectValue
    {
        public final BooleanValue enabled
            = addBoolean("enabled", true);
        public final PrimitiveValueNullable<Float> positionX
            = addPrimitiveNullable("position.x", FLOAT_SERIALIZER);
        public final PrimitiveValueNullable<Float> positionY
            = addPrimitiveNullable("position.y", FLOAT_SERIALIZER);
        public final PrimitiveValueWithDefault<Float> scale
            = addPrimitiveWithDefault("scale", FLOAT_SERIALIZER, 1f);
        public final PrimitiveValueNullable<GuiElement.Alignment> alignmentOverride
            = addPrimitiveNullable("alignmentOverride", new EnumSerializer<>(GuiElement.Alignment.class));
        public final PrimitiveValueWithDefault<SecondaryWormStatsType> statsType
            = addPrimitiveWithDefault("statsType", new EnumSerializer<>(SecondaryWormStatsType.class), SecondaryWormStatsType.PER_LOBBY);
        public final PrimitiveValueWithDefault<Integer> scathaPercentageDecimalPlaces
            = addPrimitiveWithDefault("scathaPercentage.decimalPlaces", INTEGER_SERIALIZER, 2);
        public final PrimitiveValueWithDefault<Integer> scathaPercentageCycleAmountDuration
            = addPrimitiveWithDefault("scathaPercentage.cycle.amountDuration", INTEGER_SERIALIZER, 3);
        public final PrimitiveValueWithDefault<Integer> scathaPercentageCyclePercentageDuration
            = addPrimitiveWithDefault("scathaPercentage.cycle.percentageDuration", INTEGER_SERIALIZER, 2);
        public final BooleanValue scathaPercentageAlternativePositionEnabled
            = addBoolean("scathaPercentage.alternativePosition", false);
        public final BooleanValue backgroundEnabled
            = addBoolean("backgroundEnabled", true);
        
        public final ToggleableElementStates elementStates = addValue("elementStates", new ToggleableElementStates());
        public static class ToggleableElementStates extends ObjectValue
        {
            public final BooleanValue headerShown = addBoolean("headerShown", true);
            public final BooleanValue petDropCountersShown = addBoolean("petDropCountersShown", true);
            public final BooleanValue wormStatsShown = addBoolean("wormStatsShown", true);
            public final BooleanValue scathaKillsSinceLastPetDropShown = addBoolean("scathaKillsSinceLastPetDropShown", true);
            public final BooleanValue wormSpawnCooldownTimerShown = addBoolean("wormSpawnCooldownTimerShown", false);
            public final BooleanValue tunnelVisionStatusTextShown = addBoolean("tunnelVisionStatusTextShown", true);
            public final BooleanValue timeSinceWormSpawnShown = addBoolean("timeSinceWormSpawnShown", false);
            public final BooleanValue lobbyTimeShown = addBoolean("lobbyTimeShown", false);
            public final BooleanValue coordinatesShown = addBoolean("coordinatesShown", true);
            public final BooleanValue profileStatsShown = addBoolean("profileStatsShown", false);
            public final BooleanValue realTimeClockShown = addBoolean("realTimeClockShown", false);
        }
    }
    
    //========//
    // Alerts //
    //========//
    public final AlertSettings alerts = root.addValue("alerts", new AlertSettings());
    public static class AlertSettings extends ObjectValue
    {
        public final PrimitiveValueNullable<String> mode = addPrimitiveNullable("mode", STRING_SERIALIZER);
        public final PrimitiveValueNullable<String> customModeSubmode = addPrimitiveNullable("customMode.submode", STRING_SERIALIZER);
        
        public final PrimitiveValueWithDefault<Float> titleScale
            = addPrimitiveWithDefault("title.scale", FLOAT_SERIALIZER, 1f);
        public final PrimitiveValueWithDefault<Float> titlePositionX
            = addPrimitiveWithDefault("title.position.x", FLOAT_SERIALIZER, 0.5f);
        public final PrimitiveValueWithDefault<Float> titlePositionY
            = addPrimitiveWithDefault("title.position.y", FLOAT_SERIALIZER, 0.5f);
        public final PrimitiveValueNullable<GuiElement.Alignment> titleAlignmentOverride
            = addPrimitiveNullable("title.alignmentOverride", new EnumSerializer<>(GuiElement.Alignment.class));
        
        public final BooleanValue bedrockWallAlertEnabled
            = addBoolean("alertTypes.bedrockWallAlert.enabled", true);
        public final PrimitiveValueWithDefault<Integer> bedrockWallAlertTriggerDistance
            = addPrimitiveWithDefault("alertTypes.bedrockWallAlert.triggerDistance", INTEGER_SERIALIZER, 15);
        public final BooleanValue oldLobbyAlertEnabled
            = addBoolean("alertTypes.oldLobbyAlert.enabled", false);
        public final PrimitiveValueWithDefault<Integer> oldLobbyAlertTriggerDay
            = addPrimitiveWithDefault("alertTypes.oldLobbyAlert.triggerDay", INTEGER_SERIALIZER, 12);
        public final PrimitiveValueWithDefault<OldLobbyAlertTriggerMode> oldLobbyAlertTriggerMode
            = addPrimitiveWithDefault("alertTypes.oldLobbyAlert.triggerMode",
                new EnumSerializer<>(OldLobbyAlertTriggerMode.class), OldLobbyAlertTriggerMode.ALWAYS);
        public final BooleanValue wormSpawnCooldownEndAlertEnabled
            = addBoolean("alertTypes.wormSpawnCooldownEndAlert.enabled", false);
        public final BooleanValue wormPreSpawnAlertEnabled
            = addBoolean("alertTypes.wormPreSpawnAlert.enabled", true);
        public final BooleanValue regularWormSpawnAlertEnabled
            = addBoolean("alertTypes.wormSpawnAlert.enabled", true);
        public final BooleanValue scathaSpawnAlertEnabled
            = addBoolean("alertTypes.scathaSpawnAlert.enabled", true);
        public final BooleanValue scathaPetDropAlertEnabled
            = addBoolean("alertTypes.scathaPetDropAlert.enabled", true);
        public final BooleanValue highHeatAlertEnabled
            = addBoolean("alertTypes.highHeatAlert.enabled", false);
        public final PrimitiveValueWithDefault<Integer> highHeatAlertTriggerValue
            = addPrimitiveWithDefault("alertTypes.highHeatAlert.triggerValue", INTEGER_SERIALIZER, 98);
        public final BooleanValue pickaxeAbilityReadyAlertEnabled
            = addBoolean("alertTypes.pickaxeAbilityReadyAlert.enabled", true);
        public final BooleanValue goblinSpawnAlertEnabled
            = addBoolean("alertTypes.goblinSpawnAlert.enabled", true);
        public final BooleanValue jerrySpawnAlertEnabled
            = addBoolean("alertTypes.jerrySpawnAlert.enabled", true);
        public final BooleanValue antiSleepAlertEnabled
            = addBoolean("alertTypes.antiSleepAlert.enabled", false);
        public final PrimitiveValueWithDefault<Integer> antiSleepAlertIntervalMin
            = addPrimitiveWithDefault("alertTypes.antiSleepAlert.intervalMin", INTEGER_SERIALIZER, 3);
        public final PrimitiveValueWithDefault<Integer> antiSleepAlertIntervalMax
            = addPrimitiveWithDefault("alertTypes.antiSleepAlert.intervalMax", INTEGER_SERIALIZER, 10);
    }
    
    //==============//
    // Achievements //
    //==============//
    public final AchievementSettings achievements = root.addValue("achievements", new AchievementSettings());
    public static class AchievementSettings extends ObjectValue
    {
        public final BooleanValue playAlerts = addBoolean("playAlerts", true);
        public final BooleanValue playRepeatAlerts = addBoolean("playRepeatAlerts", true);
        public final BooleanValue listPreOpenCategories = addBoolean("listPreOpenCategories", false);
        public final BooleanValue listShowBonusAchievements = addBoolean("listShowBonusAchievements", false);
        public final BooleanValue listHideUnlockedAchievements = addBoolean("listHideUnlockedAchievements", false);
        public final BooleanValue listShowRepeatCounts = addBoolean("listShowRepeatCounts", true);
    }
    
    //========//
    // Sounds //
    //========//
    public final SoundSettings sounds = root.addValue("sounds", new SoundSettings());
    public static class SoundSettings extends ObjectValue
    {
        public final PrimitiveValueWithDefault<Float> volume = addPrimitiveWithDefault("volume", FLOAT_SERIALIZER, 1f);
        public final BooleanValue muteCrystalHollowsSounds = addBoolean("muteCrystalHollowsSounds.enabled", false);
        public final BooleanValue keepDragonLairSounds = addBoolean("muteCrystalHollowsSounds.keepDragonLairSounds", false);
    }
    
    //=============//
    // Unlockables //
    //=============//
    public final UnlockablesSettings unlockables = root.addValue("unlockables", new UnlockablesSettings());
    public static class UnlockablesSettings extends ObjectValue
    {
        public final BooleanValue scappaModeEnabled = addBoolean("scappaModeEnabled", false);
        public final BooleanValue overlayIconGooglyEyesEnabled = addBoolean("overlayIconGooglyEyesEnabled", false);
    }
    
    //===============//
    // Miscellaneous //
    //===============//
    public final MiscellaneousSettings miscellaneous = root.addValue("miscellaneous", new MiscellaneousSettings());
    public static class MiscellaneousSettings extends ObjectValue
    {
        // Chat stuff
        public final BooleanValue shortChatPrefixEnabled = addBoolean("shortChatPrefix", false);
        public final PrimitiveValueNullable<ChatCopyButtonMode> chatCopyButtonMode = addPrimitiveNullable("chatCopyButtonMode", new EnumSerializer<>(ChatCopyButtonMode.class));
        public final BooleanValue hideWormSpawnMessage = addBoolean("hideWormSpawnMessage", false);
        public final BooleanValue wormSpawnTimerMessageEnabled = addBoolean("wormSpawnTimerMessage", false);
        public final BooleanValue dryStreakMessageEnabled = addBoolean("dryStreakMessage", true);
        public final BooleanValue dailyStreakMessagesEnabled = addBoolean("dailyStreakMessages", true);
        // Player rotation
        public final BooleanValue rotationAnglesEnabled = addBoolean("rotationAngles.enabled", false);
        public final PrimitiveValueWithDefault<Integer> rotationAnglesDecimalPlaces
            = addPrimitiveWithDefault("rotationAngles.decimalPlaces", INTEGER_SERIALIZER, 2);
        public final BooleanValue rotationAnglesYawOnly = addBoolean("rotationAngles.yawOnly", false);
        public final BooleanValue rotationAnglesMinimalYawEnabled = addBoolean("rotationAngles.minimalYaw", false);
        public final BooleanValue alternativeCrosshairLayoutEnabled = addBoolean("alternativeCrosshairLayoutEnabled", false);
        public final PrimitiveValueWithDefault<Float> alternativeSensitivity =
            addPrimitiveWithDefault("alternativeSensitivity", FLOAT_SERIALIZER, 0f); // note: sensitivity is 0-1 as % of the setting slider
        // Automatic stuff
        public final BooleanValue automaticBackupsEnabled = addBoolean("automaticBackups", true);
        public final BooleanValue automaticUpdateCheckEnabled = addBoolean("automaticUpdateCheck", false);
        public final BooleanValue automaticStatsParsingEnabled = addBoolean("automaticStatsParsing", true);
        public final BooleanValue automaticPetDropScreenshotEnabled = addBoolean("automaticPetDropScreenshot", false);
        // Drop message extension
        public final PrimitiveValueNullable<DropMessageRarityMode> dropMessageRarityMode
            = addPrimitiveNullable("dropMessageExtension.rarity.addRarity", new EnumSerializer<>(DropMessageRarityMode.class));
        public final BooleanValue dropMessageRarityColored
            = addBoolean("dropMessageExtension.rarity.colored", true);
        public final BooleanValue dropMessageRarityUppercase
            = addBoolean("dropMessageExtension.rarity.uppercase", false);
        public final PrimitiveValueNullable<DropMessageStatMode> dropMessageMagicFindMode
            = addPrimitiveNullable("dropMessageExtension.stats.addMagicFind", new EnumSerializer<>(DropMessageStatMode.class));
        public final PrimitiveValueNullable<DropMessageStatMode> dropMessagePetLuckMode
            = addPrimitiveNullable("dropMessageExtension.stats.addPetLuck", new EnumSerializer<>(DropMessageStatMode.class));
        public final PrimitiveValueNullable<DropMessageStatMode> dropMessageEmfMode
            = addPrimitiveNullable("dropMessageExtension.stats.addEffectiveMagicFind", new EnumSerializer<>(DropMessageStatMode.class));
        // Other
        public final BooleanValue aprilFoolsFakeDropEnabled = addBoolean("aprilFoolsFakeDropEnabled", true);
    }
    
    //===============//
    // Accessibility //
    //===============//
    public final AccessibilitySettings accessibility = root.addValue("accessibility", new AccessibilitySettings());
    public static class AccessibilitySettings extends ObjectValue
    {
        public final BooleanValue useHighContrastColors = addBoolean("useHighContrastColors", false);
    }
    
    //=====//
    // Dev //
    //=====//
    public final DeveloperSettings dev = root.addValue("dev", new DeveloperSettings());
    public static class DeveloperSettings extends ObjectValue
    {
        public final BooleanValue devModeEnabled = addBoolean("devMode", false);
    }
}
