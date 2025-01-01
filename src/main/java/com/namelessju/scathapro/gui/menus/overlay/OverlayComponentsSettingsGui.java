package com.namelessju.scathapro.gui.menus.overlay;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.HoverArea;
import com.namelessju.scathapro.gui.lists.OverlayComponentsGuiList;
import com.namelessju.scathapro.gui.menus.ScathaProGui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

public class OverlayComponentsSettingsGui extends ScathaProGui
{
    private boolean showPreview = false;
    private HoverArea previewHoverArea;
    
    public OverlayComponentsSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
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
        
        elements.add(previewHoverArea = new HoverArea(1, width / 2 - 75, 35, 150, 20, "Preview"));
        
        addDoneButton(this.width / 2 - 100, this.height - 30, 200, 20);
    }
    
    @Override
    public void updateScreen()
    {
        super.updateScreen();
        
        if (showPreview)
        {
            scathaPro.getOverlay().updateRealtimeElements();
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        boolean previewShownBefore = showPreview;
        showPreview = previewHoverArea.isHovered(mouseX, mouseY);
        if (showPreview)
        {
            if (!previewShownBefore) scathaPro.getOverlay().updateRealtimeElements();
            
            Gui.drawRect(0, 0, width, height, 0xA0000000);
            previewHoverArea.drawButton(mc, mouseX, mouseY);
            scathaPro.getOverlay().drawOverlay(width / 2 - (int) ((scathaPro.getOverlay().getWidth() / 2) * scathaPro.getOverlay().getScale()), 55);
        }
    }
    
    @Override
    public void onGuiClosed()
    {
        scathaPro.getOverlay().saveToggleableElementStates();
    }
}
