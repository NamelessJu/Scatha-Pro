package namelessju.scathapro.managers;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.entitydetection.detectedentity.DetectedEntity;
import namelessju.scathapro.entitydetection.entitydetector.EntityDetector;
import namelessju.scathapro.entitydetection.entitydetector.GoblinDetector;
import namelessju.scathapro.entitydetection.entitydetector.JerryDetector;
import namelessju.scathapro.entitydetection.entitydetector.WormDetector;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.util.TextUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntityDetectionManager
{
    private static final EntityDetector[] ENTITY_DETECTORS = new EntityDetector[] {
        new WormDetector(),
        new GoblinDetector(),
        new JerryDetector()
    };
    
    private final ScathaPro scathaPro;
    
    /** These have been detected and are remembered even if the actual entity is unloaded */
    private final HashMap<Integer, DetectedEntity> registeredEntities = new HashMap<>();
    /** These are currently loaded in the world */
    private final List<DetectedEntity> activeEntities = new ArrayList<>();
    
    public EntityDetectionManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void clearLists()
    {
        registeredEntities.clear();
        activeEntities.clear();
    }
    
    public @Nullable DetectedEntity getById(int id)
    {
        for (DetectedEntity entity : activeEntities)
        {
            if (entity.entity.getId() == id) return entity;
        }
        return null;
    }
    
    public void tick(LocalPlayer player)
    {
        AABB entityDetectionAABB = AABB.ofSize(player.position(), 60, 10, 60);
        AABB killAABB = AABB.ofSize(player.position(), 20, 255, 20);
        
        // Remove detected entities when the entity isn't in the world anymore
        for (int i = activeEntities.size() - 1; i >= 0; i --)
        {
            DetectedEntity detectedEntity = activeEntities.get(i);
            if (!detectedEntity.entity.isRemoved()) continue;
            
            DetectedEntity.LeaveWorldReason leaveWorldReason = null;
            
            if (registeredEntities.containsKey(detectedEntity.entity.getId()))
            {
                if (detectedEntity.getMaxLifetime() >= 0 && detectedEntity.getCurrentLifetime() >= detectedEntity.getMaxLifetime() - (long) Math.min(Constants.pingTreshold, detectedEntity.getMaxLifetime() * 0.2))
                {
                    registeredEntities.remove(detectedEntity.entity.getId());
                    
                    leaveWorldReason = DetectedEntity.LeaveWorldReason.LIFETIME_ENDED;
                    if (ScathaPro.LOGGER.isDebugEnabled()) ScathaPro.LOGGER.debug(
                        "Entity {} unloaded right before lifetime end, unregistered ({} total)",
                        getEntityString(detectedEntity), registeredEntities.size()
                    );
                }
                else if (killAABB.contains(detectedEntity.entity.position()))
                {
                    registeredEntities.remove(detectedEntity.entity.getId());
                    
                    leaveWorldReason = DetectedEntity.LeaveWorldReason.KILLED;
                    if (ScathaPro.LOGGER.isDebugEnabled()) ScathaPro.LOGGER.debug(
                        "Entity {} unloaded close to player, unregistered ({} total)",
                        getEntityString(detectedEntity), registeredEntities.size()
                    );
                }
                else
                {
                    leaveWorldReason = DetectedEntity.LeaveWorldReason.LEFT_SIMULATION_DISTANCE;
                    if (ScathaPro.LOGGER.isDebugEnabled()) ScathaPro.LOGGER.debug(
                        "Entity {} unloaded away from player, has likely left the simulation distance...",
                        getEntityString(detectedEntity)
                    );
                }
            }
            
            activeEntities.remove(i).onLeaveWorld(leaveWorldReason, player);
            if (ScathaPro.LOGGER.isDebugEnabled()) ScathaPro.LOGGER.debug(
                "Entity {} is not in world anymore, removed from active entities ({} total)",
                getEntityString(detectedEntity), activeEntities.size()
            );
        }
        
        // Remove registered entities when lifetime runs out
        Integer[] registeredEntityKeys = registeredEntities.keySet().toArray(new Integer[0]);
        for (int i = registeredEntityKeys.length - 1; i >= 0; i --)
        {
            Integer entityId = registeredEntityKeys[i];
            DetectedEntity detectedEntity = registeredEntities.get(entityId);
            
            if (detectedEntity.entity.isRemoved() && detectedEntity.getMaxLifetime() >= 0
                && detectedEntity.getCurrentLifetime() >= detectedEntity.getMaxLifetime())
            {
                registeredEntities.remove(entityId);
                if (ScathaPro.LOGGER.isDebugEnabled()) ScathaPro.LOGGER.debug(
                    "Entity {} lifetime ran out, unregistered ({} total)",
                    getEntityString(detectedEntity), registeredEntities.size()
                );
            }
        }
        
        // Detect entities
        List<ArmorStand> nearbyEntities = player.level().getEntitiesOfClass(ArmorStand.class, entityDetectionAABB);
        for (ArmorStand armorStand : nearbyEntities)
        {
            if (getById(armorStand.getId()) != null) continue;
            
            Component entityName = armorStand.getCustomName();
            String entityNameUnformatted = entityName != null ? StringDecomposer.getPlainText(entityName) : null;
            for (EntityDetector detector : ENTITY_DETECTORS)
            {
                DetectedEntity detectedEntity = detector.detectEntity(scathaPro, armorStand, entityNameUnformatted);
                if (detectedEntity != null)
                {
                    DetectedEntity registeredEntity = registeredEntities.get(detectedEntity.entity.getId());
                    if (registeredEntity != null) update(registeredEntity, detectedEntity);
                    else register(detectedEntity);
                    break;
                }
            }
        }
    }
    
    private void register(DetectedEntity detectedEntity)
    {
        if (detectedEntity == null || activeEntities.contains(detectedEntity)) return;
        
        activeEntities.add(detectedEntity);
        if (ScathaPro.LOGGER.isDebugEnabled()) ScathaPro.LOGGER.debug(
            "Entity {} detected ({} total)", getEntityString(detectedEntity), activeEntities.size()
        );
        
        int entityId = detectedEntity.entity.getId();
        if (!registeredEntities.containsKey(entityId))
        {
            registeredEntities.put(entityId, detectedEntity);
            detectedEntity.onRegistration();
            ScathaProEvents.detectedEntityRegisteredEvent.trigger(scathaPro,
                new ScathaProEvents.DetectedEntityRegisteredEventData(detectedEntity)
            );
            if (ScathaPro.LOGGER.isDebugEnabled()) ScathaPro.LOGGER.debug(
                "Entity {} registered ({} total)", getEntityString(detectedEntity), registeredEntities.size()
            );
        }
    }
    
    private void update(DetectedEntity oldEntity, DetectedEntity newEntity)
    {
        oldEntity.entity = newEntity.entity;
        oldEntity.onChangedEntity();
        
        activeEntities.add(oldEntity);
        
        registeredEntities.remove(oldEntity.entity.getId());
        registeredEntities.put(newEntity.entity.getId(), oldEntity);
        
        if (ScathaPro.LOGGER.isDebugEnabled()) ScathaPro.LOGGER.debug(
            "Entity {} is already registered, replaced previous entity {}",
            getEntityString(oldEntity), getEntityString(newEntity)
        );
    }
    
    private static String getEntityString(DetectedEntity detectedEntity)
    {
        return "\"" + StringDecomposer.getPlainText(detectedEntity.entity.getName()) + "\" (" + detectedEntity.entity.getId() + ")";
    }
}
