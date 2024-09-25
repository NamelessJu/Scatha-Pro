package com.namelessju.scathapro.alerts;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.menus.ScathaProGui;
import com.namelessju.scathapro.gui.menus.alertconfig.BedrockWallAlertConfigGui;
import com.namelessju.scathapro.gui.menus.alertconfig.HighHeatAlertConfigGui;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.util.SoundUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public enum Alert
{
    bedrockWall("bedrock_wall", "Bedrock Wall Alert", null, new DefaultAlertSound("note.pling", 1f, 0.5f), AlertTitle.create(null, "Close to bedrock", null, EnumChatFormatting.GRAY.toString(), 5, 20, 5), Config.Key.bedrockWallAlert, BedrockWallAlertConfigGui.class),
    
    wormPrespawn("worm_prespawn", "Worm Pre-Spawn Alert", "Triggers when either type of worm is about to spawn\n" + EnumChatFormatting.YELLOW + "(Requires hostile and master sound volumes to be above 0!)", new DefaultAlertSound("random.orb", 1f, 0.5f), AlertTitle.create(null, "Worm About To Spawn...", null, EnumChatFormatting.YELLOW.toString(), 0, 20, 5), Config.Key.wormPrespawnAlert),
    regularWormSpawn("regular_worm_spawn", "Regular Worm Spawn Alert", null, new DefaultAlertSound("random.levelup", 1f, 0.5f), AlertTitle.create("Worm", "Just a regular worm...", EnumChatFormatting.YELLOW.toString(), EnumChatFormatting.GRAY.toString(), 5, 20, 5), Config.Key.regularWormSpawnAlert),
    scathaSpawn("scatha_spawn", "Scatha Spawn Alert", null, new DefaultAlertSound("random.levelup", 1f, 0.8f), AlertTitle.create("Scatha", "Pray to RNGesus!", EnumChatFormatting.RED.toString(), EnumChatFormatting.GRAY.toString(), 0, 40, 10), Config.Key.scathaSpawnAlert),
    wormSpawnCooldownEnd("worm_spawn_cooldown_end", "Worm Spawn Cooldown Alert", "Triggers when the worm spawn cooldown ends", new DefaultAlertSound("note.pling", 1f, 0.75f), AlertTitle.create(null, "Worm Spawn Cooldown Ended", null, EnumChatFormatting.GREEN.toString(), 5, 30, 5), Config.Key.wormSpawnCooldownEndAlert),
    
    scathaPetDrop("scatha_pet_drop", "Scatha Pet Drop Alert", null, new DefaultAlertSound("mob.wither.death", 0.75f, 0.8f), AlertTitle.createWithVariableSubtitle("Scatha Pet!", EnumChatFormatting.YELLOW.toString(), 0, 130, 20), Config.Key.scathaPetDropAlert),
    
    highHeat("high_heat", "High Heat Alert", null, new DefaultAlertSound("item.fireCharge.use", 1f, 1f), AlertTitle.create("High Heat", "Cool down a little!", EnumChatFormatting.RED.toString(), EnumChatFormatting.AQUA.toString(), 5, 40, 5), Config.Key.highHeatAlert, HighHeatAlertConfigGui.class),
    anomalous_desire_ready("anomalous_desire_ready", "Anomalous Desire Ready Alert", "Triggers when you should use the Anomalous Desire pickaxe ability again", new DefaultAlertSound("random.orb", 1f, 1f), AlertTitle.create("Anomalous Desire Ready", null, EnumChatFormatting.GREEN.toString(), null, 5, 20, 5), Config.Key.pickaxeAbilityReadyAlert),
    
    goblinSpawn("goblin_spawn", "Goblin Spawn Alert", "Triggers when a golden or diamond goblin spawns", new DefaultAlertSound("random.levelup", 1f, 1.25f), AlertTitle.createWithVariableSubtitle("Goblin", EnumChatFormatting.DARK_GREEN.toString(), 3, 30, 5), Config.Key.goblinSpawnAlert),
    jerrySpawn("jerry_spawn", "Jerry Spawn Alert", "Triggers when a hidden Jerry (mayor perk) spawns", new DefaultAlertSound("random.levelup", 1f, 1.5f), AlertTitle.createWithVariableSubtitle("Jerry", EnumChatFormatting.AQUA.toString(), 5, 40, 10), Config.Key.jerrySpawnAlert);
    
    
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
    
    public final Class<? extends ScathaProGui> settingsGuiClass;
    
    private ISound lastSound = null;
    
    Alert(String alertId, String alertName, String description, DefaultAlertSound defaultSound, AlertTitle defaultTitle, Config.Key configKey)
    {
        this(alertId, alertName, description, defaultSound, defaultTitle, configKey, null);
    }
    
    Alert(String alertId, String alertName, String description, DefaultAlertSound defaultSound, AlertTitle defaultTitle, Config.Key configKey, Class<? extends ScathaProGui> settingsGuiClass)
    {
        this.alertId = alertId;
        this.alertName = alertName;
        this.description = description;
        
        this.defaultSound = defaultSound;
        this.defaultTitle = defaultTitle;
        this.configKey = configKey;
        
        this.settingsGuiClass = settingsGuiClass;
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
            else ScathaPro.getInstance().logDebug("Couldn't play alert sound \"" + soundPath + "\": Sound not found - playing default sound instead");
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
        AlertTitle modeTitle = ScathaPro.getInstance().getAlertModeManager().getCurrentMode().getTitle(this);
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
