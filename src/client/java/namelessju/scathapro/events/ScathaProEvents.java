package namelessju.scathapro.events;

import namelessju.scathapro.achievements.UnlockedAchievement;
import namelessju.scathapro.entitydetection.detectedentity.DetectedEntity;
import namelessju.scathapro.entitydetection.detectedentity.DetectedWorm;
import namelessju.scathapro.gui.overlay.elements.OverlayDynamicContainer;
import namelessju.scathapro.miscellaneous.data.PetDrop;
import namelessju.scathapro.miscellaneous.data.enums.SkyblockArea;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ScathaProEvents
{
    // Minecraft events
    
    public static final DatalessEvent playerAddedToWorldEvent = new DatalessEvent();
    public static final Event<UseItemEventData> useItemEvent = new Event<>();
    public static final Event<AttackEntityEventData> attackEntityEvent = new Event<>();
    
    // Scatha-Pro events
    
    public static final Event<OverlayInitEventData> overlayInitEvent = new Event<>();
    public static final Event<NewModVersionUsedEventData> newModVersionUsedEvent = new Event<>();
    
    public static final DatalessEvent firstSessionIngameTickEvent = new DatalessEvent();
    public static final DatalessEvent firstLevelTickEvent = new DatalessEvent();
    
    public static final Event<CrystalHollowsTickEventData> crystalHollowsTickEvent = new Event<>();
    public static final Event<SkyblockAreaDetectedEventData> skyblockAreaDetectedEvent = new Event<>();
    public static final Event<CrystalHollowsDayStartedEventData> crystalHollowsDayStartedEvent = new Event<>();
    public static final DatalessEvent bedrockWallDetectedEvent = new DatalessEvent();
    public static final Event<DetectedEntityRegisteredEventData> detectedEntityRegisteredEvent = new Event<>();
    
    public static final DatalessEvent wormPreSpawnEvent = new DatalessEvent();
    public static final Event<WormEventData> wormSpawnEvent = new Event<>();
    public static final Event<WormHitEventData> wormHitEvent = new Event<>();
    public static final Event<WormKillEventData> wormKillEvent = new Event<>();
    public static final Event<WormEventData> wormDespawnEvent = new Event<>();
    public static final Event<ScathaPetDropEventData> scathaPetDropEvent = new Event<>();
    public static final Event<ScathaFarmingStreakChangedEventData> scathaFarmingStreakChangedEvent = new Event<>();
    
    public static final Event<AchievementUnlockedEventData> achievementUnlockedEvent = new Event<>();
    
    // Other events
    
    public static final DatalessEvent realDayStartedEvent = new DatalessEvent();
    
    
    
    // Data
    
    public record UseItemEventData(@NonNull LocalPlayer player, @NonNull ItemStack usedItem) {}
    public record AttackEntityEventData(@NonNull LocalPlayer player, @NonNull Entity entity, ItemStack attackItem) {}
    
    public record NewModVersionUsedEventData(@Nullable String previousVersion, @NonNull String newVersion) {}
    public record OverlayInitEventData(@NonNull OverlayDynamicContainer mainContainer) {}
    public record SkyblockAreaDetectedEventData(@NonNull SkyblockArea area) {}
    public record CrystalHollowsTickEventData(boolean isFirstTick) {}
    public record CrystalHollowsDayStartedEventData(int day) {}
    public record DetectedEntityRegisteredEventData(@NonNull DetectedEntity entity) {}
    public record WormEventData(@NonNull DetectedWorm worm) {}
    public record WormHitEventData(@NonNull DetectedWorm worm, @Nullable ItemStack weapon) {}
    public record WormKillEventData(@NonNull DetectedWorm worm, boolean wasBlackHoled) {}
    public record ScathaPetDropEventData(@NonNull PetDrop petDrop) {}
    public record ScathaFarmingStreakChangedEventData(int streak, int highscore) {}
    public record AchievementUnlockedEventData(UnlockedAchievement unlockedAchievement) {}
}