package namelessju.scathapro.alerts;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.managers.Config;
import namelessju.scathapro.util.SoundUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

public enum Alert
{
    bedrockWall("bedrock_wall", "Bedrock Wall Alert", null, new DefaultAlertSound("note.pling", 1f, 0.5f), AlertTitle.create(null, "Close to bedrock", null, EnumChatFormatting.GRAY.toString(), 5, 20, 5), Config.Key.bedrockWallAlert),
    oldLobby("old_lobby", "Old Crystal Hollows Lobby Alert", null, new DefaultAlertSound("note.pling", 1f, 0.5f), AlertTitle.create("Old Lobby", null, EnumChatFormatting.RED.toString(), null, 20, 40, 10), Config.Key.oldLobbyAlert),
    
    wormPrespawn("worm_prespawn", "Worm Pre-Spawn Alert", "Triggers when either type\nof worm is about to spawn\n" + EnumChatFormatting.YELLOW + "(Requires hostile and master\nsound volumes to be above 0!)", new DefaultAlertSound("random.orb", 1f, 0.5f), AlertTitle.create(null, "Worm About To Spawn...", null, EnumChatFormatting.YELLOW.toString(), 0, 20, 5), Config.Key.wormPrespawnAlert),
    regularWormSpawn("regular_worm_spawn", "Regular Worm Spawn Alert", null, new DefaultAlertSound("random.levelup", 1f, 0.5f), AlertTitle.create("Worm", "Just a regular worm...", EnumChatFormatting.YELLOW.toString(), EnumChatFormatting.GRAY.toString(), 5, 20, 5), Config.Key.regularWormSpawnAlert),
    scathaSpawn("scatha_spawn", "Scatha Spawn Alert", null, new DefaultAlertSound("random.levelup", 1f, 0.8f), AlertTitle.create("Scatha", "Pray to RNGesus!", EnumChatFormatting.RED.toString(), EnumChatFormatting.GRAY.toString(), 0, 40, 10), Config.Key.scathaSpawnAlert),
    wormSpawnCooldownEnd("worm_spawn_cooldown_end", "Worm Spawn Cooldown Alert", "Triggers when the worm\nspawn cooldown ends", new DefaultAlertSound("note.pling", 1f, 0.75f), AlertTitle.create(null, "Worm Spawn Cooldown Ended", null, EnumChatFormatting.GREEN.toString(), 5, 30, 5), Config.Key.wormSpawnCooldownEndAlert),
    
    scathaPetDrop("scatha_pet_drop", "Scatha Pet Drop Alert", null, new DefaultAlertSound("mob.wither.death", 0.75f, 0.8f), AlertTitle.createWithVariableSubtitle("Scatha Pet!", EnumChatFormatting.YELLOW.toString(), 0, 130, 20), Config.Key.scathaPetDropAlert),
    
    highHeat("high_heat", "High Heat Alert", null, new DefaultAlertSound("item.fireCharge.use", 1f, 1f), AlertTitle.create("High Heat", "Cool down a little!", EnumChatFormatting.RED.toString(), EnumChatFormatting.AQUA.toString(), 5, 40, 5), Config.Key.highHeatAlert),
    anomalous_desire_ready("anomalous_desire_ready", "Anomalous Desire Ready Alert", "Triggers when you should use the\nAnomalous Desire pickaxe ability again", new DefaultAlertSound("random.orb", 1f, 1f), AlertTitle.create("Anomalous Desire Ready", null, EnumChatFormatting.GREEN.toString(), null, 5, 20, 5), Config.Key.pickaxeAbilityReadyAlert),
    
    goblinSpawn("goblin_spawn", "Goblin Spawn Alert", "Triggers when a golden or\ndiamond goblin spawns", new DefaultAlertSound("random.levelup", 1f, 1.25f), AlertTitle.createWithVariableSubtitle("Goblin", EnumChatFormatting.DARK_GREEN.toString(), 3, 30, 5), Config.Key.goblinSpawnAlert),
    jerrySpawn("jerry_spawn", "Jerry Spawn Alert", "Triggers when a hidden Jerry\n(mayor perk) spawns", new DefaultAlertSound("random.levelup", 1f, 1.5f), AlertTitle.createWithVariableSubtitle("Jerry", EnumChatFormatting.AQUA.toString(), 5, 40, 10), Config.Key.jerrySpawnAlert),
    
    antiSleep("anti_sleep", "Anti-Sleep Alert", "Plays a loud sound in a random\ninterval to keep you awake", new DefaultAlertSound("scathapro:alert.anti_sleep", 2f, 1f), AlertTitle.create("", "", null, EnumChatFormatting.GRAY.toString(), 0, 40, 20), Config.Key.antiSleepAlert);
    
    
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
        this.playSound(-1f);
    }
    
    public void playSound(float volumeOverride)
    {
        stopSound();
        
        AlertMode mode = ScathaPro.getInstance().getAlertModeManager().getCurrentMode();
        
        float volume = volumeOverride >= 0f ? volumeOverride : mode.getSoundVolume(this);
        
        ResourceLocation soundParentLocation = mode.getSoundBaseResourceLocation();
        if (soundParentLocation != null)
        {
            String soundPath = soundParentLocation.toString() + (soundParentLocation.getResourcePath().isEmpty() ? "" : ".") + alertId;
            
            if (SoundUtil.soundExists(soundPath))
            {
                lastSound = SoundUtil.playSound(soundPath, volume, 1f);
                return;
            }
            else ScathaPro.getInstance().logDebug("Couldn't play alert sound \"" + soundPath + "\": Sound not found - playing default sound instead");
        }
        
        playDefaultSound(volume);
    }
    
    public void playDefaultSound(float volume)
    {
        lastSound = SoundUtil.playSound(defaultSound.soundId, volume * defaultSound.volume, defaultSound.pitch);
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
