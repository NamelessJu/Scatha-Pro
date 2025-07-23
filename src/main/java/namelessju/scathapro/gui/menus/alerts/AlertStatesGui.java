package namelessju.scathapro.gui.menus.alerts;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.gui.lists.AlertsGuiList;
import namelessju.scathapro.gui.menus.ConfigGui;
import net.minecraft.client.gui.GuiScreen;

public class AlertStatesGui extends ConfigGui 
{
    Boolean antiSleepAlertEnabledBefore = null; 
    
    public AlertStatesGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Enable/Disable Alerts";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        scrollList = new AlertsGuiList(this);
        
        addDoneButton(this.width / 2 - 100, this.height - 30, 200, 20);
        
        if (antiSleepAlertEnabledBefore == null)
        {
            antiSleepAlertEnabledBefore = config.getBoolean(Alert.antiSleep.configKey);
        }
    }
    
    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        
        if (config.getBoolean(Alert.antiSleep.configKey))
        {
            if (antiSleepAlertEnabledBefore != null)
            {
                if (!antiSleepAlertEnabledBefore)
                {
                    scathaPro.variables.antiSleepAlertTickTimer = 0;
                    scathaPro.variables.setRandomAntiSleepAlertTriggerMinutes();
                }
                antiSleepAlertEnabledBefore = null;
            }
        }
    }
}
