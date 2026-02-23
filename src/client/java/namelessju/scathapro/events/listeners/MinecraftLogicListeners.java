package namelessju.scathapro.events.listeners;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.entitydetection.detectedentity.DetectedWorm;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class MinecraftLogicListeners
{
    private MinecraftLogicListeners() {}
    
    public static void register()
    {
        ScathaProEvents.playerAddedToWorldEvent.addListener(MinecraftLogicListeners::onPlayerAddedToWorld);
        ScathaProEvents.useItemEvent.addListener(MinecraftLogicListeners::onUseItem);
        ScathaProEvents.attackEntityEvent.addListener(MinecraftLogicListeners::onAttackEntity);
    }
    
    private static void onPlayerAddedToWorld(ScathaPro scathaPro)
    {
        scathaPro.persistentDataProfileManager.updateCurrentPlayerProfile();
        
        // Reset
        
        scathaPro.entityDetectionManager.clearLists();
        
        scathaPro.coreManager.lastWorldJoinTime = TimeUtil.now();
        scathaPro.coreManager.resetForNewLobby();
        
        scathaPro.inputManager.disableCameraRotationLock();
        
        // Update overlay
        
        scathaPro.mainOverlay.updateAll();
        
        // Update achievements
        
        scathaPro.achievementLogicManager.updateKillsAchievements();
        scathaPro.achievementLogicManager.updateSpawnAchievements(null);
        
        Achievement.crystal_hollows_time_1.setProgress(0);
        Achievement.crystal_hollows_time_2.setProgress(0);
        Achievement.crystal_hollows_time_3.setProgress(0);
        
        ScathaPro.LOGGER.debug("Player added to world");
    }
    
    private static void onUseItem(ScathaPro scathaPro, ScathaProEvents.UseItemEventData data)
    {
        if (scathaPro.coreManager.isInCrystalHollows())
        {
            Item item = data.usedItem().getItem();
            if (item == Items.FISHING_ROD || item == Items.BOW) // TODO: does this make sense?
            {
                scathaPro.coreManager.lastProjectileWeaponUsed = data.usedItem();
                ScathaPro.LOGGER.debug("Projectile weapon {} used", item);
            }
        }
    }
    
    private static void onAttackEntity(ScathaPro scathaPro, ScathaProEvents.AttackEntityEventData data)
    {
        // Worm melee attack detection
        
        if (!(data.entity() instanceof ArmorStand attackedArmorStand)) return;
        
        DetectedWorm attackedWorm = null;
        
        // Dev mode: Check for attacked armor stand being the attackedWorm itself
        if (scathaPro.config.dev.devModeEnabled.get()
            && scathaPro.entityDetectionManager.getById(attackedArmorStand.getId()) instanceof DetectedWorm worm)
        {
            attackedWorm = worm;
        }
        
        // Check for main worm armor stand (= name tag armor stand) nearby
        if (attackedWorm == null && Constants.isWormSkull(attackedArmorStand.getItemBySlot(EquipmentSlot.HEAD)))
        {
            Level level = attackedArmorStand.level();
            List<ArmorStand> nearbyArmorStands = level.getEntitiesOfClass(ArmorStand.class,
                AABB.ofSize(attackedArmorStand.position(), 16, 4, 16),
                armorStand -> armorStand != attackedArmorStand
            );
            
            for (ArmorStand armorStand : nearbyArmorStands)
            {
                if (scathaPro.entityDetectionManager.getById(armorStand.getId()) instanceof DetectedWorm worm)
                {
                    attackedWorm = worm;
                    break;
                }
            }
        }
        
        if (attackedWorm != null)
        {
            attackedWorm.attack(data.attackItem(), attackedArmorStand);
            ScathaPro.LOGGER.debug("Attacked worm entity");
        }
    }
}
