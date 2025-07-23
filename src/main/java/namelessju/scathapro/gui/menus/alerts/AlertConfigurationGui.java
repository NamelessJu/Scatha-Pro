package namelessju.scathapro.gui.menus.alerts;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.lists.AlertConfigurationList;
import namelessju.scathapro.gui.menus.ConfigGui;
import namelessju.scathapro.managers.Config;
import net.minecraft.client.gui.GuiScreen;

public class AlertConfigurationGui extends ConfigGui
{
    public AlertConfigurationGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Alert Configuration";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        this.scrollList = new AlertConfigurationList(this);
        
        addScrollListDoneButton();
    }
    
    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        
        if (config.getInt(Config.Key.antiSleepAlertIntervalMax) < scathaPro.variables.nextAntiSleepAlertTriggerTickCount)
        {
            scathaPro.variables.setRandomAntiSleepAlertTriggerMinutes();
        }
    }
}
