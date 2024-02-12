package com.namelessju.scathapro.entitydetection.detectedentities;

import java.util.ArrayList;

import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.events.WormDespawnEvent;
import com.namelessju.scathapro.events.WormHitEvent;
import com.namelessju.scathapro.events.WormKillEvent;
import com.namelessju.scathapro.events.WormSpawnEvent;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

public class DetectedWorm extends DetectedEntity
{
    public static DetectedWorm getById(int id)
    {
        DetectedEntity detectedEntity = DetectedEntity.getById(id);
        if (detectedEntity instanceof DetectedWorm)
        {
            return (DetectedWorm) detectedEntity;
        }
        return null;
    }
    
    
    public final boolean isScatha;
    private long lastAttackTime = -1;
    private long lastFireAspectAttackTime = -1;
    private int lastFireAspectLevel = 0;
    
    private ArrayList<String> hitWeapons = new ArrayList<String>();
    
    public DetectedWorm(EntityArmorStand entity, boolean isScatha)
    {
        super(entity);
        this.isScatha = isScatha;
    }

    @Override
    public long getMaxLifetime()
    {
        return Constants.wormLifetime;
    }

    @Override
    protected void onRegistration()
    {
        MinecraftForge.EVENT_BUS.post(new WormSpawnEvent(this));
    }
    
    @Override
    protected void onRemoved()
    {
        long now = Util.getCurrentTime();
        /*
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        AxisAlignedBB wormDetectionAABB = new AxisAlignedBB(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ).expand(20f, 10f, 20f);
        && getEntity().getEntityBoundingBox().intersectsWith(wormDetectionAABB)
        */
        
        int entityID = getEntity().getEntityId();
        
        // Kill
        if (getLastAttackTime() >= 0 && now - getLastAttackTime() < Constants.pingTreshold || isFireAspectActive())
        {
            registeredEntities.remove((Integer) entityID);
            MinecraftForge.EVENT_BUS.post(new WormKillEvent(this));
        }
    }
    
    @Override
    protected void onDespawn()
    {
        MinecraftForge.EVENT_BUS.post(new WormDespawnEvent(this));
    }
    
    public void attack(ItemStack weapon)
    {
        long now = Util.getCurrentTime();
        lastAttackTime = now;

        String skyblockItemID = NBTUtil.getSkyblockItemID(weapon);
        if (skyblockItemID != null && !hitWeapons.contains(skyblockItemID))
        {
            hitWeapons.add(skyblockItemID);
        }

        NBTTagCompound enchantments = NBTUtil.getSkyblockTagCompound(weapon, "enchantments");
        if (enchantments != null)
        {
            lastFireAspectLevel = enchantments.getInteger("fire_aspect");
            if (lastFireAspectLevel > 0) lastFireAspectAttackTime = now;
        }
        
        MinecraftForge.EVENT_BUS.post(new WormHitEvent(this, weapon));
    }
    
    public long getLastAttackTime()
    {
        return lastAttackTime;
    }
    
    public String[] getHitWeapons()
    {
        return hitWeapons.toArray(new String[] {});
    }
    
    public int getHitWeaponsCount()
    {
        return hitWeapons.size();
    }
    
    public boolean isFireAspectActive()
    {
        if (lastFireAspectLevel > 0)
        {
            float fireAspectDuration = 0f;
            
            switch (lastFireAspectLevel)
            {
                case 1:
                    fireAspectDuration = 3f;
                    break;
                case 2:
                case 3:
                    fireAspectDuration = 4f;
                    break;
            }
            
            return Util.getCurrentTime() - lastFireAspectAttackTime <= fireAspectDuration * 1000f + Constants.pingTreshold;
        }
        return false;
    }
}
