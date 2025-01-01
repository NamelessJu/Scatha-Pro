package com.namelessju.scathapro.gui.menus.overlay;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.ScathaProSlider;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class OverlayPositionSettingsGui extends OverlaySettingsGui implements GuiSlider.ISlider
{
    public OverlayPositionSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Overlay Position";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        double overlayX = config.getDouble(Config.Key.overlayX);
        ScathaProSlider overlayXSlider = new ScathaProSlider(1, width / 2 - 155, height - 45 - 24 - 6, 150, 20, "Overlay X Position: ", "%", -1, 100, overlayX >= 0 ? overlayX * 100 : -1, false, true, this);
        if (overlayX < 0) setSliderTextDefault(overlayXSlider);
        elements.add(overlayXSlider);
        
        double overlayY = config.getDouble(Config.Key.overlayY);
        ScathaProSlider overlayYSlider = new ScathaProSlider(2, width / 2 + 5, height - 45 - 24 - 6, 150, 20, "Overlay Y Position: ", "%", -1, 100, overlayY >= 0 ? overlayY * 100 : -1, false, true, this);
        if (overlayY < 0) setSliderTextDefault(overlayYSlider);
        elements.add(overlayYSlider);
        
        elements.add(new ScathaProSlider(3, width / 2 - 155, height - 45 - 48 - 6, 310, 20, "Overlay Scale: ", "%", 50, 150, config.getDouble(Config.Key.overlayScale) * 100, false, true, this));
        
        addDoneButton(width / 2 - 100, height - 45, 200, 20);
        
        overlay.updateOverlayFull();
    }
    
    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (!slider.enabled) return;
        switch (slider.id)
        {
            case 1:
                double overlayX = (double) slider.getValueInt() / 100;

                config.set(Config.Key.overlayX, overlayX >= 0 ? overlayX : -1);
                config.save();

                overlay.updatePosition();

                if (overlayX < 0) setSliderTextDefault(slider);
                break;

            case 2:
                double overlayY = (double) slider.getValueInt() / 100;

                config.set(Config.Key.overlayY, overlayY >= 0 ? overlayY : -1);
                config.save();

                overlay.updatePosition();

                if (overlayY < 0) setSliderTextDefault(slider);
                break;

            case 3:
                config.set(Config.Key.overlayScale, (double) slider.getValueInt() / 100);
                config.save();

                overlay.updateScale();
                overlay.updatePosition();
                break;
        }
    }
    
}
