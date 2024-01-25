package com.namelessju.scathapro.objects;

import net.minecraft.entity.player.EntityPlayer;

public class Goblin {
	
	public enum Type {
		GOLD, DIAMOND;
	}
	
    public final EntityPlayer playerEntity;
    public final Type type;

    public Goblin(EntityPlayer playerEntity, Type type) {
        this.playerEntity = playerEntity;
        this.type = type;
    }
}
