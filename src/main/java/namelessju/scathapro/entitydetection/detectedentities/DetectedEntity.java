package namelessju.scathapro.entitydetection.detectedentities;

import java.util.ArrayList;
import java.util.List;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.entitydetection.entitydetectors.EntityDetector;
import namelessju.scathapro.entitydetection.entitydetectors.GoblinDetector;
import namelessju.scathapro.entitydetection.entitydetectors.JerryDetector;
import namelessju.scathapro.entitydetection.entitydetectors.WormDetector;
import namelessju.scathapro.events.DetectedEntityRegisteredEvent;
import namelessju.scathapro.util.TimeUtil;

import java.util.HashMap;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public abstract class DetectedEntity
{
    private static final EntityDetector[] ENTITY_DETECTORS = new EntityDetector[] {
        new WormDetector(),
        new GoblinDetector(),
        new JerryDetector()
    };
    
    
    /** These have been detected and are remembered even if the actual entity is unloaded */
    private static final HashMap<Integer, DetectedEntity> registeredEntities = new HashMap<Integer, DetectedEntity>();
    /** These are currently loaded in the world */
    private static final List<DetectedEntity> activeEntities = new ArrayList<DetectedEntity>();
    
    public static void clearLists()
    {
        registeredEntities.clear();
        activeEntities.clear();
    }
    
    public static DetectedEntity getById(int id)
    {
        for (int i = 0; i < activeEntities.size(); i ++)
        {
            DetectedEntity entity = activeEntities.get(i);
            if (entity.getEntity().getEntityId() == id) return entity;
        }
        return null;
    }
    
    public static void update(EntityPlayer player)
    {
        AxisAlignedBB playerPositionAABB = new AxisAlignedBB(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ);
        AxisAlignedBB entityDetectionAABB = playerPositionAABB.expand(30f, 5f, 30f);
        AxisAlignedBB killAABB = playerPositionAABB.expand(10f, 255f, 10f);
        
        // Remove detected entities when the entity isn't in the world anymore
        
        for (int i = activeEntities.size() - 1; i >= 0; i --)
        {
            DetectedEntity detectedEntity = activeEntities.get(i);
            
            if (detectedEntity.getEntity() == null)
            {
                activeEntities.remove(i);
                continue;
            }
            
            if (!isInWorld(detectedEntity.entity, player.worldObj))
            {
                LeaveWorldReason leaveWorldReason = null;
                
                if (registeredEntities.containsKey(detectedEntity.entity.getEntityId()))
                {
                    if (detectedEntity.getMaxLifetime() >= 0 && detectedEntity.getCurrentLifetime() >= detectedEntity.getMaxLifetime() - (long) Math.min(Constants.pingTreshold, detectedEntity.getMaxLifetime() * 0.2))
                    {
                        registeredEntities.remove(detectedEntity.entity.getEntityId());

                        leaveWorldReason = LeaveWorldReason.LIFETIME_ENDED;
                        ScathaPro.getInstance().logDebug("Entity " + getEntityString(detectedEntity) + " unloaded right before lifetime end, unregistered (" + registeredEntities.size() + " total)");
                    }
                    else if (killAABB.isVecInside(detectedEntity.entity.getPositionVector()))
                    {
                        registeredEntities.remove(detectedEntity.entity.getEntityId());

                        leaveWorldReason = LeaveWorldReason.KILLED;
                        ScathaPro.getInstance().logDebug("Entity " + getEntityString(detectedEntity) + " unloaded close to player, unregistered (" + registeredEntities.size() + " total)");
                    }
                    else
                    {
                        leaveWorldReason = LeaveWorldReason.LEFT_SIMULATION_DISTANCE;
                        ScathaPro.getInstance().logDebug("Entity " + getEntityString(detectedEntity) + " unloaded away from player, has likely left the simulation distance...");
                    }
                }
                
                activeEntities.remove(i).onLeaveWorld(leaveWorldReason);
                ScathaPro.getInstance().logDebug("Entity " + getEntityString(detectedEntity) + " is not in world anymore, removed from active entities (" + activeEntities.size() + " total)");
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
                
                ScathaPro.getInstance().logDebug("Entity " + getEntityString(detectedEntity) + " lifetime ran out, unregistered (" + registeredEntities.size() + " total)");
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
                if (detectedEntity != null)
                {
                    DetectedEntity registeredEntity = registeredEntities.get(detectedEntity.entity.getEntityId());
                    if (registeredEntity != null) update(registeredEntity, detectedEntity);
                    else DetectedEntity.register(detectedEntity);
                    break;
                }
            }
        }
    }
    
    private static void register(DetectedEntity detectedEntity)
    {
        if (detectedEntity == null || detectedEntity.entity == null || activeEntities.contains(detectedEntity)) return;
        
        activeEntities.add(detectedEntity);
        
        ScathaPro.getInstance().logDebug("Entity " + getEntityString(detectedEntity) + " detected (" + activeEntities.size() + " total)");
        
        int entityId = detectedEntity.entity.getEntityId();
        if (!registeredEntities.containsKey(entityId))
        {
            registeredEntities.put(entityId, detectedEntity);
            detectedEntity.onRegistration();
            
            ScathaPro.getInstance().logDebug("Entity " + getEntityString(detectedEntity) + " registered (" + registeredEntities.size() + " total)");
            
            MinecraftForge.EVENT_BUS.post(new DetectedEntityRegisteredEvent(detectedEntity));
        }
    }
    
    private static void update(DetectedEntity oldEntity, DetectedEntity newEntity)
    {
        oldEntity.entity = newEntity.entity;
        oldEntity.onChangedEntity();
        
        activeEntities.add(oldEntity);
        
        registeredEntities.remove(oldEntity.entity.getEntityId());
        registeredEntities.put(newEntity.entity.getEntityId(), oldEntity);
        
        ScathaPro.getInstance().logDebug("Entity " + getEntityString(oldEntity) + " is already registered, replaced previous entity " + getEntityString(newEntity));
    }
    
    private static boolean isInWorld(EntityArmorStand entity, World world)
    {
        return world.loadedEntityList.contains(entity);
    }
    
    private static String getEntityString(DetectedEntity detectedEntity)
    {
        return "\"" + detectedEntity.entity.getName() + "\" (" + detectedEntity.entity.getEntityId() + ")";
    }
    

    public final long spawnTime;
    private EntityArmorStand entity;
    
    public DetectedEntity(EntityArmorStand entity)
    {
        this.entity = entity;
        
        spawnTime = TimeUtil.now();
    }
    
    public abstract long getMaxLifetime();
    
    protected void onRegistration() {}
    protected void onChangedEntity() {}
    protected void onLeaveWorld(LeaveWorldReason leaveWorldReason) {}
    
    
    public long getCurrentLifetime()
    {
        return TimeUtil.now() - spawnTime;
    }
    
    public EntityArmorStand getEntity()
    {
        return entity;
    }
    
    
    public static enum LeaveWorldReason
    {
        LIFETIME_ENDED, KILLED, LEFT_SIMULATION_DISTANCE
    }
}
