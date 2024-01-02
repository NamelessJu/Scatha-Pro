package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.OverlayManager;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.DoneButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class OverlaySettingsGui extends ScathaProGui implements GuiSlider.ISlider {

	private OverlayManager overlayManager = ScathaPro.getInstance().overlayManager;
	private Config config = ScathaPro.getInstance().config;
    
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
        
        buttonList.add(new BooleanSettingButton(504704101, width / 2 - 155, height - 118 - 6, 150, 20, "UI Overlay", Config.Key.overlay));
        
        double overlayX = config.getDouble(Config.Key.overlayX);
        GuiSlider overlayXSlider = new GuiSlider(504704102, width / 2 + 5, height - 118 - 6, 150, 20, "Overlay X Position: ", "%", -1, 100, overlayX >= 0 ? overlayX * 100 : -1, false, true, this);
        if (overlayX < 0) setSliderDefaultString(overlayXSlider);
        buttonList.add(overlayXSlider);
        
        double overlayY = config.getDouble(Config.Key.overlayY);
        GuiSlider overlayYSlider = new GuiSlider(504704103, width / 2 + 5, height - 94 - 6, 150, 20, "Overlay Y Position: ", "%", -1, 100, overlayY >= 0 ? overlayY * 100 : -1, false, true, this);
        if (overlayY < 0) setSliderDefaultString(overlayYSlider);
        buttonList.add(overlayYSlider);
        
        buttonList.add(new GuiSlider(504704104, width / 2 - 155, height - 94 - 6, 150, 20, "Overlay Scale: ", "%", 50, 150, config.getDouble(Config.Key.overlayScale) * 100, false, true, this));
        
        buttonList.add(new DoneButton(504704199, width / 2 - 100, height - 70, 200, 20, "Done", this));
        
        overlayManager.updateOverlayFull();
    }
    
    @Override
    protected void drawCustomBackground() {
    	overlayManager.drawOverlay();
    }
    

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
    	super.actionPerformed(button);
    	
        if (button.enabled) {
            switch (button.id) {
            
                case 504704101:
                	overlayManager.updateVisibility();
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
                    
                    overlayManager.updatePosition();
                    
                    if (overlayX < 0) setSliderDefaultString(slider);
                    break;
                    
                case 504704103:
                    double overlayY = (double) slider.getValueInt() / 100;
                    
                    config.set(Config.Key.overlayY, overlayY >= 0 ? overlayY : -1);
                    config.save();
                    
                    overlayManager.updatePosition();
                    
                    if (overlayY < 0) setSliderDefaultString(slider);
                    break;
                    
                case 504704104:
                	config.set(Config.Key.overlayScale, (double) slider.getValueInt() / 100);
                	config.save();
                    
                    overlayManager.updateScale();
                    overlayManager.updatePosition();
                    break;
            }
        }
    }
}
