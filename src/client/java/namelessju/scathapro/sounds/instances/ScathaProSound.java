package namelessju.scathapro.sounds.instances;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class ScathaProSound extends AbstractSoundInstance
{
    protected final ScathaPro scathaPro;
    
    public ScathaProSound(ScathaPro scathaPro, Identifier identifier, float volume, float pitch)
    {
        super(identifier, SoundSource.MASTER, SoundInstance.createUnseededRandom());
        this.scathaPro = scathaPro;
        
        this.volume = volume;
        this.pitch = pitch;
        
        this.x = 0f;
        this.y = 0f;
        this.z = 0f;
        
        this.attenuation = Attenuation.NONE;
        this.relative = true;
        
        this.looping = false;
        this.delay = 0;
    }
    
    @Override
    public float getVolume()
    {
        return Mth.clamp(this.volume * scathaPro.config.sounds.volume.get(), 0f, 1f);
    }
}
