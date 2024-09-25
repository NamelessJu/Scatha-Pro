package com.namelessju.scathapro.gui.menus.alertconfig;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.gui.menus.ScathaProGui;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class HighHeatAlertConfigGui extends ScathaProGui implements GuiSlider.ISlider
{
    private final Config config;

    public HighHeatAlertConfigGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
        
        config = scathaPro.getConfig();
    }
    
    @Override
    public String getTitle()
    {
        return Alert.highHeat.alertName + " Configuration";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        addGridButton(new GuiSlider(1, 0, 0, 0, 0, "Trigger Heat Value: ", "", 90, 100, config.getInt(Config.Key.highHeatAlertTriggerValue), false, true, this), true);
        addDoneButton();
    }
    
    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {

        if (!slider.enabled) return;
        switch (slider.id)
        {
            case 1:
                config.set(Config.Key.highHeatAlertTriggerValue, slider.getValueInt());
                config.save();
                break;
        }
    }
}
