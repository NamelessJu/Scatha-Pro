package com.namelessju.scathapro.objects;

import java.util.ArrayList;
import java.util.List;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.events.WormHitEvent;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

public class Worm {
    
    public final EntityArmorStand armorStand;
	public final boolean isScatha;
    public final long spawnTime;
	private long lastAttackTime = -1;
	private long lastFireAspectAttackTime = -1;
	private int lastFireAspectLevel = 0;
	
	private ArrayList<String> hitWeapons = new ArrayList<String>();
	
	public Worm(EntityArmorStand armorStand, boolean isScatha) {
	    this.armorStand = armorStand;
		this.isScatha = isScatha;
		spawnTime = Util.getCurrentTime();
	}
	
	public static Worm getByID(int id) {
	    List<Worm> worms = ScathaPro.getInstance().activeWorms;
		for (int i = 0; i < worms.size(); i ++)
			if (worms.get(i).armorStand.getEntityId() == id) return worms.get(i);
		return null;
	}
	
	public void attack(ItemStack weapon) {
	    long now = Util.getCurrentTime();
		lastAttackTime = now;
		ScathaPro.getInstance().lastWormAttackTime = lastAttackTime;

        String skyblockItemID = NBTUtil.getSkyblockItemID(weapon);
        if (skyblockItemID != null && !hitWeapons.contains(skyblockItemID))
            hitWeapons.add(skyblockItemID);

        NBTTagCompound enchantments = NBTUtil.getSkyblockTagCompound(weapon, "enchantments");
        if (enchantments != null) {
            lastFireAspectLevel = enchantments.getInteger("fire_aspect");
            if (lastFireAspectLevel > 0)
                lastFireAspectAttackTime = now;
        }
        
        MinecraftForge.EVENT_BUS.post(new WormHitEvent(this, weapon));
	}
	
	public long getLastAttackTime() {
		return lastAttackTime;
	}
	
	public long getLifetime() {
	    return Util.getCurrentTime() - spawnTime;
	}
    
    public String[] getHitWeapons() {
        return hitWeapons.toArray(new String[] {});
    }
	
	public int getHitWeaponsCount() {
	    return hitWeapons.size();
	}
	
	public boolean isFireAspectActive() {
	    if (lastFireAspectLevel > 0) {
    	    float fireAspectDuration = 0f;
    	    
    	    switch (lastFireAspectLevel) {
        	    case 1:
        	        fireAspectDuration = 3f;
                    break;
                case 2:
                case 3:
                    fireAspectDuration = 4f;
                    break;
    	    }
    	    
    	    return Util.getCurrentTime() - lastFireAspectAttackTime <= fireAspectDuration * 1000f + ScathaPro.pingTreshold;
	    }
	    return false;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{entityID: \""+armorStand.getEntityId()+"\", isScatha: "+isScatha+"}";
	}
}
