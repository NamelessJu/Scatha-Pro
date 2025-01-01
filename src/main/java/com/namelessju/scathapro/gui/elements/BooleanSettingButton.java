package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.managers.Config;

public class BooleanSettingButton extends ScathaProButton implements IClickActionButton
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
        Config config = ScathaPro.getInstance().getConfig();
        config.set(configSetting, !config.getBoolean(configSetting));
        config.save();
        updateText();
    }
    
    private void updateText()
    {
        this.displayString = (text != null ? text + ": " : "") + (isSettingEnabled() ? "ON" : "OFF");
    }
    
    public boolean isSettingEnabled()
    {
        return ScathaPro.getInstance().getConfig().getBoolean(configSetting);
    }
}
