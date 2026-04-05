package namelessju.scathapro.sounds;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.sounds.instances.ScathaProSound;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public record SoundData(Identifier identifier, float volume, float pitch)
{
    public SoundData(Identifier identifier, float volume, float pitch)
    {
        this.identifier = identifier;
        this.volume = Mth.clamp(volume, 0f, 1f);
        this.pitch = pitch;
    }
    
    public static SoundData vanilla(String vanillaSoundPath, float volume, float pitch)
    {
        return new SoundData(Identifier.withDefaultNamespace(vanillaSoundPath), volume, pitch);
    }
    
    public static SoundData scathaPro(String scathaProSoundPath, float volume, float pitch)
    {
        return new SoundData(ScathaPro.getIdentifier(scathaProSoundPath), volume, pitch);
    }
    
    public SoundData withVolume(float newVolume)
    {
        return new SoundData(identifier, newVolume, pitch);
    }
    
    public ScathaProSound playAsScathaProSound(SoundManager soundManager)
    {
        return playAsScathaProSound(soundManager, 1f);
    }
    
    public ScathaProSound playAsScathaProSound(SoundManager soundManager, float volumeMultiplier)
    {
        return soundManager.playAsScathaProSound(identifier, volumeMultiplier * volume, pitch);
    }
}
