package com.namelessju.scathapro.gui.menus.overlay;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.menus.ScathaProGui;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.overlay.Overlay;

import net.minecraft.client.gui.GuiScreen;

public abstract class OverlaySettingsGui extends ScathaProGui
{
    protected final Config config;
    protected final Overlay overlay;
    
    private boolean debugInfoShownBefore = false;
    
    @Override
    public boolean hasBackground()
    {
        return false;
    }
    
    @Override
    protected void drawCustomBackground()
    {
        overlay.drawOverlay();
    }
    
    public OverlaySettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
        
        config = scathaPro.getConfig();
        overlay = scathaPro.getOverlay();
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        debugInfoShownBefore = scathaPro.getMinecraft().gameSettings.showDebugInfo;
        scathaPro.getMinecraft().gameSettings.showDebugInfo = false;
    }
    
    @Override
    public void updateScreen()
    {
        super.updateScreen();
        
        overlay.updateRealtimeElements();
    }
    
    @Override
    public void onGuiClosed()
    {
        scathaPro.getMinecraft().gameSettings.showDebugInfo = debugInfoShownBefore;
    }
}
