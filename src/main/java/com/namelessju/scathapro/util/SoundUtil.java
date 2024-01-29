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
        ISound sound = new ScathaProSound(soundId, volume * (float) ScathaPro.getInstance().config.getDouble(Config.Key.soundsVolume), pitch);
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
