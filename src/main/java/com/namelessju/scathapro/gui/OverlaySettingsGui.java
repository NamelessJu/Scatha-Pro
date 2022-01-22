package com.namelessju.scathapro.gui;

import java.io.IOException;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;

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
    
    
    private final Config config = Config.getInstance();
    
    public OverlaySettingsGui(GuiScreen parentGui) {
        super(parentGui);
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        Minecraft.getMinecraft().gameSettings.showDebugInfo = false;

        buttonList.add(new GuiButton(504704101, width / 2 - 155, height - 118 - 6, 150, 20, getOverlayString()));
        
        double overlayX = config.getDouble(Config.Key.overlayX);
        GuiSlider overlayXSlider = new GuiSlider(504704102, width / 2 + 5, height - 118 - 6, 150, 20, "Overlay X Position: ", "%", -1, 100, overlayX >= 0 ? overlayX * 100 : -1, false, true, this);
        if (overlayX < 0) setSliderDefaultString(overlayXSlider);
        buttonList.add(overlayXSlider);
        
        double overlayY = config.getDouble(Config.Key.overlayY);
        GuiSlider overlayYSlider = new GuiSlider(504704103, width / 2 + 5, height - 94 - 6, 150, 20, "Overlay Y Position: ", "%", -1, 100, overlayY >= 0 ? overlayY * 100 : -1, false, true, this);
        if (overlayY < 0) setSliderDefaultString(overlayYSlider);
        buttonList.add(overlayYSlider);
        
        buttonList.add(new GuiSlider(504704104, width / 2 - 155, height - 94 - 6, 150, 20, "Overlay Scale: ", "%", 10, 150, config.getDouble(Config.Key.overlayScale) * 100, false, true, this));
        
        buttonList.add(new GuiButton(504704199, width / 2 - 100, height - 70, 200, 20, "Done"));
        
        ScathaPro.getInstance().updateOverlayFull();
    }
    
    @Override
    protected void drawCustomBackground() {
        ScathaPro.getInstance().drawOverlay();
    }
    

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            switch (button.id) {
            
                case 504704101:
                    config.set(Config.Key.overlay, !config.getBoolean(Config.Key.overlay));
                    config.save();
                    
                    ScathaPro.getInstance().updateOverlayVisibility();
                    
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
                    
                    config.set(Config.Key.overlayX, overlayX >= 0 ? overlayX : -1);
                    config.save();
                    
                    ScathaPro.getInstance().updateOverlayPosition();
                    
                    if (overlayX < 0) setSliderDefaultString(slider);
                    break;
                    
                case 504704103:
                    double overlayY = (double) slider.getValueInt() / 100;
                    
                    config.set(Config.Key.overlayY, overlayY >= 0 ? overlayY : -1);
                    config.save();
                    
                    ScathaPro.getInstance().updateOverlayPosition();
                    
                    if (overlayY < 0) setSliderDefaultString(slider);
                    break;
                    
                case 504704104:
                    config.set(Config.Key.overlayScale, (double) slider.getValueInt() / 100);
                    config.save();
                    
                    ScathaPro.getInstance().updateOverlayScale();
                    ScathaPro.getInstance().updateOverlayPosition();
                    break;
            }
        }
    }

    
    private String getOverlayString() {
        boolean enabled = config.getBoolean(Config.Key.overlay);
        return "UI Overlay: " + getEnabledString(enabled);
    }
}
