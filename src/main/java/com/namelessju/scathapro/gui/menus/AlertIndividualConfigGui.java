package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.gui.elements.ScathaProSlider;
import com.namelessju.scathapro.gui.elements.ScathaProLabel;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class AlertIndividualConfigGui extends ScathaProGui implements GuiSlider.ISlider
{
    private final Config config;
    
    public AlertIndividualConfigGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
        
        config = scathaPro.getConfig();
    }
    
    @Override
    public String getTitle()
    {
        return "Alert Configuration";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        elements.add(setGridPosition(new ScathaProLabel(0, 0, 0, 0, Alert.bedrockWall.alertName).setCentered(), GridElementMode.FULL_WIDTH));
        addGridButton(new ScathaProSlider(1, 0, 0, 0, 0, "Max. Trigger Distance: ", " Blocks", 0, 50, config.getInt(Config.Key.bedrockWallAlertTriggerDistance), false, true, this), GridElementMode.FULL_WIDTH);
        
        addGridGap();
        
        elements.add(setGridPosition(new ScathaProLabel(0, 0, 0, 0, Alert.highHeat.alertName).setCentered(), GridElementMode.FULL_WIDTH));
        addGridButton(new ScathaProSlider(2, 0, 0, 0, 0, "Trigger Heat Value: ", "", 90, 100, config.getInt(Config.Key.highHeatAlertTriggerValue), false, true, this), GridElementMode.FULL_WIDTH);
        
        addDoneButton();
    }
    
    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (!slider.enabled) return;
        switch (slider.id)
        {
            case 1:
                config.set(Config.Key.bedrockWallAlertTriggerDistance, slider.getValueInt());
                config.save();
                break;
            
            case 2:
                config.set(Config.Key.highHeatAlertTriggerValue, slider.getValueInt());
                config.save();
                break;
        }
    }
}
