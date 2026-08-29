package namelessju.scathapro.files;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.alerts.alertmodes.AlertModeManager;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.files.framework.ObjectRootJsonFile;
import namelessju.scathapro.gui.overlay.elements.OverlayElement;
import namelessju.scathapro.miscellaneous.data.enums.*;
import namelessju.scathapro.util.JsonUtil;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class Config extends ObjectRootJsonFile
{
    public Config(ScathaPro scathaPro)
    {
        super(scathaPro, scathaPro.getSaveDirectoryPath().resolve("configV2.json").toFile(), true);
        this.savesDefaultValues = true;
        this.postLoadConsumer = new ConfigUpdater(this);
    }

    public final PrimitiveValueNullable<Integer> version = root.addPrimitiveNullable("version", INTEGER_SERIALIZER);

    //=========//
    // Overlay //
    //=========//
    public final OverlaySettings overlay = root.addValue("overlay", new OverlaySettings());
    public static class OverlaySettings extends ObjectValue
    {
        public final BooleanValue enabled
            = addBoolean("enabled", true);
        public final BooleanValue showImmediately
            = addBoolean("showImmediately", false);
        public final PrimitiveValueNullable<Float> positionX
            = addPrimitiveNullable("position.x", FLOAT_SERIALIZER);
        public final PrimitiveValueNullable<Float> positionY
            = addPrimitiveNullable("position.y", FLOAT_SERIALIZER);
        public final PrimitiveValueWithDefault<Float> scale
            = addPrimitiveWithDefault("scale", FLOAT_SERIALIZER, 1f);
        public final PrimitiveValueNullable<OverlayElement.Alignment> alignmentOverride
            = addPrimitiveNullable("alignmentOverride", new EnumSerializer<>(OverlayElement.Alignment.class));
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
        public final PrimitiveValueWithDefault<Float> backgroundOpacity
            = addPrimitiveWithDefault("backgroundOpacity", FLOAT_SERIALIZER, 0.3f);
        public final BooleanValue iconsEnabled
            = addBoolean("iconsEnabled", true);

        public final ToggleableElementStates elementStates = addValue("elementStates", new ToggleableElementStates());
        public static class ToggleableElementStates extends ObjectValue
        {
            public final BooleanValue headerShown = addBoolean("headerShown", true);
            public final BooleanValue petDropCountersShown = addBoolean("petDropCountersShown", true);
            public final BooleanValue wormStatsShown = addBoolean("wormStatsShown", true);
            public final BooleanValue scathaKillsSinceLastPetDropShown = addBoolean("scathaKillsSinceLastPetDropShown", true);
            public final BooleanValue blockBransCounterShown = addBoolean("blockBransCounterShown", false);
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
        public final PrimitiveValueWithDefault<AlertMode> mode = addPrimitiveWithDefault("mode", ScathaProSerializers.ALERT_MODE_SERIALIZER, AlertModeManager.DEFAULT_MODE);
        public final PrimitiveValueNullable<String> customModeSubmode = addPrimitiveNullable("customMode.submode", STRING_SERIALIZER);

        public final PrimitiveValueWithDefault<Float> titleScale
            = addPrimitiveWithDefault("title.scale", FLOAT_SERIALIZER, 1f);
        public final PrimitiveValueWithDefault<Float> titlePositionX
            = addPrimitiveWithDefault("title.position.x", FLOAT_SERIALIZER, 0.5f);
        public final PrimitiveValueWithDefault<Float> titlePositionY
            = addPrimitiveWithDefault("title.position.y", FLOAT_SERIALIZER, 0.5f);
        public final PrimitiveValueNullable<OverlayElement.Alignment> titleAlignmentOverride
            = addPrimitiveNullable("title.alignmentOverride", new EnumSerializer<>(OverlayElement.Alignment.class));

        public final BooleanValue bedrockWallAlertEnabled
            = addBoolean("alertTypes.bedrockWallAlert.enabled", true);
        public final BooleanValue crawlingAlertEnabled
            = addBoolean("alertTypes.crawlingAlert.enabled", true);
        public final PrimitiveValueWithDefault<Integer> crawlingAlertTriggerDelayTicks
            = addPrimitiveWithDefault("alertTypes.crawlingAlert.triggerDelayTicks", INTEGER_SERIALIZER, 40);
        public final PrimitiveValueWithDefault<Integer> crawlingAlertTriggerIntervalTicks
            = addPrimitiveWithDefault("alertTypes.crawlingAlert.triggerIntervalTicks", INTEGER_SERIALIZER, 60);
        public final BooleanValue obstacleAlertEnabled
            = addBoolean("alertTypes.obstacleAlert.enabled", false);
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
            = addBoolean("alertTypes.highHeatAlert.enabled", true);
        public final PrimitiveValueWithDefault<Integer> highHeatAlertTriggerValue
            = addPrimitiveWithDefault("alertTypes.highHeatAlert.triggerValue", INTEGER_SERIALIZER, 99);
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

    //==========//
    // Pet Drop //
    //==========//
    public final PetDropSettings petDrop = root.addValue("petDrop", new PetDropSettings());
    public static class PetDropSettings extends ObjectValue
    {
        public final BooleanValue itemPopupEnabled = addBoolean("itemPopup.enabled", true);
        public final PrimitiveValueWithDefault<Integer> itemPopupAnimationTicks = addPrimitiveWithDefault("itemPopup.animationTicks", INTEGER_SERIALIZER, 40);
        public final BooleanValue itemPopupUseAltRotAnimCurve = addBoolean("itemPopup.useAlternativeRotationAnimationCurve", false);
        public final BooleanValue fireworkEnabled = addBoolean("firework.enabled", true);
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
    // Worm Entities //
    //===============//
    public final WormSettings worms = root.addValue("worms", new WormSettings());
    public static class WormSettings extends ObjectValue
    {
        public final BooleanValue showLifetimeLeft = addBoolean("showLifetimeLeft", true);

        // Textures
        public final BooleanValue revertRegularWormTexture = addBoolean("revertRegularWormTexture", false);
        public final ResolvableProfileValue regularWormPlayerHeadProfile = addValue("regularWormPlayerHeadProfile", new ResolvableProfileValue());
        public final ResolvableProfileValue scathaPlayerHeadProfile = addValue("scathaPlayerHeadProfile", new ResolvableProfileValue());
    }

    //===============//
    // Miscellaneous //
    //===============//
    public final MiscellaneousSettings miscellaneous = root.addValue("miscellaneous", new MiscellaneousSettings());
    public static class MiscellaneousSettings extends ObjectValue
    {
        public final BooleanValue showScathaProMenuButtons = addBoolean("showScathaProMenuButtons", true);
        // Chat stuff
        public final PrimitiveValueWithDefault<ChatPrefixType> chatPrefixType
            = addPrimitiveWithDefault("chatPrefixType", new EnumSerializer<>(ChatPrefixType.class), ChatPrefixType.FULL_NAME_BRACKETS);
        public final PrimitiveValueNullable<ChatCopyButtonMode> chatCopyButtonMode
            = addPrimitiveNullable("chatCopyButtonMode", new EnumSerializer<>(ChatCopyButtonMode.class));
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
            // note: sensitivity is 0-1 as % of the setting slider
            addPrimitiveWithDefault("alternativeSensitivity", FLOAT_SERIALIZER, 0f);
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
        // Scatha Drops Slot Machine
        public final BooleanValue dropsSlotMachineEnabled = addBoolean("scathaDropsSlotMachine.enabled", false);
        public final PrimitiveValueWithDefault<Integer> dropsSlotMachineAnimationTicks = addPrimitiveWithDefault("scathaDropsSlotMachine.animationDurationTicks", INTEGER_SERIALIZER, 120);
        public final PrimitiveValueWithDefault<Float> dropsSlotMachineScaleMultiplier = addPrimitiveWithDefault("scathaDropsSlotMachine.scaleMultiplier", FLOAT_SERIALIZER, 1f);
        public final PrimitiveValueWithDefault<MaxSlotMachineFakeScathaRarity> dropsSlotMachineMaxFakeScathaRarity
            = addPrimitiveWithDefault("scathaDropsSlotMachine.maxFakeScathaRarity", new EnumSerializer<>(MaxSlotMachineFakeScathaRarity.class), MaxSlotMachineFakeScathaRarity.LEGENDARY);
        public final BooleanValue dropsSlotMachineHidePetRarity = addBoolean("scathaDropsSlotMachine.hidePetRarity", false);
        public final BooleanValue dropsSlotMachineApplyRandomOffset = addBoolean("scathaDropsSlotMachine.applyRandomOffset", false);
        // Other
        public final BooleanValue aprilFoolsFakeDropEnabled = addBoolean("aprilFoolsFakeDropEnabled", true);
    }

    //===============//
    // Accessibility //
    //===============//
    public final AccessibilitySettings accessibility = root.addValue("accessibility", new AccessibilitySettings());
    public static class AccessibilitySettings extends ObjectValue
    {
        public final PrimitiveValueWithDefault<DateFormat> dateFormat
            = addPrimitiveWithDefault("dateFormat", new EnumSerializer<>(DateFormat.class), DateFormat.SYSTEM);
        public final PrimitiveValueWithDefault<TimeFormat> timeFormat
            = addPrimitiveWithDefault("timeFormat", new EnumSerializer<>(TimeFormat.class), TimeFormat.SYSTEM);
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



    public static final class ResolvableProfileValue implements JsonValue
    {
        @Nullable public ResolvableProfile value;

        @Override
        public void reset()
        {
            value = null;
        }

        @Override
        public boolean hasValue()
        {
            return value != null;
        }

        @Override
        public void loadFromJson(@Nullable JsonElement jsonElement)
        {
            if (!(jsonElement instanceof JsonObject object))
            {
                reset();
                return;
            }

            String name = JsonUtil.getString(object, "name");
            JsonPrimitive uuidPrimitive = JsonUtil.getJsonPrimitive(object, "uuid");
            String textures = JsonUtil.getString(object, "texturesBase64");

            if (name != null || uuidPrimitive != null || textures != null)
            {
                if (name == null) name = "";
                UUID uuid = uuidPrimitive != null ? UUID_SERIALIZER.jsonToValue(uuidPrimitive) : new UUID(0L, 0L);
                PropertyMap properties = textures != null
                    ? new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", textures)))
                    : PropertyMap.EMPTY;
                value = ResolvableProfile.createResolved(new GameProfile(uuid, name, properties));
            }
            else value = null;
        }

        @Override
        public @NonNull JsonElement getAsJson(@NonNull JsonFile<?> jsonFile)
        {
            JsonObject object = new JsonObject();
            if (value != null)
            {
                GameProfile profile = value.partialProfile();
                object.add("name", new JsonPrimitive(profile.name()));
                object.add("uuid", UUID_SERIALIZER.valueToJson(profile.id()));
                profile.properties().get("textures").stream()
                    .filter(property -> property.name().equals("textures"))
                    .findFirst().map(Property::value).ifPresent(
                        textures -> object.add("texturesBase64", new JsonPrimitive(textures))
                    );
            }
            return object;
        }

        public @Nullable GameProfile getResolved()
        {
            return value != null ? value.partialProfile() : null;
        }

        public void setResolved(@Nullable GameProfile partialProfile)
        {
            value = partialProfile != null ? ResolvableProfile.createResolved(partialProfile) : null;
        }
    }
}