package namelessju.scathapro.miscellaneous.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.entity.Entity;

public class ScathaProMovingSound extends ScathaProSound implements ITickableSound
{
    public Entity entity;
    protected boolean isDone = false;
    private boolean muted = false;
    private float x = 0f, y = 0f, z = 0f;
    
    public ScathaProMovingSound(String soundPath, float volume, float pitch, Entity entity)
    {
        super(soundPath, volume, pitch);
        this.entity = entity;
    }
    
    @Override
    public void update()
    {
        updatePosition(true);
    }
    
    protected void updatePosition(boolean stopIfEntityIsInvalid)
    {
        boolean isEntityInvalid = entity == null || entity.isDead || entity.worldObj == null;
        if (isEntityInvalid)
        {
            if (stopIfEntityIsInvalid)
            {
                isDone = true;
                entity = null;
                return;
            }
            
            muted = true;
        }
        else
        {
            muted = false;
            
            x = (float) entity.posX;
            y = (float) entity.posY;
            z = (float) entity.posZ;
        }
    }
    
    @Override
    public float getVolume()
    {
        return muted ? 0f : super.getVolume();
    }

    @Override
    public float getXPosF()
    {
        return x;
    }
    
    @Override
    public float getYPosF()
    {
        return y;
    }
    
    @Override
    public float getZPosF()
    {
        return z;
    }
    
    @Override
    public boolean isDonePlaying()
    {
        return isDone || Minecraft.getMinecraft().theWorld == null;
    }
    
    @Override
    public AttenuationType getAttenuationType()
    {
        return AttenuationType.LINEAR;
    }
}
