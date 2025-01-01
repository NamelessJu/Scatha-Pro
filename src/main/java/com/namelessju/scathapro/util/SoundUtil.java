package com.namelessju.scathapro.util;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.miscellaneous.ScathaProMovingSound;
import com.namelessju.scathapro.miscellaneous.ScathaProSound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public abstract class SoundUtil
{
    public static ScathaProSound playSound(String soundId)
    {
        return playSound(soundId, 1f, 1f);
    }
    
    public static ScathaProSound playSound(String soundId, float volume, float pitch)
    {
        ScathaProSound sound = new ScathaProSound(soundId, volume, pitch);
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
        return sound;
    }
    
    public static ScathaProMovingSound playMovingSound(String soundId, float volume, float pitch, Entity entity)
    {
        ScathaProMovingSound sound = new ScathaProMovingSound(soundId, volume, pitch, entity);
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
        return sound;
    }
    
    public static ScathaProSound playModSound(String sound)
    {
        return playModSound(sound, 1f, 1f);
    }
    
    public static ScathaProSound playModSound(String sound, float volume, float pitch)
    {
        return playSound(ScathaPro.MODID + ":" + sound, volume, pitch);
    }
    
    public static boolean soundExists(String id)
    {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        SoundEventAccessorComposite soundeventaccessorcomposite = soundHandler.getSound(new ResourceLocation(id));
        return soundeventaccessorcomposite != null && soundeventaccessorcomposite.cloneEntry() != SoundHandler.missing_sound;
    }
    

    private SoundUtil() {}
}
