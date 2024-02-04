package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.gui.GuiButton;

public class BooleanSettingButton extends GuiButton implements IClickActionButton
{
    public Config.Key configSetting;
    public String text;
    
    public BooleanSettingButton(int buttonId, int x, int y, int widthIn, int heightIn, String text, Config.Key configSetting)
    {
        super(buttonId, x, y, widthIn, heightIn, "");
        
        this.text = text;
        this.configSetting = configSetting;

        updateText();
    }
    
    @Override
    public void click()
    {
        Config config = ScathaPro.getInstance().config;
        config.set(configSetting, !config.getBoolean(configSetting));
        config.save();
        updateText();
    }
    
    private void updateText()
    {
        boolean enabled = ScathaPro.getInstance().config.getBoolean(configSetting);
        this.displayString = (text != null ? text + ": " : "") + (enabled ? "ON" : "OFF");
    }
}
