package com.namelessju.scathapro.gui.menus.alerts;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.lists.AlertsGuiList;
import com.namelessju.scathapro.gui.menus.ScathaProGui;

import net.minecraft.client.gui.GuiScreen;

public class AlertStatesGui extends ScathaProGui 
{
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
    }
}
