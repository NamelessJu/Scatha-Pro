package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alertmodes.AlertMode;
import com.namelessju.scathapro.alertmodes.AlertModeManager;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AlertModeSettingButton extends GuiButton implements IClickActionButton {
	
	private ScathaPro scathaPro = ScathaPro.getInstance();
	private AlertModeManager alertModeManager = scathaPro.alertModeManager;
	
	public String text;
	
    public AlertModeSettingButton(int buttonId, int x, int y, int widthIn, int heightIn, String text) {
        super(buttonId, x, y, widthIn, heightIn, "");
        
        this.text = text;

		updateText();
    }
    
	@Override
	public void click() {
		AlertMode[] allModes = alertModeManager.getAllModes();
		AlertMode currentMode = alertModeManager.getCurrentMode();
        
        int nextModeIndex = 0;
        
        for (int i = 0; i < allModes.length; i ++) {
        	if (allModes[i] == currentMode) {
        		nextModeIndex = i + 1;
        		break;
        	}
        }
        
        if (nextModeIndex >= allModes.length) nextModeIndex = 0;
        
        Config config = scathaPro.config;
        config.set(Config.Key.mode, allModes[nextModeIndex].id);
        config.save();
        
        scathaPro.overlayManager.updateScathaPetImage();
        
		updateText();
	}
	
	private void updateText() {
        displayString = text + ": " + alertModeManager.getCurrentMode().name;
	}
}
