package namelessju.scathapro.gui.menus;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.lists.MiscSettingsList;
import net.minecraft.client.gui.GuiScreen;

public class MiscSettingsGui extends ConfigGui
{
    public MiscSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }

    @Override
    public String getTitle()
    {
        return "Miscellaneous Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        this.scrollList = new MiscSettingsList(this);
        addScrollListDoneButton();
    }
}
