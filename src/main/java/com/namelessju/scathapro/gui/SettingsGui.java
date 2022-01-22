package com.namelessju.scathapro.gui;

import java.io.IOException;

import com.namelessju.scathapro.API;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.Util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.client.config.GuiSlider;

public class SettingsGui extends ScathaProGui implements GuiSlider.ISlider {
    
    @Override
    public String getTitle() {
        return "Settings";
    }
    
    
    private final Config config = Config.getInstance();
    
    private String apiKeyInitialValue;
    
    public SettingsGui(GuiScreen parentGui) {
        super(parentGui);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        
        apiKeyInitialValue = config.getString(Config.Key.apiKey);
        
        GuiLabel apiKeyLabel = new GuiLabel(fontRendererObj, 1, width / 2 - 155, height / 6 - 1 - 6, 310, 10, Util.Color.GRAY.getValue());
        apiKeyLabel.func_175202_a("API-Key");
        labelList.add(apiKeyLabel);
        
        GuiTextField apiKeyTextField = new GuiTextField(50470401, fontRendererObj, width / 2 - 155, height / 6 + 10 - 6, 310, 20);
        apiKeyTextField.setMaxStringLength(64);
        apiKeyTextField.setText(apiKeyInitialValue);
        textFieldList.add(apiKeyTextField);
        
        double volume = config.getDouble(Config.Key.volume);
        GuiSlider volumeSlider = new GuiSlider(504704002, width / 2 - 155, height / 6 + 48 - 6, 310, 20, "Alert Volume: ", "%", 0, 100, volume * 100, false, true, this);
        buttonList.add(volumeSlider);

        buttonList.add(new GuiButton(504704003, width / 2 - 155, height / 6 + 72 - 6, 150, 20, "Overlay..."));
        buttonList.add(new GuiButton(504704004, width / 2 + 5, height / 6 + 72 - 6, 150, 20, getWormPreAlertString()));
        buttonList.add(new GuiButton(504704005, width / 2 - 155, height / 6 + 96 - 6, 150, 20, getWormAlertString()));
        buttonList.add(new GuiButton(504704006, width / 2 + 5, height / 6 + 96 - 6, 150, 20, getScathaAlertString()));
        buttonList.add(new GuiButton(504704007, width / 2 - 155, height / 6 + 120 - 6, 150, 20, getWallAlertString()));
        buttonList.add(new GuiButton(504704008, width / 2 + 5, height / 6 + 120 - 6, 150, 20, getPetAlertString()));
        buttonList.add(new GuiButton(504704009, width / 2 - 155, height / 6 + 144 - 6, 150, 20, getModeString()));
        buttonList.add(new GuiButton(504704010, width / 2 + 5, height / 6 + 144 - 6, 150, 20, getChatCopyString()));
        
        buttonList.add(new GuiButton(504704099, width / 2 - 100, height / 6 + 168, 200, 20, "Done"));
    }
    
    @Override
    public void onGuiClosed() {
        if (!config.getString(Config.Key.apiKey).equals(apiKeyInitialValue) && ScathaPro.getInstance().profilesDataRequestNeeded()) API.requestProfilesData();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            switch (button.id) {
            
                case 504704003:
                    openGui(new OverlaySettingsGui(this));
                    break;
                    
                case 504704004:
                    config.set(Config.Key.wormPreAlert, !config.getBoolean(Config.Key.wormPreAlert));
                    config.save();
                    
                    button.displayString = getWormPreAlertString();
                    break;
            
                case 504704005:
                    config.set(Config.Key.wormAlert, !config.getBoolean(Config.Key.wormAlert));
                    config.save();
                    
                    button.displayString = getWormAlertString();
                    break;
            
                case 504704006:
                    config.set(Config.Key.scathaAlert, !config.getBoolean(Config.Key.scathaAlert));
                    config.save();
                    
                    button.displayString = getScathaAlertString();
                    break;
                    
                case 504704007:
                    config.set(Config.Key.wallAlert, !config.getBoolean(Config.Key.wallAlert));
                    config.save();
                    
                    button.displayString = getWallAlertString();
                    break;
                    
                case 504704008:
                    boolean enabled = !config.getBoolean(Config.Key.petAlert);
                    config.set(Config.Key.petAlert, enabled);
                    config.save();
                    
                    if (enabled) ScathaPro.getInstance().resetPreviousScathaPets();
                    
                    button.displayString = getPetAlertString();
                    break;
                    
                case 504704009:
                    int currentMode = config.getInt(Config.Key.mode);
                    
                    int nextMode = currentMode + 1;
                    if (nextMode > 2) nextMode = 0;
                    
                    config.set(Config.Key.mode, nextMode);
                    config.save();
                    
                    ScathaPro.getInstance().updateOverlayScathaPetImage();
                    
                    button.displayString = getModeString();
                    break;
                
                case 504704010:
                    config.set(Config.Key.chatCopy, !config.getBoolean(Config.Key.chatCopy));
                    config.save();
                    
                    button.displayString = getChatCopyString();
                    break;
                
                case 504704099:
                    openParentGui();
                    break;
            }
        }
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        if (slider.enabled) {
            switch (slider.id) {
                case 504704002:
                    double volume = (double) slider.getValueInt() / 100;
                    
                    config.set(Config.Key.volume, volume);
                    config.save();
                    break;
            }
        }
    }
    
    @Override
    protected void textFieldTyped(GuiTextField textField) {
        switch (textField.getId()) {
            case 50470401:
                String apiKey = textField.getText();
                apiKey = apiKey.replace(" ", "");
    
                config.set(Config.Key.apiKey, apiKey);
                config.save();
                
                ScathaPro.getInstance().repeatProfilesDataRequest = true;
                
                if (!textField.getText().equals(apiKey)) textField.setText(apiKey);
                break;
        }
    }
    
    
    private String getWormAlertString() {
        boolean enabled = config.getBoolean(Config.Key.wormAlert);
        return "Worm Spawn Alert: " + getEnabledString(enabled);
    }
    
    private String getScathaAlertString() {
        boolean enabled = config.getBoolean(Config.Key.scathaAlert);
        return "Scatha Spawn Alert: " + getEnabledString(enabled);
    }
    
    private String getWormPreAlertString() {
        boolean enabled = config.getBoolean(Config.Key.wormPreAlert);
        return "Worm Pre-Spawn Alert: " + getEnabledString(enabled);
    }
    
    private String getWallAlertString() {
        boolean enabled = config.getBoolean(Config.Key.wallAlert);
        return "Bedrock Wall Alert: " + getEnabledString(enabled);
    }
    
    private String getPetAlertString() {
        boolean enabled = config.getBoolean(Config.Key.petAlert);
        return "Scatha Drop Alert: " + getEnabledString(enabled);
    }
    
    private String getModeString() {
        int mode = config.getInt(Config.Key.mode);

        String modeName;
        
        switch (mode) {
            case 0:
                modeName = "Normal";
                break;
            case 1:
                modeName = "Meme";
                break;
            case 2:
                modeName = "Anime";
                break;
            default:
                modeName = "unknown";
        }
        
        return "Mode: " + modeName;
    }
    
    private String getChatCopyString() {
        boolean enabled = config.getBoolean(Config.Key.chatCopy);
        return "Chat Copy Button: " + getEnabledString(enabled);
    }
}
