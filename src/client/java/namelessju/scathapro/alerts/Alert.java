package namelessju.scathapro.alerts;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.alerts.title.AlertTitleTemplate;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.sounds.SoundData;
import namelessju.scathapro.sounds.SoundManager;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Alert
{
    public final @NonNull String alertId;
    public final @NonNull String alertName;
    public final @Nullable Component description;
    public final JsonFile.@NonNull BooleanValue isEnabledConfigValue;

    public final @NonNull AlertTitleTemplate titleTemplate;
    public final @NonNull SoundData defaultSound;
    /** Extra alert sounds that are unaffected by the alert mode */
    private @Nullable SoundData[] extraSounds = null;

    private @Nullable SoundInstance lastPlayedSound = null;

    public Alert(@NonNull String alertId, @NonNull String alertName, @Nullable Component description,
                 @NonNull SoundData defaultSound, @NonNull AlertTitleTemplate titleTemplate,
                 JsonFile.@NonNull BooleanValue isEnabledConfigValue)
    {
        this.alertId = alertId;
        this.alertName = alertName;
        this.description = description;

        this.defaultSound = defaultSound;
        this.titleTemplate = titleTemplate;
        this.isEnabledConfigValue = isEnabledConfigValue;
    }

    public void setExtraSounds(@NonNull SoundData... extraSounds)
    {
        this.extraSounds = extraSounds;
    }


    public void play(ScathaPro scathaPro)
    {
        play(scathaPro, scathaPro.config.alerts.mode.get());
    }

    public void play(ScathaPro scathaPro, @Nullable Component details)
    {
        play(scathaPro, scathaPro.config.alerts.mode.get(), details);
    }

    public void play(ScathaPro scathaPro, @NonNull AlertMode alertMode)
    {
        play(scathaPro, alertMode, null);
    }

    public void play(ScathaPro scathaPro, @NonNull AlertMode alertMode, @Nullable Component details)
    {
        if (!isEnabledConfigValue.get()) return;
        playSound(scathaPro.soundManager, alertMode);
        displayTitle(scathaPro, alertMode, details);
    }

    @SuppressWarnings("UnusedReturnValue")
    public SoundInstance playSound(SoundManager soundManager, @NonNull AlertMode alertMode)
    {
        return playSound(soundManager, alertMode, -1f);
    }

    public SoundInstance playSound(SoundManager soundManager, @NonNull AlertMode alertMode, float volumeMultiplierOverride)
    {
        stopSound(soundManager);

        SoundData soundData = alertMode.getSoundData(this);
        if (soundData == null)
        {
            soundData = defaultSound;
        }

        if (extraSounds != null)
        {
            for (SoundData sound : extraSounds)
            {
                if (sound == null) continue;
                sound.playAsScathaProSound(soundManager, volumeMultiplierOverride);
                ScathaPro.LOGGER.debug("Played extra alert sound {}", sound.identifier().toString());
            }
        }

        float volumeMultiplier = volumeMultiplierOverride >= 0f
            ? volumeMultiplierOverride
            : alertMode.getSoundVolumeMultiplier(this);

        lastPlayedSound = soundData.playAsScathaProSound(soundManager, volumeMultiplier);
        return lastPlayedSound;
    }

    public void stopSound(SoundManager soundManager)
    {
        if (lastPlayedSound != null) soundManager.stop(lastPlayedSound);
    }

    public boolean isSoundPlaying(SoundManager soundManager)
    {
        return lastPlayedSound != null && soundManager.isPlaying(lastPlayedSound);
    }

    public void displayTitle(ScathaPro scathaPro, @NonNull AlertMode alertMode, @Nullable Component details)
    {
        Component modeTitleOverride = alertMode.getTitleOverride(this);
        Component modeSubtitleOverride = alertMode.getSubtitleOverride(this);
        scathaPro.alertTitleOverlay.displayTitle(titleTemplate.getDisplayable(modeTitleOverride, modeSubtitleOverride, details));
    }
}