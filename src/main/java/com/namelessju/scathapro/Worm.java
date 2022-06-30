package com.namelessju.scathapro;

import java.util.ArrayList;
import java.util.List;

import com.namelessju.scathapro.achievements.Achievement;

import net.minecraft.item.ItemStack;

public class Worm {
	public final int entityID;
	public final boolean isScatha;
	private long lastAttackTime = -1;
	private long spawnTime;
	
	private ArrayList<String> hitWeapons = new ArrayList<String>();
	
	public Worm(int entityID, boolean isScatha) {
		this.entityID = entityID;
		this.isScatha = isScatha;
		spawnTime = Util.getCurrentTime();
	}
	
	public static Worm getByID(List<Worm> list, int id) {
		for (int i = 0; i < list.size(); i ++)
			if (list.get(i).entityID == id) return list.get(i);
		return null;
	}
	
	public void attack(ItemStack weapon) {
		lastAttackTime = Util.getCurrentTime();
		ScathaPro.getInstance().lastWormAttackTime = lastAttackTime;
		
	    String skyblockItemID = Util.getSkyblockItemID(weapon);
	    if (skyblockItemID != null && !hitWeapons.contains(skyblockItemID)) {
            hitWeapons.add(skyblockItemID);
            
            if (skyblockItemID.equals("DIRT") && isScatha) Achievement.scatha_hit_dirt.setProgress(Achievement.scatha_hit_dirt.goal);
	    }
	}
	
	public long getLastAttackTime() {
		return lastAttackTime;
	}
	
	public long getLifetime() {
	    return Util.getCurrentTime() - spawnTime;
	}
	
	public int getHitWeaponsCount() {
	    return hitWeapons.size();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{entityID:\""+entityID+"\", isScatha:"+isScatha+"}";
	}
}
