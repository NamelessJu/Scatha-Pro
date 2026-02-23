package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.gui.menus.screens.FakeBanScreen;
import namelessju.scathapro.sounds.instances.ScathaProSound;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class SoundManager
{
    private final ScathaPro scathaPro;
    
    public SoundManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public <T extends SoundInstance> T play(T sound)
    {
        scathaPro.minecraft.getSoundManager().play(sound);
        return sound;
    }
    
    public ScathaProSound playModSound(Identifier identifier, float volume, float pitch)
    {
        return play(new ScathaProSound(scathaPro, identifier, volume, pitch));
    }
    
    public boolean isPlaying(SoundInstance sound)
    {
        return scathaPro.minecraft.getSoundManager().isActive(sound);
    }
    
    public void stop(SoundInstance sound)
    {
        scathaPro.minecraft.getSoundManager().stop(sound);
    }
    
    public boolean soundExists(Identifier identifier)
    {
        return scathaPro.minecraft.getSoundManager().getSoundEvent(identifier) != null;
    }
    
    
    // TODO: shouldn't be in here
    public boolean shouldPlaySound(SoundInstance sound)
    {
        // ScathaPro.LOGGER.debug("Playing sound {} at pitch {} with volume {}", sound.getIdentifier(), sound.getPitch(), sound.getVolume());
        
        // Mute sounds in fake ban screen
        
        if (scathaPro.minecraft.screen instanceof FakeBanScreen
            && !isVanillaSound(sound, "ui.button.click"))
        {
            return false;
        }
        
        
        if (!scathaPro.coreManager.isInCrystalHollows()) return true;
        
        
        // Detect worm pre-spawn
        
        long now = TimeUtil.now();
        if (
            (scathaPro.coreManager.lastPreAlertTime < 0L || now - scathaPro.coreManager.lastPreAlertTime > 10000)
            && isVanillaSound(sound, "entity.spider.step")
            && (
                Mth.equal(sound.getPitch(), 2.0952382f)
                || scathaPro.config.dev.devModeEnabled.get() && sound.getPitch() >= 2f
            )
        ) {
            scathaPro.coreManager.lastPreAlertTime = now;
            ScathaProEvents.wormPreSpawnEvent.trigger(scathaPro);
        }
        
        // Mute non-Scatha-Pro sounds in Crystal Hollows
        
        if (!(sound instanceof ScathaProSound)
            && scathaPro.config.sounds.muteCrystalHollowsSounds.get())
        {
            boolean cancel = true;
            
            if (isVanillaSound(sound, "ui.button.click")) cancel = false;
            else if (scathaPro.config.sounds.keepDragonLairSounds.get()
                && (isVanillaSound(sound, "entity.ender_dragon.growl") || isVanillaSound(sound, "entity.ender_dragon.flap")))
                cancel = false;
            
            return !cancel;
        }
        
        return true;
    }
    
    private boolean isVanillaSound(SoundInstance sound, String path)
    {
        return sound.getIdentifier().equals(Identifier.withDefaultNamespace(path));
    }
}
