package namelessju.scathapro.sounds.instances;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.resources.sounds.TickableSoundInstance;

public class VolumePreviewSound extends ScathaProSound implements TickableSoundInstance
{
    public VolumePreviewSound(ScathaPro scathaPro)
    {
        super(scathaPro, ScathaPro.getIdentifier("alert_modes.meme.regular_worm_spawn"), 1f, 1f);
    }
    
    @Override
    public boolean isStopped()
    {
        return false;
    }
    
    @Override
    public void tick()
    {
        // tickable just to automatically update
        // the volume of the playing sound
    }
}
