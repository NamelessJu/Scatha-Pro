package com.namelessju.scathapro.miscellaneous;

import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;

public class ScathaProSound implements ISound
{
    public final ResourceLocation resourceLocation;
    public float volume;
    public float pitch;
    
    public ScathaProSound(String soundPath, float volume, float pitch)
    {
        this.resourceLocation = new ResourceLocation(soundPath);
        this.volume = volume;
        this.pitch = pitch;
    }
    
    @Override
    public ResourceLocation getSoundLocation()
    {
        return resourceLocation;
    }

    @Override
    public boolean canRepeat()
    {
        return false;
    }

    @Override
    public int getRepeatDelay()
    {
        return 0;
    }

    @Override
    public float getVolume()
    {
        return volume;
    }

    @Override
    public float getPitch()
    {
        return pitch;
    }

    @Override
    public float getXPosF()
    {
        return 0f;
    }
    
    @Override
    public float getYPosF()
    {
        return 0f;
    }

    @Override
    public float getZPosF()
    {
        return 0f;
    }

    @Override
    public AttenuationType getAttenuationType()
    {
        return AttenuationType.NONE;
    }
}
