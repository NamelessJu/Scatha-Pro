package com.namelessju.scathapro;

import java.util.ArrayList;
import java.util.List;

import com.namelessju.scathapro.events.WormHitEvent;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class Worm {
    
    public final EntityArmorStand armorStand;
	public final boolean isScatha;
    public final long spawnTime;
	private long lastAttackTime = -1;
	
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
		lastAttackTime = Util.getCurrentTime();
		ScathaPro.getInstance().lastWormAttackTime = lastAttackTime;
        
        MinecraftForge.EVENT_BUS.post(new WormHitEvent(this, weapon));

        String skyblockItemID = NBTUtil.getSkyblockItemID(weapon);
        if (skyblockItemID != null && !hitWeapons.contains(skyblockItemID))
            hitWeapons.add(skyblockItemID);
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
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{entityID: \""+armorStand.getEntityId()+"\", isScatha: "+isScatha+"}";
	}
}
