package com.namelessju.scathapro.entitydetection.detectedentities;

import java.util.ArrayList;

import com.google.common.base.Predicate;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.events.WormDespawnEvent;
import com.namelessju.scathapro.events.WormHitEvent;
import com.namelessju.scathapro.events.WormKillEvent;
import com.namelessju.scathapro.events.WormSpawnEvent;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.TimeUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
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
    
    public boolean lootsharePossible = false;
    
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
    protected void onLeaveWorld(boolean despawned)
    {
        ScathaPro sp = ScathaPro.getInstance();
        
        // check for direct kill
        boolean countAsKilled = getLastAttackTime() >= 0 && TimeUtil.now() - getLastAttackTime() < Constants.pingTreshold;
        
        if (!countAsKilled) // check for kill by fire aspect
        {
            countAsKilled = isFireAspectActive() && (getMaxLifetime() < 0 || getCurrentLifetime() < getMaxLifetime());
        }
        if (!countAsKilled && this.lootsharePossible) // check for lootshare
        {
            EntityPlayer player = sp.getMinecraft().thePlayer;
            AxisAlignedBB playerDetectionAABB = Util.getEntityPositionAABB(this.getEntity()).expand(30f, 30f, 30f);
            
            if (Util.getEntityPositionAABB(player).intersectsWith(playerDetectionAABB))
            {
                int nearbyOtherPlayerCount =  sp.getMinecraft().theWorld.getEntitiesInAABBexcluding(player, playerDetectionAABB, new Predicate<Entity>() {
                    @Override
                    public boolean apply(Entity input)
                    {
                        return input instanceof EntityPlayer;
                    }
                }).size();
                
                if (nearbyOtherPlayerCount > 0)
                {
                    sp.logDebug("Worm treated as lootshared");
                    countAsKilled = true;
                }
            }
        }
        
        if (countAsKilled)
        {
            sp.logDebug("Worm left world, counted as kill");
            MinecraftForge.EVENT_BUS.post(new WormKillEvent(this));
        }
        else if (despawned)
        {
            sp.logDebug("Worm left world, counted as despawn");
            MinecraftForge.EVENT_BUS.post(new WormDespawnEvent(this));
        }
    }
    
    public void attack(ItemStack weapon)
    {
        long now = TimeUtil.now();
        lastAttackTime = now;
        
        String skyblockItemID = NBTUtil.getSkyblockItemID(weapon);
        if (skyblockItemID != null)
        {
            hitWeapons.remove(skyblockItemID);
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
            
            return TimeUtil.now() - lastFireAspectAttackTime <= fireAspectDuration * 1000f + Constants.pingTreshold;
        }
        return false;
    }
}
