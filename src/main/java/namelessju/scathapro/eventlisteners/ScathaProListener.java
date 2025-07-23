package namelessju.scathapro.eventlisteners;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.Minecraft;

public abstract class ScathaProListener
{
    protected final ScathaPro scathaPro;
    protected final Minecraft mc;
    
    public ScathaProListener(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        this.mc = scathaPro.getMinecraft();
    }
}
