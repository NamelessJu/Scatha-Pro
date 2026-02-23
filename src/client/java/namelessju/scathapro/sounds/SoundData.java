package namelessju.scathapro.sounds;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.SoundManager;
import namelessju.scathapro.sounds.instances.ScathaProSound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public record SoundData(ResourceLocation identifier, float volume, float pitch)
{
    public SoundData(ResourceLocation identifier, float volume, float pitch)
    {
        this.identifier = identifier;
        this.volume = Mth.clamp(volume, 0f, 1f);
        this.pitch = pitch;
    }
    
    public static SoundData vanilla(String vanillaSoundPath, float volume, float pitch)
    {
        return new SoundData(ResourceLocation.withDefaultNamespace(vanillaSoundPath), volume, pitch);
    }
    
    public static SoundData scathaPro(String scathaProSoundPath, float volume, float pitch)
    {
        return new SoundData(ScathaPro.getIdentifier(scathaProSoundPath), volume, pitch);
    }
    
    public ScathaProSound playModSound(SoundManager soundManager)
    {
        return playModSound(soundManager, 1f);
    }
    
    public ScathaProSound playModSound(SoundManager soundManager, float volumeMultiplier)
    {
        return soundManager.playModSound(identifier, volumeMultiplier * volume, pitch);
    }
}
