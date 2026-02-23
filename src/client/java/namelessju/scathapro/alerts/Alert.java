package namelessju.scathapro.alerts;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.alerts.title.AlertTitleTemplate;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.sounds.SoundData;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Alert
{
    private final @NonNull ScathaPro scathaPro;
    
    public final @NonNull String alertId;
    public final @NonNull String alertName;
    public final @Nullable Component description;
    public final JsonFile.@NonNull BooleanValue configValue;
    
    public final @NonNull SoundData defaultSound;
    public final @NonNull AlertTitleTemplate titleTemplate;
    
    private @Nullable SoundData[] extraSounds = null;
    private @Nullable SoundInstance lastSound = null;
    
    public Alert(@NonNull ScathaPro scathaPro, @NonNull String alertId, @NonNull String alertName, @Nullable Component description,
                 @NonNull SoundData defaultSound, @NonNull AlertTitleTemplate titleTemplate,
                 JsonFile.@NonNull BooleanValue configValue)
    {
        this.scathaPro = scathaPro;
        this.alertId = alertId;
        this.alertName = alertName;
        this.description = description;
        
        this.defaultSound = defaultSound;
        this.titleTemplate = titleTemplate;
        this.configValue = configValue;
    }
    
    /**
     * Sets extra alert sounds that are unaffected by the alert mode
     */
    public void setExtraSounds(@NonNull SoundData... extraSounds)
    {
        this.extraSounds = extraSounds;
    }
    
    
    public void play()
    {
        play(null);
    }
    
    public void play(@Nullable Component details)
    {
        if (!configValue.get()) return;
        playSound();
        displayTitle(details);
    }
    
    public void playSound()
    {
        stopSound();
        
        AlertMode mode = scathaPro.alertModeManager.getCurrentMode();
        float volume = mode.getAlertSoundVolume(this);
        
        if (extraSounds != null)
        {
            for (SoundData sound : extraSounds)
            {
                if (sound == null) continue;
                sound.playModSound(scathaPro.soundManager, volume);
                ScathaPro.LOGGER.debug("Played extra alert sound {}", sound.identifier().toString());
            }
        }
        
        ResourceLocation soundParentIdentifier = mode.getSoundBaseIdentifier();
        if (soundParentIdentifier != null)
        {
            ResourceLocation soundIdentifier = ResourceLocation.fromNamespaceAndPath(
                soundParentIdentifier.getNamespace(),
                soundParentIdentifier.getPath().isEmpty()
                    ? alertId
                    : soundParentIdentifier.getPath() + "." + alertId
            );
            if (scathaPro.soundManager.soundExists(soundIdentifier))
            {
                lastSound = scathaPro.soundManager.playModSound(soundIdentifier, volume, 1f);
                return;
            }
            else ScathaPro.LOGGER.debug("Couldn't play alert sound \"{}\": Sound not found - playing default sound instead", soundIdentifier);
        }
        
        playDefaultSound(volume);
    }
    
    public void playDefaultSound(float volume)
    {
        lastSound = scathaPro.soundManager.playModSound(defaultSound.identifier(), volume * defaultSound.volume(), defaultSound.pitch());
    }
    
    public void stopSound()
    {
        if (lastSound != null) scathaPro.soundManager.stop(lastSound);
    }
    
    public boolean isSoundPlaying()
    {
        return lastSound != null && scathaPro.soundManager.isPlaying(lastSound);
    }
    
    
    public void displayTitle(@Nullable Component details)
    {
        Component modeTitleOverride = scathaPro.alertModeManager.getCurrentMode().getTitleOverride(this);
        Component modeSubtitleOverride = scathaPro.alertModeManager.getCurrentMode().getSubtitleOverride(this);
        scathaPro.alertTitleOverlay.displayTitle(titleTemplate.getDisplayable(modeTitleOverride, modeSubtitleOverride, details));
    }
}
