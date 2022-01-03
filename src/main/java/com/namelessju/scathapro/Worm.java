package com.namelessju.scathapro;

import java.util.List;

public class Worm {
	private int entityID;
	private boolean isScatha;
	private long lastAttackTime = -1;
	
	public Worm(int entityID, boolean isScatha) {
		this.entityID = entityID;
		this.isScatha = isScatha;
	}
	
	public static Worm getByID(List<Worm> list, int id) {
		for (int i = 0; i < list.size(); i ++)
			if (list.get(i).getEntityID() == id) return list.get(i);
		return null;
	}
	
	public int getEntityID() {
		return entityID;
	}

	public boolean isScatha() {
		return isScatha;
	}
	
	public void attack() {
		lastAttackTime = Util.getCurrentTime();
	}
	
	public long getLastAttackTime() {
		return lastAttackTime;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{entityID:\""+entityID+"\", isScatha:"+isScatha+"}";
	}
}
