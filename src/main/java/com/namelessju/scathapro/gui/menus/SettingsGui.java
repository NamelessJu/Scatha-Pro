package com.namelessju.scathapro.gui.menus;

import java.io.IOException;

import com.namelessju.scathapro.API;
import com.namelessju.scathapro.AlertMode;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.OverlayManager;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class SettingsGui extends ScathaProGui {
    
    @Override
    public String getTitle() {
        return "Settings";
    }
    
    
    GuiTextField apiKeyTextField;
    private String apiKeyInitialValue;
    private boolean editingApiKey = false;
    
    public SettingsGui(GuiScreen parentGui) {
        super(parentGui);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        
        apiKeyInitialValue = Config.instance.getString(Config.Key.apiKey);
        
        GuiLabel apiKeyLabel = new GuiLabel(fontRendererObj, 1, width / 2 - 155, height / 6 - 1 - 6, 310, 10, Util.Color.GRAY.getValue());
        apiKeyLabel.func_175202_a("API-Key");
        labelList.add(apiKeyLabel);
        
        apiKeyTextField = new GuiTextField(50470401, fontRendererObj, width / 2 - 155, height / 6 + 10 - 6, 265, 20);
        apiKeyTextField.setMaxStringLength(64);
        textFieldList.add(apiKeyTextField);
        updateApiKeyTextField();
        
        buttonList.add(new GuiButton(504704003, width / 2 + 115, height / 6 + 10 - 6, 40, 20, getEditApiKeyString()));

        buttonList.add(new GuiButton(504704004, width / 2 - 155, height / 6 + 48 - 6, 150, 20, "Overlay..."));
        buttonList.add(new GuiButton(504704005, width / 2 + 5, height / 6 + 48 - 6, 150, 20, "Alerts..."));
        buttonList.add(new GuiButton(504704006, width / 2 - 155, height / 6 + 72 - 6, 150, 20, getModeString()));
        buttonList.add(new GuiButton(504704007, width / 2 + 5, height / 6 + 72 - 6, 150, 20, getShowRotationAnglesString()));
        buttonList.add(new GuiButton(504704008, width / 2 - 155, height / 6 + 96 - 6, 150, 20, getChatCopyString()));
        buttonList.add(new GuiButton(504704009, width / 2 + 5, height / 6 + 96 - 6, 150, 20, getAutomaticBackupsString()));
        buttonList.add(new GuiButton(504704010, width / 2 - 155, height / 6 + 120 - 6, 150, 20, getAutomaticUpdateChecksString()));
        
        buttonList.add(new GuiButton(504704099, width / 2 - 100, height / 6 + 168, 200, 20, "Done"));
    }
    
    @Override
    public void onGuiClosed() {
        if (!Config.instance.getString(Config.Key.apiKey).equals(apiKeyInitialValue)) {
            ScathaPro.getInstance().repeatProfilesDataRequest = true;
            if (ScathaPro.getInstance().profilesDataRequestNeeded()) API.requestProfilesData();
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            switch (button.id) {
            
                case 504704003:
                    if (editingApiKey) {
                        String apiKey = apiKeyTextField.getText();
                        apiKey = apiKey.replace(" ", "");
            
                        Config.instance.set(Config.Key.apiKey, apiKey);
                        Config.instance.save();
                    }
                    
                    editingApiKey = !editingApiKey;
                    
                    updateApiKeyTextField();
                    
                    button.displayString = getEditApiKeyString();
                    break;
            
                case 504704004:
                    openGui(new OverlaySettingsGui(this));
                    break;
            
                case 504704005:
                    openGui(new AlertSettingsGui(this));
                    break;
                    
                case 504704006:
                    int currentMode = Config.instance.getInt(Config.Key.mode);
                    
                    int nextMode = currentMode + 1;
                    if (nextMode >= AlertMode.values().length) nextMode = 0;
                    
                    Config.instance.set(Config.Key.mode, nextMode);
                    Config.instance.save();
                    
                    OverlayManager.instance.updateScathaPetImage();
                    
                    button.displayString = getModeString();
                    break;
                    
                case 504704007:
                    Config.instance.set(Config.Key.showRotationAngles, !Config.instance.getBoolean(Config.Key.showRotationAngles));
                    Config.instance.save();
                    
                    button.displayString = getShowRotationAnglesString();
                    break;
                
                case 504704008:
                    Config.instance.set(Config.Key.chatCopy, !Config.instance.getBoolean(Config.Key.chatCopy));
                    Config.instance.save();
                    
                    button.displayString = getChatCopyString();
                    break;
                
                case 504704009:
                    Config.instance.set(Config.Key.automaticBackups, !Config.instance.getBoolean(Config.Key.automaticBackups));
                    Config.instance.save();
                    
                    button.displayString = getAutomaticBackupsString();
                    break;
                    
                case 504704010:
                    Config.instance.set(Config.Key.automaticUpdateChecks, !Config.instance.getBoolean(Config.Key.automaticUpdateChecks));
                    Config.instance.save();
                    
                    button.displayString = getAutomaticUpdateChecksString();
                    break;
                
                case 504704099:
                    openParentGui();
                    break;
            }
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
    
    private String getShowRotationAnglesString() {
        boolean enabled = Config.instance.getBoolean(Config.Key.showRotationAngles);
        return "Show Rotation Angles: " + getEnabledString(enabled);
    }
    
    private String getModeString() {
        return "Mode: " + AlertMode.getCurrentMode().name;
    }
    
    private String getChatCopyString() {
        boolean enabled = Config.instance.getBoolean(Config.Key.chatCopy);
        return "Chat Copy Button: " + getEnabledString(enabled);
    }
    
    private String getAutomaticBackupsString() {
        boolean enabled = Config.instance.getBoolean(Config.Key.automaticBackups);
        return "Auto Backups: " + getEnabledString(enabled);
    }

    private String getAutomaticUpdateChecksString() {
        boolean enabled = Config.instance.getBoolean(Config.Key.automaticUpdateChecks);
        return "Auto Update Checks: " + getEnabledString(enabled);
    }
    
}
