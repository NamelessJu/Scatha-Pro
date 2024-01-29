package com.namelessju.scathapro.entitydetection.detectedentities;

import com.namelessju.scathapro.events.GoblinSpawnEvent;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.common.MinecraftForge;

public class DetectedGoblin extends DetectedEntity
{
    public enum Type
    {
        GOLD, DIAMOND;
    }
    
    public final Type type;

    public DetectedGoblin(EntityArmorStand entity, Type type)
    {
        super(entity);
        this.type = type;
    }

    @Override
    protected void onRegistration()
    {
        MinecraftForge.EVENT_BUS.post(new GoblinSpawnEvent(this));
    }
}
