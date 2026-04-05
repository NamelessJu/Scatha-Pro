package namelessju.scathapro.sounds;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.sounds.instances.ScathaProSound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
    
    public ScathaProSound playAsScathaProSound(Identifier identifier, float volume, float pitch)
    {
        return play(new ScathaProSound(scathaPro, identifier, volume, pitch));
    }
    
    public boolean isPlaying(@Nullable SoundInstance sound)
    {
        if (sound == null) return false;
        return scathaPro.minecraft.getSoundManager().isActive(sound);
    }
    
    public void stop(@Nullable SoundInstance sound)
    {
        if (sound == null) return;
        scathaPro.minecraft.getSoundManager().stop(sound);
    }
    
    public boolean soundExists(@NonNull Identifier identifier)
    {
        return scathaPro.minecraft.getSoundManager().getSoundEvent(identifier) != null;
    }
}
