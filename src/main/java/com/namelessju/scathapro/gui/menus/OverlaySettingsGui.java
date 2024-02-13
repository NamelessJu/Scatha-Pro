package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.MultiOptionButton;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.OverlayStatsType;
import com.namelessju.scathapro.overlay.Overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class OverlaySettingsGui extends ScathaProGui implements GuiSlider.ISlider
{
    private final Config config;
    private final Overlay overlay;
    
    public OverlaySettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
        
        config = scathaPro.getConfig();
        overlay = scathaPro.getOverlay();
    }
    
    @Override
    public String getTitle()
    {
        return "Overlay Settings";
    }
    
    @Override
    public boolean hasBackground()
    {
        return false;
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        Minecraft.getMinecraft().gameSettings.showDebugInfo = false;
        
        buttonList.add(new BooleanSettingButton(504704101, width / 2 - 155, height - 88 - 6, 150, 20, "UI Overlay", Config.Key.overlay));
        
        double overlayX = config.getDouble(Config.Key.overlayX);
        GuiSlider overlayXSlider = new GuiSlider(504704102, width / 2 + 5, height - 88 - 6, 150, 20, "Overlay X Position: ", "%", -1, 100, overlayX >= 0 ? overlayX * 100 : -1, false, true, this);
        if (overlayX < 0) setSliderTextDefault(overlayXSlider);
        buttonList.add(overlayXSlider);
        
        double overlayY = config.getDouble(Config.Key.overlayY);
        GuiSlider overlayYSlider = new GuiSlider(504704103, width / 2 + 5, height - 64 - 6, 150, 20, "Overlay Y Position: ", "%", -1, 100, overlayY >= 0 ? overlayY * 100 : -1, false, true, this);
        if (overlayY < 0) setSliderTextDefault(overlayYSlider);
        buttonList.add(overlayYSlider);
        
        buttonList.add(new GuiSlider(504704104, width / 2 - 155, height - 64 - 6, 150, 20, "Overlay Scale: ", "%", 50, 150, config.getDouble(Config.Key.overlayScale) * 100, false, true, this));

        buttonList.add(new MultiOptionButton<String>(504704105, width / 2 - 155, height - 112 - 12, 150, 20, "Stats per", OverlayStatsType.values(), config.getString(Config.Key.statsType), new MultiOptionButton.IOptionChangedListener<String>() {
            @Override
            public void onChange(MultiOptionButton<String> button)
            {
                config.set(Config.Key.statsType, button.getSelectedValue());
                config.save();
                scathaPro.getOverlay().setStatsType((OverlayStatsType) button.getSelectedOption());
            }
        }));
        buttonList.add(new MultiOptionButton<Integer>(504704106, width / 2 + 5, height - 112 - 12, 150, 20, "Scatha % Dec. Places", MultiOptionButton.IntegerOption.range(0, 3), config.getInt(Config.Key.scathaPercentageDecimalDigits), new MultiOptionButton.IOptionChangedListener<Integer>() {
            @Override
            public void onChange(MultiOptionButton<Integer> button)
            {
                config.set(Config.Key.scathaPercentageDecimalDigits, button.getSelectedValue());
                config.save();
                scathaPro.getOverlay().updateTotalKills();
            }
        }));
        
        buttonList.add(new DoneButton(504704199, width / 2 - 100, height - 40, 200, 20, "Done", this));
        
        overlay.updateOverlayFull();
    }
    
    @Override
    protected void drawCustomBackground()
    {
        overlay.drawOverlay();
    }
    

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        super.actionPerformed(button);
        
        if (button.enabled)
        {
            switch (button.id)
            {
                case 504704101:
                    overlay.updateVisibility();
                    break;
            }
        }
    }
    
    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (slider.enabled)
        {
            switch (slider.id)
            {
                case 504704102:
                    double overlayX = (double) slider.getValueInt() / 100;
                    
                    config.set(Config.Key.overlayX, overlayX >= 0 ? overlayX : -1);
                    config.save();
                    
                    overlay.updatePosition();
                    
                    if (overlayX < 0) setSliderTextDefault(slider);
                    break;
                    
                case 504704103:
                    double overlayY = (double) slider.getValueInt() / 100;
                    
                    config.set(Config.Key.overlayY, overlayY >= 0 ? overlayY : -1);
                    config.save();
                    
                    overlay.updatePosition();
                    
                    if (overlayY < 0) setSliderTextDefault(slider);
                    break;
                    
                case 504704104:
                    config.set(Config.Key.overlayScale, (double) slider.getValueInt() / 100);
                    config.save();
                    
                    overlay.updateScale();
                    overlay.updatePosition();
                    break;
            }
        }
    }
}
