package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.AlertMode;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.OverlayManager;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AlertModeSettingButton extends GuiButton implements ClickActionButton {
	
	public String text;
	
    public AlertModeSettingButton(int buttonId, int x, int y, int widthIn, int heightIn, String text) {
        super(buttonId, x, y, widthIn, heightIn, "");
        
        this.text = text;

		updateText();
    }
    
	@Override
	public void click() {
        int currentMode = Config.instance.getInt(Config.Key.mode);
        
        int nextMode = currentMode + 1;
        if (nextMode >= AlertMode.values().length) nextMode = 0;
        
        Config.instance.set(Config.Key.mode, nextMode);
        Config.instance.save();
        
        OverlayManager.instance.updateScathaPetImage();
        
		updateText();
	}
	
	private void updateText() {
        displayString = text + ": " + AlertMode.getCurrentMode().name;
	}
}
