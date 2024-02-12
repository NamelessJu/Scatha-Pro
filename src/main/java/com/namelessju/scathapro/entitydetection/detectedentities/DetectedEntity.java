package com.namelessju.scathapro.entitydetection.detectedentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.events.DetectedEntityRegisteredEvent;
import com.namelessju.scathapro.util.Util;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public abstract class DetectedEntity
{
    // These have been detected and are remembered even if the actual entity is unloaded
    protected static final Hashtable<Integer, Long> registeredEntities = new Hashtable<Integer, Long>();
    // These are currently loaded in the world
    private static final List<DetectedEntity> detectedEntities = new ArrayList<DetectedEntity>();
    
    public static void clearLists()
    {
        registeredEntities.clear();
        detectedEntities.clear();
    }
    
    public static void register(DetectedEntity detectedEntity)
    {
        if (detectedEntity == null || detectedEntity.entity == null || detectedEntities.contains(detectedEntity)) return;
        
        detectedEntities.add(detectedEntity);
        
        int entityId = detectedEntity.entity.getEntityId();
        if (!registeredEntities.contains(entityId))
        {
            registeredEntities.put(entityId, detectedEntity.spawnTime);
            detectedEntity.onRegistration();
            MinecraftForge.EVENT_BUS.post(new DetectedEntityRegisteredEvent(detectedEntity));
        }
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
    
    public static void update(World world)
    {
        for (int i = detectedEntities.size() - 1; i >= 0; i --)
        {
            DetectedEntity detectedEntity = detectedEntities.get(i);
            
            if (detectedEntity.getEntity() == null)
            {
                detectedEntities.remove(i);
                continue;
            }
            
            if (!world.loadedEntityList.contains(detectedEntity.entity))
            {
                if (detectedEntity.getCurrentLifetime() >= detectedEntity.getMaxLifetime() - Constants.pingTreshold)
                {
                    registeredEntities.remove(detectedEntity.entity.getEntityId());
                    detectedEntity.onDespawn();
                }
                
                detectedEntities.remove(i).onRemoved();
            }
        }
    }
    

    public final long spawnTime;
    private final EntityArmorStand entity;
    
    public DetectedEntity(EntityArmorStand entity)
    {
        this.entity = entity;
        
        Long registeredSpawnTime = registeredEntities.get(entity.getEntityId());
        if (registeredSpawnTime != null) spawnTime = registeredSpawnTime;
        else spawnTime = Util.getCurrentTime();
    }
    
    public abstract long getMaxLifetime();
    
    protected void onRegistration() {}
    protected void onRemoved() {}
    protected void onDespawn() {}
    
    
    public long getCurrentLifetime()
    {
        return Util.getCurrentTime() - spawnTime;
    }
    
    public EntityArmorStand getEntity()
    {
        return entity;
    }
}
