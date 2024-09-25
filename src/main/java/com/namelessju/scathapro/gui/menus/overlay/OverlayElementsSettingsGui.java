package com.namelessju.scathapro.gui.menus.overlay;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.lists.OverlayComponentsGuiList;
import com.namelessju.scathapro.gui.menus.ScathaProGui;

import net.minecraft.client.gui.GuiScreen;

public class OverlayElementsSettingsGui extends ScathaProGui
{
    
    public OverlayElementsSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Overlay Components";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        scrollList = new OverlayComponentsGuiList(this);
        
        addDoneButton(this.width / 2 - 100, this.height - 30, 200, 20);
    }
    
    @Override
    public void onGuiClosed()
    {
        scathaPro.getOverlay().saveToggleableElementStates();
    }
    
}
