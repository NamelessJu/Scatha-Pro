package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.Config;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BooleanSettingButton extends GuiButton implements ClickActionButton {
	
	public Config.Key configSetting;
	public String text;
	
    public BooleanSettingButton(int buttonId, int x, int y, int widthIn, int heightIn, String text, Config.Key configSetting) {
        super(buttonId, x, y, widthIn, heightIn, "");
        
        this.text = text;
        this.configSetting = configSetting;

		updateText();
    }
    
	@Override
	public void click() {
		Config.instance.set(configSetting, !Config.instance.getBoolean(configSetting));
		Config.instance.save();
		updateText();
	}
	
	private void updateText() {
		boolean enabled = Config.instance.getBoolean(configSetting);
		this.displayString = text + ": " + (enabled ? "ON" : "OFF");
	}
}
