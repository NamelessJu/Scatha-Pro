package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alertmodes.CustomAlertMode;
import com.namelessju.scathapro.gui.elements.AlertModeSettingButton;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.SubMenuButton;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class SettingsGui extends ScathaProGui implements GuiSlider.ISlider {
    
    @Override
    public String getTitle() {
        return "Settings";
    }
    
    private GuiButton alertModeSettingButton;
    private GuiButton customAlertModeEditButton;
    
    private boolean modeChanged = false;
    
    public SettingsGui(GuiScreen parentGui) {
        super(parentGui);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        
        buttonList.add(new SubMenuButton(504704004, width / 2 - 155, height / 6 - 12, 150, 20, "Overlay...", this, OverlaySettingsGui.class));
        buttonList.add(new SubMenuButton(504704005, width / 2 + 5, height / 6 - 12, 150, 20, "Alerts...", this, AlertSettingsGui.class));
        
        buttonList.add(new GuiSlider(504704014, width / 2 - 155, height / 6 + 24 - 12, 150, 20, "Mod Sounds Volume: ", "%", 0, 100, ScathaPro.getInstance().config.getDouble(Config.Key.soundsVolume) * 100, false, true, this));
        buttonList.add(new BooleanSettingButton(504704015, width / 2 + 5, height / 6 + 24 - 12, 150, 20, "Mute C. Hollows sounds", Config.Key.muteOtherSounds));

        buttonList.add(alertModeSettingButton = new AlertModeSettingButton(504704006, width / 2 - 155, height / 6 + 48 - 6, 150, 20, "Mode"));
        buttonList.add(customAlertModeEditButton = new SubMenuButton(504704013, width / 2 - 45, height / 6 + 48 - 6, 40, 20, "Edit...", this, CustomAlertModeGui.class));
        updateModeButtons();
        
        buttonList.add(new BooleanSettingButton(504704011, width / 2 + 5, height / 6 + 48 - 6, 150, 20, "Bestiary Kills Parsing", Config.Key.automaticStatsParsing));
        
        buttonList.add(new BooleanSettingButton(504704016, width / 2 - 155, height / 6 + 72 - 6, 150, 20, "Dry Streak Message", Config.Key.dryStreakMessage));
        buttonList.add(new BooleanSettingButton(504704012, width / 2 + 5, height / 6 + 72 - 6, 150, 20, "Worm Spawn Timer", Config.Key.wormSpawnTimer));
        
        buttonList.add(new BooleanSettingButton(504704007, width / 2 - 155, height / 6 + 96 - 6, 150, 20, "Show Rotation Angles", Config.Key.showRotationAngles));
        buttonList.add(new BooleanSettingButton(504704008, width / 2 + 5, height / 6 + 96 - 6, 150, 20, "Chat Copy Button", Config.Key.chatCopy));
        
        buttonList.add(new BooleanSettingButton(504704010, width / 2 - 155, height / 6 + 120 - 6, 150, 20, "Auto Update Checks", Config.Key.automaticUpdateChecks));
        buttonList.add(new BooleanSettingButton(504704009, width / 2 + 5, height / 6 + 120 - 6, 150, 20, "Auto Backups", Config.Key.automaticBackups));
        
        buttonList.add(new DoneButton(504704099, width / 2 - 100, height / 6 + 168, 200, 20, "Done", this));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
    	super.actionPerformed(button);
    	
        if (button.enabled) {
            switch (button.id) {
                case 504704006:
                	modeChanged = true;
                    break;
            }
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        if (modeChanged) {
        	/* if this is immediately done in actionPerformed(),
        	 * the custom mode edit button might instantly get pressed
        	 * if the mouse is over it while pressing the mode swapper button
        	 * to fix this, the mode buttons are updated after all click checks are done
        	 */
        	updateModeButtons();
        	modeChanged = false;
        }
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        if (slider.enabled) {
            switch (slider.id) {
                case 504704014:
                    double volume = (double) slider.getValueInt() / 100;
                    
                    Config config = ScathaPro.getInstance().config;
                    config.set(Config.Key.soundsVolume, volume);
                    config.save();
                    break;
            }
        }
    }
    
    private void updateModeButtons() {
    	if (ScathaPro.getInstance().alertModeManager.getCurrentMode() instanceof CustomAlertMode) {
    		alertModeSettingButton.width = 107;
    		customAlertModeEditButton.visible = true;
    	}
    	else {
    		alertModeSettingButton.width = 150;
    		customAlertModeEditButton.visible = false;
    	}
    }
    
}
