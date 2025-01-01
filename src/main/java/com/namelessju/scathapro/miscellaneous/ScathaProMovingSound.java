package com.namelessju.scathapro.miscellaneous;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.entity.Entity;

public class ScathaProMovingSound extends ScathaProSound implements ITickableSound
{
    public Entity entity;
    private boolean isDone = false;
    
    private float x = 0f, y = 0f, z = 0f;
    
    public ScathaProMovingSound(String soundPath, float volume, float pitch, Entity entity)
    {
        super(soundPath, volume, pitch);
        this.entity = entity;
    }

    @Override
    public void update()
    {
        if (entity == null || entity.isDead
            || Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().theWorld.getEntityByID(entity.getEntityId()) == null)
        {
            isDone = true;
            entity = null;
            return;
        }
        
        x = (float) entity.posX;
        y = (float) entity.posY;
        z = (float) entity.posZ;
    }

    @Override
    public float getXPosF()
    {
        return x;
    }
    
    @Override
    public float getYPosF()
    {
        return y;
    }

    @Override
    public float getZPosF()
    {
        return z;
    }
    
    @Override
    public boolean isDonePlaying()
    {
        return isDone;
    }

    @Override
    public AttenuationType getAttenuationType()
    {
        return AttenuationType.LINEAR;
    }
}
