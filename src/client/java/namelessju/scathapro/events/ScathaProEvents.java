package namelessju.scathapro.events;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.UnlockedAchievement;
import namelessju.scathapro.events.framework.DataEvent;
import namelessju.scathapro.gui.overlay.elements.OverlayDynamicContainer;
import namelessju.scathapro.managers.detectors.entities.detected.DetectedEntity;
import namelessju.scathapro.managers.detectors.entities.detected.DetectedWorm;
import namelessju.scathapro.miscellaneous.data.ScathaPetDrop;
import namelessju.scathapro.miscellaneous.data.enums.SkyBlockArea;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ScathaProEvents
{
    // Minecraft events

    public static final DataEvent<ScathaPro> playerAddedToWorldEvent = new DataEvent<>();
    public static final DataEvent<ScathaPro> worldLeftEvent = new DataEvent<>();
    public static final DataEvent<UseItemEventData> useItemEvent = new DataEvent<>();
    public static final DataEvent<AttackEntityEventData> attackEntityEvent = new DataEvent<>();

    // Scatha-Pro events

    public static final DataEvent<OverlayInitEventData> overlayInitEvent = new DataEvent<>();
    public static final DataEvent<NewModVersionUsedEventData> newModVersionUsedEvent = new DataEvent<>();

    public static final DataEvent<ScathaPro> firstSessionIngameTickEvent = new DataEvent<>();
    public static final DataEvent<ScathaPro> firstLevelTickEvent = new DataEvent<>();

    public static final DataEvent<CrystalHollowsTickEventData> crystalHollowsTickEvent = new DataEvent<>();
    public static final DataEvent<SkyBlockAreaDetectedEventData> skyBlockAreaDetectedEvent = new DataEvent<>();
    public static final DataEvent<CrystalHollowsDayStartedEventData> crystalHollowsDayStartedEvent = new DataEvent<>();
    public static final DataEvent<ScathaPro> bedrockWallDetectedEvent = new DataEvent<>();
    public static final DataEvent<DetectedEntityRegisteredEventData> detectedEntityRegisteredEvent = new DataEvent<>();

    public static final DataEvent<ScathaPro> wormPreSpawnEvent = new DataEvent<>();
    public static final DataEvent<WormEventData> wormSpawnEvent = new DataEvent<>();
    public static final DataEvent<WormHitEventData> wormHitEvent = new DataEvent<>();
    public static final DataEvent<WormKillEventData> wormKillEvent = new DataEvent<>();
    public static final DataEvent<WormEventData> wormDespawnEvent = new DataEvent<>();
    public static final DataEvent<ScathaExtraDropEventData> scathaExtraItemDropEvent = new DataEvent<>();
    public static final DataEvent<ScathaPetDropEventData> scathaPetDropEvent = new DataEvent<>();
    public static final DataEvent<ScathaFarmingStreakChangedEventData> scathaFarmingStreakChangedEvent = new DataEvent<>();

    public static final DataEvent<AchievementUnlockedEventData> achievementUnlockedEvent = new DataEvent<>();

    // Other events

    public static final DataEvent<ScathaPro> realDayStartedEvent = new DataEvent<>();



    // Data

    public record UseItemEventData(ScathaPro scathaPro, LocalPlayer player, ItemStack usedItem) {}
    public record AttackEntityEventData(ScathaPro scathaPro, LocalPlayer player, Entity entity, ItemStack attackItem) {}

    public record NewModVersionUsedEventData(ScathaPro scathaPro, @Nullable String previousVersion, String newVersion) {}
    public record OverlayInitEventData(ScathaPro scathaPro, OverlayDynamicContainer mainContainer) {}
    public record SkyBlockAreaDetectedEventData(ScathaPro scathaPro, SkyBlockArea area) {}
    public record CrystalHollowsTickEventData(ScathaPro scathaPro, boolean isFirstTick) {}
    public record CrystalHollowsDayStartedEventData(ScathaPro scathaPro, int day) {}
    public record DetectedEntityRegisteredEventData(ScathaPro scathaPro, DetectedEntity entity) {}
    public record WormEventData(ScathaPro scathaPro, DetectedWorm worm) {}
    public record WormHitEventData(ScathaPro scathaPro, DetectedWorm worm, @Nullable ItemStack weapon) {}
    public record WormKillEventData(ScathaPro scathaPro, DetectedWorm worm, boolean wasBlackHoled) {}
    public record ScathaExtraDropEventData(ScathaPro scathaPro, ItemType itemType)
    {
        public enum ItemType { DWARVEN_OS_BLOCK_BRAN }
    }
    public record ScathaPetDropEventData(ScathaPro scathaPro, ScathaPetDrop scathaPetDrop) {}
    public record ScathaFarmingStreakChangedEventData(ScathaPro scathaPro, int streak, int highScore) {}
    public record AchievementUnlockedEventData(ScathaPro scathaPro, UnlockedAchievement unlockedAchievement) {}
}