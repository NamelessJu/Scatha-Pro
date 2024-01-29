package com.namelessju.scathapro.events;

import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;

import net.minecraft.item.ItemStack;

public class WormHitEvent extends WormEvent
{
    public final ItemStack weapon;

    public WormHitEvent(DetectedWorm worm, ItemStack weapon)
    {
        super(worm);
        this.weapon = weapon;
    }
}
