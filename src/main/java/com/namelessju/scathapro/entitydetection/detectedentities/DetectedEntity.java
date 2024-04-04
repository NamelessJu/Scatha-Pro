package com.namelessju.scathapro.entitydetection.detectedentities;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.entitydetection.entitydetectors.EntityDetector;
import com.namelessju.scathapro.entitydetection.entitydetectors.GoblinDetector;
import com.namelessju.scathapro.entitydetection.entitydetectors.JerryDetector;
import com.namelessju.scathapro.entitydetection.entitydetectors.WormDetector;
import com.namelessju.scathapro.events.DetectedEntityRegisteredEvent;
import com.namelessju.scathapro.util.Util;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public abstract class DetectedEntity
{
    private static final EntityDetector[] ENTITY_DETECTORS = new EntityDetector[] {new WormDetector(), new GoblinDetector(), new JerryDetector()};
    
    
    // These have been detected and are remembered even if the actual entity is unloaded
    private static final HashMap<Integer, DetectedEntity> registeredEntities = new HashMap<Integer, DetectedEntity>();
    // These are currently loaded in the world
    private static final List<DetectedEntity> detectedEntities = new ArrayList<DetectedEntity>();
    
    public static void clearLists()
    {
        registeredEntities.clear();
        detectedEntities.clear();
    }
    
    public static DetectedEntity getById(int id)
    {
        for (int i = 0; i < detectedEntities.size(); i ++)
        {
            DetectedEntity entity = detectedEntities.get(i);
            if (entity.getEntity().getEntityId() == id) return entity;
        }
        return null;
    }
    
    public static void update(EntityPlayer player)
    {
        AxisAlignedBB playerPositionAABB = new AxisAlignedBB(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ);
        AxisAlignedBB entityDetectionAABB = playerPositionAABB.expand(20f, 5f, 20f);
        AxisAlignedBB killAABB = playerPositionAABB.expand(10f, 255f, 10f);
        
        // Remove detected entities when the entity isn't in the world anymore
        
        for (int i = detectedEntities.size() - 1; i >= 0; i --)
        {
            DetectedEntity detectedEntity = detectedEntities.get(i);
            
            if (detectedEntity.getEntity() == null)
            {
                detectedEntities.remove(i);
                continue;
            }
            
            if (!isInWorld(detectedEntity.entity, player.worldObj))
            {
                boolean despawned = false;
                
                if (registeredEntities.containsKey(detectedEntity.entity.getEntityId()))
                {
                    if (detectedEntity.getMaxLifetime() >= 0 && detectedEntity.getCurrentLifetime() >= detectedEntity.getMaxLifetime() - (long) Math.min(Constants.pingTreshold, detectedEntity.getMaxLifetime() * 0.2))
                    {
                        despawned = true;
                        registeredEntities.remove(detectedEntity.entity.getEntityId());
                        
                        ScathaPro.getInstance().logDebug("Entity \"" + detectedEntity.entity.getName() + "\" unloaded right before lifetime end, unregistered (" + registeredEntities.size() + " total)");
                    }
                    else if (killAABB.isVecInside(detectedEntity.entity.getPositionVector()))
                    {
                        registeredEntities.remove(detectedEntity.entity.getEntityId());
                        
                        ScathaPro.getInstance().logDebug("Entity \"" + detectedEntity.entity.getName() + "\" unloaded close to player, unregistered (" + registeredEntities.size() + " total)");
                    }
                    else
                    {
                        ScathaPro.getInstance().logDebug("Entity \"" + detectedEntity.entity.getName() + "\" unloaded away from player, was either killed or has left the simulation distance...");
                    }
                }
                
                detectedEntities.remove(i).onLeaveWorld(despawned);
                ScathaPro.getInstance().logDebug("Entity \"" + detectedEntity.entity.getName() + "\" is not in world anymore, removed from detected entities (" + detectedEntities.size() + " total)");
            }
        }
        
        // Remove registered entities when lifetime runs out
        
        Integer[] registeredEntityKeys = registeredEntities.keySet().toArray(new Integer[0]);
        for (int i = registeredEntityKeys.length - 1; i >= 0; i --)
        {
            Integer entityId = registeredEntityKeys[i];
            DetectedEntity detectedEntity = registeredEntities.get(entityId);
            
            if (!isInWorld(detectedEntity.entity, player.worldObj) && detectedEntity.getMaxLifetime() >= 0 && detectedEntity.getCurrentLifetime() >= detectedEntity.getMaxLifetime())
            {
                registeredEntities.remove(entityId);
                
                ScathaPro.getInstance().logDebug("Entity \"" + detectedEntity.entity.getName() + "\" lifetime ran out, unregistered (" + registeredEntities.size() + " total)");
            }
        }
        
        // Detect entities
        
        List<EntityArmorStand> nearbyEntities = player.worldObj.getEntitiesWithinAABB(EntityArmorStand.class, entityDetectionAABB);
        
        for (int i = 0; i < nearbyEntities.size(); i ++)
        {
            EntityArmorStand armorStand = nearbyEntities.get(i);
            
            if (DetectedEntity.getById(armorStand.getEntityId()) != null) continue;
            
            String entityName = armorStand.hasCustomName() ? StringUtils.stripControlCodes(armorStand.getCustomNameTag()) : "";
            for (EntityDetector detector : ENTITY_DETECTORS)
            {
                DetectedEntity detectedEntity = detector.detectEntity(armorStand, entityName);
                DetectedEntity.register(detectedEntity);
            }
        }
    }
    
    private static void register(DetectedEntity detectedEntity)
    {
        if (detectedEntity == null || detectedEntity.entity == null || detectedEntities.contains(detectedEntity)) return;
        
        detectedEntities.add(detectedEntity);
        
        ScathaPro.getInstance().logDebug("Entity \"" + detectedEntity.entity.getName() + "\" detected (" + detectedEntities.size() + " total)");
        
        int entityId = detectedEntity.entity.getEntityId();
        if (!registeredEntities.containsKey(entityId))
        {
            registeredEntities.put(entityId, detectedEntity);
            detectedEntity.onRegistration();
            
            ScathaPro.getInstance().logDebug("Entity \"" + detectedEntity.entity.getName() + "\" registered (" + registeredEntities.size() + " total)");
            
            MinecraftForge.EVENT_BUS.post(new DetectedEntityRegisteredEvent(detectedEntity));
        }
    }
    
    private static boolean isInWorld(EntityArmorStand entity, World world)
    {
        return world.loadedEntityList.contains(entity);
    }
    

    public final long spawnTime;
    private final EntityArmorStand entity;
    
    public DetectedEntity(EntityArmorStand entity)
    {
        this.entity = entity;
        
        int entityId = entity.getEntityId();
        
        DetectedEntity registeredEntity = registeredEntities.get(entityId);
        if (registeredEntity != null)
        {
            spawnTime = registeredEntity.spawnTime;
            registeredEntities.put(entityId, this);

            ScathaPro.getInstance().logDebug("Entity \"" + this.entity.getName() + "\" is already registered, replaced previous entity");
        }
        else
        {
            spawnTime = Util.getCurrentTime();
        }
    }
    
    public abstract long getMaxLifetime();
    
    protected void onRegistration() {}
    protected void onLeaveWorld(boolean despawned) {}
    
    
    public long getCurrentLifetime()
    {
        return Util.getCurrentTime() - spawnTime;
    }
    
    public EntityArmorStand getEntity()
    {
        return entity;
    }
}
