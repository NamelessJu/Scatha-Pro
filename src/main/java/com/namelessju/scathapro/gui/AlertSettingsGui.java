package com.namelessju.scathapro.gui;

import java.io.IOException;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class AlertSettingsGui extends ScathaProGui implements GuiSlider.ISlider {
    
    @Override
    public String getTitle() {
        return "Alert Settings";
    }
    

    public AlertSettingsGui(GuiScreen parentGui) {
        super(parentGui);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        
        double volume = Config.instance.getDouble(Config.Key.volume);
        buttonList.add(new GuiSlider(504704401, width / 2 - 155, height / 6 - 6, 310, 20, "Alert Volume: ", "%", 0, 100, volume * 100, false, true, this));

        buttonList.add(new GuiButton(504704402, width / 2 + 5, height / 6 + 24 - 6, 150, 20, getWormAlertString()));
        buttonList.add(new GuiButton(504704403, width / 2 - 155, height / 6 + 48 - 6, 150, 20, getScathaAlertString()));
        buttonList.add(new GuiButton(504704404, width / 2 - 155, height / 6 + 24 - 6, 150, 20, getWormPreAlertString()));
        buttonList.add(new GuiButton(504704405, width / 2 + 5, height / 6 + 48 - 6, 150, 20, getWallAlertString()));
        buttonList.add(new GuiButton(504704406, width / 2 - 155, height / 6 + 72 - 6, 150, 20, getPetAlertString()));
        
        buttonList.add(new GuiButton(504704499, width / 2 - 100, height / 6 + 168, 200, 20, "Done"));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            switch (button.id) {
            
                case 504704402:
                    Config.instance.set(Config.Key.wormAlert, !Config.instance.getBoolean(Config.Key.wormAlert));
                    Config.instance.save();
                    
                    button.displayString = getWormAlertString();
                    break;
            
                case 504704403:
                    Config.instance.set(Config.Key.scathaAlert, !Config.instance.getBoolean(Config.Key.scathaAlert));
                    Config.instance.save();
                    
                    button.displayString = getScathaAlertString();
                    break;
                    
                case 504704404:
                    Config.instance.set(Config.Key.wormPreAlert, !Config.instance.getBoolean(Config.Key.wormPreAlert));
                    Config.instance.save();
                    
                    button.displayString = getWormPreAlertString();
                    break;
                    
                case 504704405:
                    Config.instance.set(Config.Key.wallAlert, !Config.instance.getBoolean(Config.Key.wallAlert));
                    Config.instance.save();
                    
                    button.displayString = getWallAlertString();
                    break;
                    
                case 504704406:
                    boolean enabled = !Config.instance.getBoolean(Config.Key.petAlert);
                    Config.instance.set(Config.Key.petAlert, enabled);
                    Config.instance.save();
                    
                    if (enabled) ScathaPro.getInstance().resetPreviousScathaPets();
                    
                    button.displayString = getPetAlertString();
                    break;
                
                case 504704499:
                    openParentGui();
                    break;
            }
        }
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        if (slider.enabled) {
            switch (slider.id) {
                case 504704401:
                    double volume = (double) slider.getValueInt() / 100;
                    
                    Config.instance.set(Config.Key.volume, volume);
                    Config.instance.save();
                    break;
            }
        }
    }
    
    
    private String getWormAlertString() {
        boolean enabled = Config.instance.getBoolean(Config.Key.wormAlert);
        return "Worm Spawn Alert: " + getEnabledString(enabled);
    }
    
    private String getScathaAlertString() {
        boolean enabled = Config.instance.getBoolean(Config.Key.scathaAlert);
        return "Scatha Spawn Alert: " + getEnabledString(enabled);
    }
    
    private String getWormPreAlertString() {
        boolean enabled = Config.instance.getBoolean(Config.Key.wormPreAlert);
        return "Worm Pre-Spawn Alert: " + getEnabledString(enabled);
    }
    
    private String getWallAlertString() {
        boolean enabled = Config.instance.getBoolean(Config.Key.wallAlert);
        return "Bedrock Wall Alert: " + getEnabledString(enabled);
    }
    
    private String getPetAlertString() {
        boolean enabled = Config.instance.getBoolean(Config.Key.petAlert);
        return "Scatha Drop Alert: " + getEnabledString(enabled);
    }
    
}
