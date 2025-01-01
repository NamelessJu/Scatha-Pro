package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.lists.CustomAlertModeGuiList;

import net.minecraft.client.gui.GuiScreen;

public class CustomAlertModeGui extends ScathaProGui
{
    public CustomAlertModeGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    public String getTitle()
    {
        return "Custom Alert Modes";
    }
    
    public boolean hasBackground()
    {
        return false;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        
        scrollList = new CustomAlertModeGuiList(this);
        
        addDoneButton(this.width / 2 - 100, this.height - 30, 200, 20);
    }
}
