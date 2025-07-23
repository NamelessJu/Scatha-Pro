package namelessju.scathapro.miscellaneous.sound;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.Config;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class ScathaProSound implements ISound
{
    public final ResourceLocation resourceLocation;
    public float volume;
    public float pitch;
    
    public ScathaProSound(String soundPath, float volume, float pitch)
    {
        this.resourceLocation = new ResourceLocation(soundPath);
        this.volume = volume;
        this.pitch = pitch;
    }
    
    @Override
    public ResourceLocation getSoundLocation()
    {
        return resourceLocation;
    }

    @Override
    public boolean canRepeat()
    {
        return false;
    }

    @Override
    public int getRepeatDelay()
    {
        return 0;
    }
    
    @Override
    public float getVolume()
    {
        return MathHelper.clamp_float(volume * (float) ScathaPro.getInstance().getConfig().getDouble(Config.Key.soundsVolume), 0f, 1f);
    }
    
    @Override
    public float getPitch()
    {
        return pitch;
    }

    @Override
    public float getXPosF()
    {
        return 0.5f;
    }
    
    @Override
    public float getYPosF()
    {
        return 0.5f;
    }

    @Override
    public float getZPosF()
    {
        return 0.5f;
    }

    @Override
    public AttenuationType getAttenuationType()
    {
        return AttenuationType.NONE;
    }
}
