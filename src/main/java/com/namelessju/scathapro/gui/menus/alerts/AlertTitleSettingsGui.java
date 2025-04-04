package com.namelessju.scathapro.gui.menus.alerts;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.gui.elements.CycleButton;
import com.namelessju.scathapro.gui.elements.ScathaProSlider;
import com.namelessju.scathapro.gui.menus.ScathaProGui;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.overlay.AlertTitleOverlay;
import com.namelessju.scathapro.overlay.elements.OverlayElement;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.config.GuiSlider;

public class AlertTitleSettingsGui extends ScathaProGui implements GuiSlider.ISlider
{
    protected final Config config;
    protected final AlertTitleOverlay alertTitleOverlay;
    
    public AlertTitleSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
        
        config = scathaPro.getConfig();
        alertTitleOverlay = scathaPro.getAlertTitleOverlay();
    }
    
    @Override
    public String getTitle()
    {
        return "Alert Title Position";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        double titleX = config.getDouble(Config.Key.alertTitlePositionX);
        elements.add(new ScathaProSlider(1, width / 2 - 155, height - 45 - 24 - 6, 150, 20, "X Position: ", "%", 0, 100, titleX >= 0 ? titleX * 100 : -1, false, true, this));
        
        double titleY = config.getDouble(Config.Key.alertTitlePositionY);
        elements.add(new ScathaProSlider(2, width / 2 + 5, height - 45 - 24 - 6, 150, 20, "Y Position: ", "%", 0, 100, titleY >= 0 ? titleY * 100 : -1, false, true, this));
        
        elements.add(new ScathaProSlider(3, width / 2 - 155, height - 45 - 48 - 6, 310, 20, "Scale: ", "%", 25, 175, config.getDouble(Config.Key.alertTitleScale) * 100, false, true, this));
        
        CycleButton<OverlayElement.Alignment> alignmentButton = new CycleButton<OverlayElement.Alignment>(4, width / 2 - 155, height - 45 - 72 - 6, 310, 20, "Alignment", CycleButton.EnumOption.from(OverlayElement.Alignment.class, true), config.getEnum(Config.Key.alertTitleAlignment, OverlayElement.Alignment.class), new CycleButton.IOptionChangedListener<OverlayElement.Alignment>() {
            @Override
            public void onChange(CycleButton<OverlayElement.Alignment> button) {
                OverlayElement.Alignment value = button.getSelectedValue();
                config.set(Config.Key.alertTitleAlignment, value != null ? value.name() : "");
                config.save();
                
                scathaPro.getAlertTitleOverlay().updateContentAlignment();
            }
        });
        alignmentButton.setNullOptionName("Automatic");
        elements.add(alignmentButton);
        
        addDoneButton(width / 2 - 100, height - 45, 200, 20);
    }
    
    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (!slider.enabled) return;
        switch (slider.id)
        {
            case 1:
                double x = (double) slider.getValueInt() / 100;

                config.set(Config.Key.alertTitlePositionX, x >= 0 ? x : -1);
                config.save();

                alertTitleOverlay.updatePosition();
                break;

            case 2:
                double y = (double) slider.getValueInt() / 100;

                config.set(Config.Key.alertTitlePositionY, y >= 0 ? y : -1);
                config.save();

                alertTitleOverlay.updatePosition();
                break;

            case 3:
                config.set(Config.Key.alertTitleScale, (double) slider.getValueInt() / 100);
                config.save();
                
                alertTitleOverlay.updateScale();
                break;
        }
    }
    
    @Override
    public boolean hasBackground()
    {
        return false;
    }
    
    @Override
    protected void drawCustomBackground()
    {
        scathaPro.getAlertTitleOverlay().drawStatic(EnumChatFormatting.GREEN + "Example Alert", EnumChatFormatting.GRAY + "This is what alerts will look like");
    }
}
