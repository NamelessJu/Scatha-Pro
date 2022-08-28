package com.namelessju.scathapro.events;

import com.namelessju.scathapro.Worm;

import net.minecraft.item.ItemStack;

public class WormHitEvent extends WormEvent {
    
    public final ItemStack weapon;

    public WormHitEvent(Worm worm, ItemStack weapon) {
        super(worm);
        this.weapon = weapon;
    }

}
