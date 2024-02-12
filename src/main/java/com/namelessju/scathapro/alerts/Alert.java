package com.namelessju.scathapro.alerts;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.util.SoundUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public enum Alert
{
    bedrockWall("bedrock_wall", "Bedrock Alert", "Triggers when you're < 10 blocks away from bedrock", new DefaultAlertSound("note.pling", 1f, 0.5f), new AlertTitle(null, "Close to bedrock", null, EnumChatFormatting.GRAY.toString(), 5, 20, 5), Config.Key.bedrockWallAlert),
    // TODO:
    bedrockHole("bedrock_hole", "Bedrock Hole Alert", "Triggers when there's a hole in the bedrock wall/floor", new DefaultAlertSound("note.pling", 1f, 0.75f), new AlertTitle("Hole in bedrock!", (String) null, EnumChatFormatting.DARK_RED.toString(), null, 0, 40, 10), null),
    
    wormPrespawn("worm_prespawn", "Worm Pre-Spawn Alert", "Triggers when either type of worm is about to spawn", new DefaultAlertSound("random.orb", 1f, 0.5f), new AlertTitle(null, "Worm about to spawn...", null, EnumChatFormatting.YELLOW.toString(), 0, 20, 5), Config.Key.wormPrespawnAlert),
    regularWormSpawn("regular_worm_spawn", "Regular Worm Spawn Alert", "Triggers when a regular worm spawns around you", new DefaultAlertSound("random.levelup", 1f, 0.5f), new AlertTitle("Worm", "Just a regular worm...", EnumChatFormatting.YELLOW.toString(), EnumChatFormatting.GRAY.toString(), 5, 20, 5), Config.Key.regularWormSpawnAlert),
    scathaSpawn("scatha_spawn", "Scatha Spawn Alert", "Triggers when a Scatha spawns around you", new DefaultAlertSound("random.levelup", 1f, 0.8f), new AlertTitle("Scatha", "Pray to RNGesus!", EnumChatFormatting.RED.toString(), EnumChatFormatting.GRAY.toString(), 0, 40, 10), Config.Key.scathaSpawnAlert),
    wormSpawnCooldownEnd("worm_spawn_cooldown_end", "Worm Spawn Cooldown End Alert", "Triggers when the worm spawn cooldown runs out", new DefaultAlertSound("note.pling", 1f, 0.75f), new AlertTitle(null, "Worm spawn cooldown ended", null, EnumChatFormatting.GREEN.toString(), 5, 30, 5), Config.Key.wormSpawnCooldownEndAlert),
    
    scathaPetDrop("scatha_pet_drop", "Scatha Pet Drop Alert", "Triggers when you get a Scatha pet drop", new DefaultAlertSound("mob.wither.death", 0.75f, 0.8f), new AlertTitle("SCATHA PET DROP!", AlertTitle.SubtitleType.VARIABLE, EnumChatFormatting.YELLOW.toString(), null, 0, 130, 20), Config.Key.scathaPetDropAlert),
    
    goblinSpawn("goblin_spawn", "Goblin Spawn Alert", "Triggers when a golden/diamond goblin spawns", new DefaultAlertSound("random.levelup", 1f, 1.25f), new AlertTitle("Goblin", AlertTitle.SubtitleType.VARIABLE, EnumChatFormatting.DARK_GREEN.toString(), null, 3, 30, 5), Config.Key.goblinSpawnAlert),
    jerrySpawn("jerry_spawn", "Jerry Spawn Alert", "Triggers when a hidden Jerry (mayor perk) spawns", new DefaultAlertSound("random.levelup", 1f, 1.5f), new AlertTitle("Jerry", AlertTitle.SubtitleType.VARIABLE, EnumChatFormatting.AQUA.toString(), null, 5, 40, 10), Config.Key.jerrySpawnAlert);
    
    
    private static class DefaultAlertSound
    {
        public final String soundId;
        public final float volume;
        public final float pitch;
        
        public DefaultAlertSound(String soundId, float volume, float pitch)
        {
            this.soundId = soundId;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
    
    
    public final String alertId;
    public final String alertName;
    public final String description;
    public final Config.Key configKey;
    
    private final DefaultAlertSound defaultSound;
    private final AlertTitle defaultTitle;
    
    private ISound lastSound = null;
    
    Alert(String alertId, String alertName, String description, DefaultAlertSound defaultSound, AlertTitle defaultTitle, Config.Key configKey)
    {
        this.alertId = alertId;
        this.alertName = alertName;
        this.description = description;
        
        this.defaultSound = defaultSound;
        this.defaultTitle = defaultTitle;
        this.configKey = configKey;
    }
    

    public void play()
    {
        play(null);
    }
    
    public void play(String details)
    {
        if (!ScathaPro.getInstance().getConfig().getBoolean(configKey)) return;
        playSound();
        displayTitle(details);
    }
    
    
    public void playSound()
    {
        stopSound();
        
        ResourceLocation soundParentLocation = ScathaPro.getInstance().getAlertModeManager().getCurrentMode().getSoundBaseResourceLocation();
        if (soundParentLocation != null)
        {
            String soundPath = soundParentLocation.toString() + (soundParentLocation.getResourcePath().isEmpty() ? "" : ".") + alertId;
            if (SoundUtil.soundExists(soundPath))
            {
                lastSound = SoundUtil.playSound(soundPath);
                return;
            }
        }
        
        playDefaultSound();
    }
    
    public void playDefaultSound()
    {
        lastSound = SoundUtil.playSound(defaultSound.soundId, defaultSound.volume, defaultSound.pitch);
    }
    
    public void stopSound()
    {
        if (isSoundPlaying()) Minecraft.getMinecraft().getSoundHandler().stopSound(lastSound);
    }
    
    public boolean isSoundPlaying()
    {
        return lastSound != null && Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(lastSound);
    }
    
    
    public void displayTitle(String details)
    {
        AlertTitle modeTitle = ScathaPro.getInstance().getAlertModeManager().getCurrentMode().getTitle(alertId);
        AlertTitle title = defaultTitle.replaceWith(modeTitle, details);
        title.display();
    }
    
    public boolean hasTitle()
    {
        return defaultTitle.title != null;
    }
    
    public boolean hasSubtitle()
    {
        return defaultTitle.subtitle != null;
    }
    
    public AlertTitle getDefaultTitle()
    {
        return defaultTitle.replaceWith(null, null);
    }
}
