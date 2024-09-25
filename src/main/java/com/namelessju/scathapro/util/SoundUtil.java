package com.namelessju.scathapro.util;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.ScathaProSound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

public abstract class SoundUtil
{
    public static ISound playSound(String soundId)
    {
        return playSound(soundId, 1f, 1f);
    }
    
    public static ISound playSound(String soundId, float volume, float pitch)
    {
        float scathaProVolume = Math.min((float) ScathaPro.getInstance().getConfig().getDouble(Config.Key.soundsVolume), 1f);
        float totalVolume = volume * scathaProVolume;
        ScathaProSound sound = new ScathaProSound(soundId, totalVolume, pitch);
        ScathaPro.getInstance().logDebug("Playing sound \"" + soundId + "\" with volume " + volume + " and Scatha-Pro volume " + scathaProVolume + " -> total volume: " + totalVolume);
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
        return sound;
    }
    
    public static ISound playModSound(String sound)
    {
        return playModSound(sound, 1f, 1f);
    }
    
    public static ISound playModSound(String sound, float volume, float pitch)
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
