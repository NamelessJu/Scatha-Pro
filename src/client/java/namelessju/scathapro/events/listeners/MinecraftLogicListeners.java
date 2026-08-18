package namelessju.scathapro.events.listeners;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.managers.detectors.entities.detected.DetectedWorm;
import namelessju.scathapro.miscellaneous.data.mixindata.IWormArmorStandData;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.world.entity.decoration.ArmorStand;

public final class MinecraftLogicListeners
{
    private MinecraftLogicListeners() {}

    public static void register()
    {
        ScathaProEvents.playerAddedToWorldEvent.addListener(MinecraftLogicListeners::onPlayerAddedToWorld);
        ScathaProEvents.worldLeftEvent.addListener(MinecraftLogicListeners::onWorldLeft);
        ScathaProEvents.useItemEvent.addListener(MinecraftLogicListeners::onUseItem);
        ScathaProEvents.attackEntityEvent.addListener(MinecraftLogicListeners::onAttackEntity);
    }

    private static void onPlayerAddedToWorld(ScathaPro scathaPro)
    {
        // Update in case account was switched by an ingame account manager mod
        scathaPro.persistentDataProfileManager.updateCurrentPlayerProfile();

        // Reset

        scathaPro.coreManager.lastWorldJoinTime = TimeUtil.getEpochMilliseconds();
        scathaPro.coreManager.resetForNewLobby();
        scathaPro.inputManager.disableCameraRotationLock();

        // Update overlay

        scathaPro.mainOverlay.setShown(false);
        scathaPro.mainOverlay.updateAll();

        // Update achievements

        scathaPro.achievementLogicManager.updateKillsAchievements();
        scathaPro.achievementLogicManager.updateSpawnAchievements();

        Achievement.crystal_hollows_time_1.setProgress(0);
        Achievement.crystal_hollows_time_2.setProgress(0);
        Achievement.crystal_hollows_time_3.setProgress(0);

        ScathaPro.LOGGER.debug("Player added to world");
    }

    private static void onWorldLeft(ScathaPro scathaPro)
    {
        scathaPro.scathaDropsSlotMachineManager.reset();
        scathaPro.secondaryStatsManager.perLobbyStats.reset();

        ScathaPro.LOGGER.debug("Left world");
    }

    private static void onUseItem(ScathaProEvents.UseItemEventData data)
    {
        data.scathaPro().coreManager.projectileWormHitDetector.checkItemUsed(data.usedItem());
        data.scathaPro().coreManager.entityDetectionManager.checkItemUsed(data.usedItem());
    }

    private static void onAttackEntity(ScathaProEvents.AttackEntityEventData data)
    {
        ScathaPro scathaPro = data.scathaPro();

        // Worm melee attack detection

        if (!(data.entity() instanceof ArmorStand attackedArmorStand)) return;
        DetectedWorm attackedWorm = null;

        // Dev mode: Check for attacked armor stand being the worm itself
        if (scathaPro.config.dev.devModeEnabled.get()
            && scathaPro.coreManager.entityDetectionManager.getDetectedEntityById(attackedArmorStand.getId()) instanceof DetectedWorm worm)
        {
            attackedWorm = worm;
        }

        // Get worm reference from armor stand mixin
        if (attackedWorm == null)
        {
            attackedWorm = ((IWormArmorStandData) attackedArmorStand).scathapro$getWorm();
        }

        if (attackedWorm != null)
        {
            attackedWorm.attack(data.attackItem(), attackedArmorStand);
            ScathaPro.LOGGER.debug("Attacked worm entity");
        }
    }
}