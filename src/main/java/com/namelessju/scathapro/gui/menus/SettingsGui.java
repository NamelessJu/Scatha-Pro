package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.alertmodes.AlertModeManager;
import com.namelessju.scathapro.alertmodes.CustomAlertMode;
import com.namelessju.scathapro.gui.elements.AlertModeSettingButton;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.SubMenuButton;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class SettingsGui extends ScathaProGui {
    
    @Override
    public String getTitle() {
        return "Settings";
    }
    
    private GuiButton alertModeSettingButton;
    private GuiButton customAlertModeEditButton;
    /*
    private GuiTextField apiKeyTextField;
    private String apiKeyInitialValue;
    private boolean editingApiKey = false;
    */
    
    public SettingsGui(GuiScreen parentGui) {
        super(parentGui);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        
        // apiKeyInitialValue = Config.instance.getString(Config.Key.apiKey);

        /*
        GuiLabel apiKeyLabel = new GuiLabel(fontRendererObj, 1, width / 2 - 155, height / 6 - 1 - 6, 310, 10, Util.Color.GRAY.getValue());
        apiKeyLabel.func_175202_a("API-Key");
        labelList.add(apiKeyLabel);
        
        apiKeyTextField = new GuiTextField(50470401, fontRendererObj, width / 2 - 155, height / 6 + 10 - 6, 265, 20);
        apiKeyTextField.setMaxStringLength(64);
        textFieldList.add(apiKeyTextField);
        updateApiKeyTextField();
        
        buttonList.add(new GuiButton(504704003, width / 2 + 115, height / 6 + 10 - 6, 40, 20, getEditApiKeyString()));
        */
        
        buttonList.add(new SubMenuButton(504704004, width / 2 - 155, height / 6 - 12, 150, 20, "Overlay...", this, OverlaySettingsGui.class));
        buttonList.add(new SubMenuButton(504704005, width / 2 + 5, height / 6 - 12, 150, 20, "Alerts...", this, AlertSettingsGui.class));

        buttonList.add(customAlertModeEditButton = new SubMenuButton(504704013, width / 2 - 45, height / 6 + 48 - 6, 40, 20, "Edit...", this, CustomAlertModeGui.class));
        buttonList.add(alertModeSettingButton = new AlertModeSettingButton(504704006, width / 2 - 155, height / 6 + 48 - 6, 150, 20, "Mode"));
        updateModeButtons();
        
        buttonList.add(new BooleanSettingButton(504704011, width / 2 + 5, height / 6 + 48 - 6, 150, 20, "Bestiary Kills Parsing", Config.Key.automaticStatsParsing));
        buttonList.add(new BooleanSettingButton(504704007, width / 2 - 155, height / 6 + 72 - 6, 150, 20, "Show Rotation Angles", Config.Key.showRotationAngles));
        buttonList.add(new BooleanSettingButton(504704008, width / 2 + 5, height / 6 + 72 - 6, 150, 20, "Chat Copy Button", Config.Key.chatCopy));
        buttonList.add(new BooleanSettingButton(504704010, width / 2 - 155, height / 6 + 96 - 6, 150, 20, "Auto Update Checks", Config.Key.automaticUpdateChecks));
        buttonList.add(new BooleanSettingButton(504704009, width / 2 + 5, height / 6 + 96 - 6, 150, 20, "Auto Backups", Config.Key.automaticBackups));
        buttonList.add(new BooleanSettingButton(504704012, width / 2 - 155, height / 6 + 120 - 6, 150, 20, "Worm Spawn Timer", Config.Key.wormSpawnTimer));
        
        buttonList.add(new DoneButton(504704099, width / 2 - 100, height / 6 + 168, 200, 20, "Done", this));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
    	super.actionPerformed(button);
    	
        if (button.enabled) {
            switch (button.id) {
                case 504704006:
                	updateModeButtons();
                    break;
            }
        }
    }
    
    private void updateModeButtons() {
    	if (AlertModeManager.getCurrentMode() instanceof CustomAlertMode) {
    		alertModeSettingButton.width = 107;
    		customAlertModeEditButton.visible = true;
    	}
    	else {
    		alertModeSettingButton.width = 150;
    		customAlertModeEditButton.visible = false;
    	}
    }
    
    /*
    @Override
    public void onGuiClosed() {
        if (!Config.instance.getString(Config.Key.apiKey).equals(apiKeyInitialValue)) {
            ScathaPro.getInstance().repeatProfilesDataRequest = true;
            if (ScathaPro.getInstance().profilesDataRequestNeeded()) HypixelApiManager.requestProfilesData();
        }
    }
	
    private void updateApiKeyTextField() {
        if (editingApiKey) {
            apiKeyTextField.setText(Config.instance.getString(Config.Key.apiKey));
            apiKeyTextField.setEnabled(true);
        }
        else {
            String apiKey = Config.instance.getString(Config.Key.apiKey);
            
            apiKeyTextField.setText(apiKey.replaceAll(".", "*"));
            apiKeyTextField.setEnabled(false);
        }
    }
    
    private String getEditApiKeyString() {
        return editingApiKey ? "save" : "edit";
    }
    */
    
}
