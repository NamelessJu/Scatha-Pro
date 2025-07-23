package namelessju.scathapro.gui.menus;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.Config;
import net.minecraft.client.gui.GuiScreen;

public abstract class ConfigGui extends ScathaProGui
{
    protected final Config config;
    
    public ConfigGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
        this.config = scathaPro.getConfig();
    }
    
    @Override
    public void onGuiClosed()
    {
        config.save();
    }
}
