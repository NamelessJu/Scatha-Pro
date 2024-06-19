package com.namelessju.scathapro.gui.elements;

import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.alerts.AlertTitle;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.util.JsonUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

public class CustomAlertModeEditGuiList extends ScathaProGuiList
{
    private final CustomAlertModeManager customAlertModeManager;

    private final String customModeId;
    private final JsonObject modeProperties;
    
    private FileDialog activeFileDialog = null;
    private Alert lastAlertSoundPlayed = null;
    
    public CustomAlertModeEditGuiList(GuiScreen gui, String customModeId)
    {
        super(gui, 75, gui.height - 40, 110);
        
        customAlertModeManager = ScathaPro.getInstance().getCustomAlertModeManager();
        
        this.customModeId = customModeId;
        modeProperties = customAlertModeManager.loadSubmodeProperties(customModeId);
        
        Alert[] alerts = Alert.values();
        for (Alert alert : alerts)
        {
            listEntries.add(new AlertEditEntry(alert));
        }
    }
    
    public void resize()
    {
        this.width = gui.width;
        this.height = gui.height;
        this.bottom = gui.height - 32;
        this.right = this.width;
    }
    
    public void onGuiClosed()
    {
        if (activeFileDialog != null)
        {
            activeFileDialog.setVisible(false);
        }

        Alert[] alerts = Alert.values();
        for (Alert alert : alerts)
        {
            alert.stopSound();
        }
    }
    
    public boolean saveChanges()
    {
        boolean reloadRequired = false;
        for (ListEntry entry : listEntries)
        {
            if (entry instanceof ISaveableEntry)
            {
                if (((ISaveableEntry) entry).saveChanges()) reloadRequired = true;
            }
        }
        return reloadRequired;
    }

    public static interface ISaveableEntry
    {
        /**
         * @return <code>boolean</code> Whether reloading resources is required
         */
        public boolean saveChanges();
    }
    
    private class AlertEditEntry extends ListEntry implements ISaveableEntry
    {
        private final File audioResetFlag = new File("");
        private final File alertAudioFile;
        
        public final Alert alert;
        
        private final GuiButton playButton;
        private final GuiButton audioFileButton;
        private final GuiButton clearFileButton;
        private final GuiButton resetAudioButton;
        private final ScathaProTextField titleTextField;
        private final ScathaProTextField subtitleTextField;

        private String initialTitle;
        private String initialSubtitle;
        
        private File newSoundFile = null;
        
        
        public AlertEditEntry(Alert alert)
        {
            this.alert = alert;
            alertAudioFile = CustomAlertModeManager.getAlertAudioFile(customModeId, alert);
            
            addLabel(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.UNDERLINE + alert.alertName, 0, 5, getListWidth(), 10).setCentered();
            
            // Titles
            
            AlertTitle defaultTitle = alert.getDefaultTitle();
            boolean canEditSubtitle = !defaultTitle.hasVariableSubtitle;
            initialTitle = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/title");
            if (initialTitle == null) initialTitle = "";
            initialSubtitle = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/subtitle");
            if (initialSubtitle == null) initialSubtitle = "";
            
            addLabel(EnumChatFormatting.GRAY + "Title", 0, 23, getListWidth() / 2 - 5, 10);
            
            titleTextField = new ScathaProTextField(0, mc.fontRendererObj, 0, 33, getListWidth() / 2 - 5, 20);
            titleTextField.setText(initialTitle);
            titleTextField.setPlaceholder(StringUtils.isNullOrEmpty(defaultTitle.title) ? EnumChatFormatting.ITALIC + "(no default title)" : defaultTitle.title);
            addTextField(titleTextField);
            
            addLabel((canEditSubtitle ? EnumChatFormatting.GRAY : EnumChatFormatting.DARK_GRAY) + "Subtitle", getListWidth() / 2 + 5, 23, getListWidth() / 2 - 5, 10);
            
            subtitleTextField = new ScathaProTextField(0, mc.fontRendererObj, getListWidth() / 2 + 5, 33, getListWidth() / 2 - 5, 20);
            if (canEditSubtitle)
            {
                subtitleTextField.setText(initialSubtitle);
                subtitleTextField.setPlaceholder(StringUtils.isNullOrEmpty(defaultTitle.subtitle) ? EnumChatFormatting.ITALIC + "(no default subtitle)" : defaultTitle.subtitle);
            }
            else
            {
                subtitleTextField.setEnabled(false);
                String placeholderText;
                if (defaultTitle.hasVariableSubtitle) placeholderText = EnumChatFormatting.ITALIC + "(dynamic)";
                else placeholderText = EnumChatFormatting.ITALIC + "(not editable)";
                subtitleTextField.setPlaceholder(placeholderText);
            }
            addTextField(subtitleTextField);
            
            // Audio

            boolean audioExists = alertAudioFile.exists();
            boolean canPlayAudio = customAlertModeManager.isSubmodeActive(customModeId) || !audioExists;
            
            playButton = new GuiButton(0, 0, 60, 100, 20, "");
            playButton.enabled = canPlayAudio;
            addButton(playButton);
            
            String audioText;
            if (audioExists) audioText = "Custom audio set";
            else audioText = EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + "Default audio";
            if (canPlayAudio) addLabel(audioText, 105, 66, getListWidth() - 110, 10);
            else
            {
                addLabel(audioText, 105, 61, getListWidth() - 110, 10);
                addLabel(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + "(Mode needs to be selected to play)", 105, 71, getListWidth() - 110, 10);
            }
            
            addButton(audioFileButton = new GuiButton(1, 0, 85, getListWidth() - 110, 20, ""));
            addButton(clearFileButton = new GuiButton(2, getListWidth() - 105, 85, 50, 20, "Clear"));
            addButton(resetAudioButton = new GuiButton(3, getListWidth() - 50, 85, 50, 20, "Reset"));
            updateAudioFileButtons();
        }
        
        @Override
        protected void onButtonPressed(GuiButton button)
        {
            switch (button.id)
            {
                case 0:
                    if (alert.isSoundPlaying()) alert.stopSound();
                    else
                    {
                        if (lastAlertSoundPlayed != null) lastAlertSoundPlayed.stopSound();
                        
                        if (customAlertModeManager.isSubmodeActive(customModeId)) alert.playSound();
                        else alert.playDefaultSound();
                        
                        lastAlertSoundPlayed = alert;
                    }
                    break;
                
                case 1:
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run()
                        {
                            FileDialog fileDialog = new FileDialog((java.awt.Frame) null);
                            fileDialog.setMultipleMode(false);
                            
                            boolean mp3Supported = false;
                            fileDialog.setFile((mp3Supported ? "*.mp3;" : "") + "*.ogg");
                            
                            fileDialog.setTitle("Select " + alert.alertName + " audio...");
                            try
                            {
                                InputStream iconInputStream = Minecraft.getMinecraft().mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png")); 
                                fileDialog.setIconImage(TextureUtil.readBufferedImage(iconInputStream));
                            }
                            catch (Exception e) {}
                            
                            if (activeFileDialog != null)
                            {
                                activeFileDialog.setVisible(false);
                            }
                            activeFileDialog = fileDialog;
                            
                            fileDialog.setVisible(true);
                            
                            File[] selectedFiles = fileDialog.getFiles();
                            if (selectedFiles.length > 0)
                            {
                                onAudioFileChosen(selectedFiles[0]);
                            }
                            if (activeFileDialog == fileDialog)
                            {
                                activeFileDialog = null;
                            }
                        }
                    });
                    thread.start();
                    break;
                    
                case 2:
                    newSoundFile = null;
                    updateAudioFileButtons();
                    break;
                
                case 3:
                    newSoundFile = audioResetFlag;
                    updateAudioFileButtons();
                    break;
            }
        }
        
        private void onAudioFileChosen(File file)
        {
            newSoundFile = file;
            updateAudioFileButtons();
        }
        
        private void updateAudioFileButtons()
        {
            if (newSoundFile == null)
            {
                audioFileButton.displayString = "Select new audio...";
                clearFileButton.enabled = false;
            }
            else
            {
                String audioName;
                
                if (newSoundFile == audioResetFlag) audioName = EnumChatFormatting.ITALIC + "Default";
                else audioName = newSoundFile.getName(); 
                
                audioFileButton.displayString = EnumChatFormatting.RESET + audioName + EnumChatFormatting.RESET + EnumChatFormatting.ITALIC + " (unsaved)";
                

                clearFileButton.enabled = true;
            }
            
            resetAudioButton.enabled = alertAudioFile.exists() && newSoundFile != audioResetFlag;
        }
        
        @Override
        public boolean saveChanges()
        {
            boolean reloadRequired = false;
            
            // Title
            
            boolean titleChanged = false;
            
            String newTitle = titleTextField.getText().trim();
            if (!newTitle.equals(initialTitle))
            {
                JsonUtil.set(modeProperties, "titles/" + alert.alertId + "/title", new JsonPrimitive(newTitle));
                titleChanged = true;
            }
            
            String newSubtitle = subtitleTextField.getText().trim();
            if (!newSubtitle.equals(initialSubtitle))
            {
                JsonUtil.set(modeProperties, "titles/" + alert.alertId + "/subtitle", new JsonPrimitive(newSubtitle));
                titleChanged = true;
            }
            
            if (titleChanged)
            {
                customAlertModeManager.saveSubmodeProperties(customModeId, modeProperties);
                if (customAlertModeManager.isSubmodeActive(customModeId)) customAlertModeManager.loadCurrentSubmodeProperties(); // TODO: prevent doing this multiple times per save
            }
            
            // Sound
            
            if (newSoundFile != null)
            {
                if (newSoundFile == audioResetFlag)
                {
                    if (alertAudioFile.exists())
                    {
                        if (customAlertModeManager.isSubmodeActive(customModeId))
                        {
                            Minecraft.getMinecraft().getSoundHandler().unloadSounds();
                            reloadRequired = true;
                        }
                        alertAudioFile.delete();
                    }
                }
                else
                {
                    // TODO: add .mp3 to .ogg converter?
                    
                    if (alertAudioFile.exists())
                    {
                        if (customAlertModeManager.isSubmodeActive(customModeId))
                        {
                            Minecraft.getMinecraft().getSoundHandler().unloadSounds();
                            reloadRequired = true;
                        }
                    }
                    else
                    {
                        alertAudioFile.getParentFile().mkdirs();
                    }
                    
                    try
                    {
                        Files.copy(newSoundFile, alertAudioFile);
                        
                        if (customAlertModeManager.isSubmodeActive(customModeId))
                        {
                            reloadRequired = true;
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            
            return reloadRequired;
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            if (playButton.enabled || playButton.displayString.isEmpty()) playButton.displayString = alert.isSoundPlaying() ? "Stop" : "Play current";
            
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
        }
    }

}
