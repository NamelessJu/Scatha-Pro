package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.OverlayToggleableElementsGuiList;

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
        
        scrollList = new OverlayToggleableElementsGuiList(scathaPro, this);
        
        buttonList.add(new DoneButton(504704999, this.width / 2 - 100, this.height - 30, 200, 20, "Done", this));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        // scathaPro.getOverlay().drawOverlay(width / 2, 30, Alignment.CENTER);
    }
    
    @Override
    public void onGuiClosed()
    {
        scathaPro.getOverlay().saveToggleableElementStates();
    }
    
}
