package com.namelessju.scathapro.util;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.ScathaProSound;

import net.minecraft.client.Minecraft;

public abstract class SoundUtil {

    public static void playSound(String sound) {
        playSound(sound, 1f, 1f);
    }
    public static void playSound(String sound, float volume, float pitch) {
        /*
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) player.playSound(sound, (float) (Config.instance.getDouble(Config.Key.volume) * volume), pitch);
        */

        Minecraft.getMinecraft().getSoundHandler().playSound(new ScathaProSound(sound, volume, pitch));
    }

    public static void playModSound(String sound) {
        playModSound(sound, 1f, 1f);
    }
    public static void playModSound(String sound, float volume, float pitch) {
        playSound(ScathaPro.MODID + ":" + sound, volume, pitch);
    }
    
}
