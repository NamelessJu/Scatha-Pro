package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.OverlayManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class OverlaySettingsGui extends ScathaProGui implements GuiSlider.ISlider {
    
    @Override
    public String getTitle() {
        return "Overlay Settings";
    }

    @Override
    public boolean hasBackground() {
        return false;
    }
    
    
    public OverlaySettingsGui(GuiScreen parentGui) {
        super(parentGui);
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        Minecraft.getMinecraft().gameSettings.showDebugInfo = false;

        buttonList.add(new GuiButton(504704101, width / 2 - 155, height - 118 - 6, 150, 20, getOverlayString()));
        
        double overlayX = Config.instance.getDouble(Config.Key.overlayX);
        GuiSlider overlayXSlider = new GuiSlider(504704102, width / 2 + 5, height - 118 - 6, 150, 20, "Overlay X Position: ", "%", -1, 100, overlayX >= 0 ? overlayX * 100 : -1, false, true, this);
        if (overlayX < 0) setSliderDefaultString(overlayXSlider);
        buttonList.add(overlayXSlider);
        
        double overlayY = Config.instance.getDouble(Config.Key.overlayY);
        GuiSlider overlayYSlider = new GuiSlider(504704103, width / 2 + 5, height - 94 - 6, 150, 20, "Overlay Y Position: ", "%", -1, 100, overlayY >= 0 ? overlayY * 100 : -1, false, true, this);
        if (overlayY < 0) setSliderDefaultString(overlayYSlider);
        buttonList.add(overlayYSlider);
        
        buttonList.add(new GuiSlider(504704104, width / 2 - 155, height - 94 - 6, 150, 20, "Overlay Scale: ", "%", 50, 150, Config.instance.getDouble(Config.Key.overlayScale) * 100, false, true, this));
        
        buttonList.add(new GuiButton(504704199, width / 2 - 100, height - 70, 200, 20, "Done"));
        
        OverlayManager.instance.updateOverlayFull();
    }
    
    @Override
    protected void drawCustomBackground() {
        OverlayManager.instance.drawOverlay();
    }
    

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            switch (button.id) {
            
                case 504704101:
                    Config.instance.set(Config.Key.overlay, !Config.instance.getBoolean(Config.Key.overlay));
                    Config.instance.save();
                    
                    OverlayManager.instance.updateVisibility();
                    
                    button.displayString = getOverlayString();
                    break;
                
                case 504704199:
                    openParentGui();
                    break;
            }
        }
    }
    
    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        if (slider.enabled) {
            switch (slider.id) {
                case 504704102:
                    double overlayX = (double) slider.getValueInt() / 100;
                    
                    Config.instance.set(Config.Key.overlayX, overlayX >= 0 ? overlayX : -1);
                    Config.instance.save();
                    
                    OverlayManager.instance.updatePosition();
                    
                    if (overlayX < 0) setSliderDefaultString(slider);
                    break;
                    
                case 504704103:
                    double overlayY = (double) slider.getValueInt() / 100;
                    
                    Config.instance.set(Config.Key.overlayY, overlayY >= 0 ? overlayY : -1);
                    Config.instance.save();
                    
                    OverlayManager.instance.updatePosition();
                    
                    if (overlayY < 0) setSliderDefaultString(slider);
                    break;
                    
                case 504704104:
                    Config.instance.set(Config.Key.overlayScale, (double) slider.getValueInt() / 100);
                    Config.instance.save();
                    
                    OverlayManager.instance.updateScale();
                    OverlayManager.instance.updatePosition();
                    break;
            }
        }
    }

    
    private String getOverlayString() {
        boolean enabled = Config.instance.getBoolean(Config.Key.overlay);
        return "UI Overlay: " + getEnabledString(enabled);
    }
}
