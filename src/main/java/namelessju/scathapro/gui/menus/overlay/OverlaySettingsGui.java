package namelessju.scathapro.gui.menus.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.ConfigGui;
import namelessju.scathapro.overlay.Overlay;
import net.minecraft.client.gui.GuiScreen;

public abstract class OverlaySettingsGui extends ConfigGui
{
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
        
        overlay = scathaPro.getOverlay();
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        debugInfoShownBefore = scathaPro.getMinecraft().gameSettings.showDebugInfo;
        scathaPro.getMinecraft().gameSettings.showDebugInfo = false;

        overlay.updateRealtimeElements();
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
        super.onGuiClosed();
        
        scathaPro.getMinecraft().gameSettings.showDebugInfo = debugInfoShownBefore;
    }
}
