package namelessju.scathapro.sounds.instances;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class ScathaProMovingEntitySound extends ScathaProSound implements TickableSoundInstance
{
    public Entity entity;
    protected boolean isStopped = false;
    private boolean muted = false;
    
    private final boolean stopIfEntityIsRemoved;
    
    public ScathaProMovingEntitySound(ScathaPro scathaPro, Identifier identifier, float volume, float pitch, Entity entity, boolean stopIfEntityIsRemoved)
    {
        super(scathaPro, identifier, volume, pitch);
        this.entity = entity;
        this.stopIfEntityIsRemoved = stopIfEntityIsRemoved;
        
        this.attenuation = Attenuation.LINEAR;
        this.relative = false;
    }
    
    @Override
    public float getVolume()
    {
        return muted ? 0f : super.getVolume();
    }
    
    @Override
    public boolean isStopped()
    {
        return isStopped;
    }
    
    @Override
    public void tick()
    {
        if (entity.isRemoved())
        {
            if (stopIfEntityIsRemoved)
            {
                isStopped = true;
                entity = null;
                return;
            }
            
            muted = true;
        }
        else
        {
            muted = false;
            
            x = (float) entity.getX();
            y = (float) entity.getY();
            z = (float) entity.getZ();
        }
    }
    
    public void stop()
    {
        isStopped = true;
    }
}
