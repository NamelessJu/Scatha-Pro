package namelessju.scathapro.miscellaneous.sound;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.SoundUtil;
import net.minecraft.entity.Entity;

public class ScappaSound extends ScathaProMovingSound
{
    public static ScappaSound play(float volume, float pitch, Entity entity)
    {
        ScappaSound sound = new ScappaSound(volume, pitch, entity);
        SoundUtil.playSound(sound);
        return sound;
    }
    
    public ScappaSound(float volume, float pitch, Entity entity)
    {
        super(ScathaPro.MODID + ":scappa", volume, pitch, entity);
    }
    
    @Override
    public void update()
    {
        if (this.isDone) return;
        
        updatePosition(false);
    }
    
    public void stop()
    {
        this.isDone = true;
    }
}
