package com.namelessju.scathapro.util;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.ScathaProSound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

public abstract class SoundUtil {

    public static void playSound(String sound) {
        playSound(sound, 1f, 1f);
    }
    public static void playSound(String sound, float volume, float pitch) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new ScathaProSound(sound, volume, pitch));
    }

    public static void playModSound(String sound) {
        playModSound(sound, 1f, 1f);
    }
    public static void playModSound(String sound, float volume, float pitch) {
        playSound(ScathaPro.MODID + ":" + sound, volume, pitch);
    }
    
    public static boolean soundExists(String id) {
    	SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
    	SoundEventAccessorComposite soundeventaccessorcomposite = soundHandler.getSound(new ResourceLocation(id));
    	return soundeventaccessorcomposite != null && soundeventaccessorcomposite.cloneEntry() != SoundHandler.missing_sound;
    }
    
}
