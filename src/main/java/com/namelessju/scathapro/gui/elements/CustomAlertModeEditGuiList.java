package com.namelessju.scathapro.gui.elements;

import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.Level;

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

public class CustomAlertModeEditGuiList extends ScathaProGuiList
{
    private final CustomAlertModeManager customAlertModeManager;

    private final String customModeId;
    private final JsonObject modeProperties;
    
    private FileDialog activeFileDialog = null;
    
    public CustomAlertModeEditGuiList(GuiScreen gui, String customModeId)
    {
        super(gui, 75, gui.height - 40, 110);
        
        customAlertModeManager = ScathaPro.getInstance().customAlertModeManager;
        
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

    public interface ISaveableEntry
    {
        /**
         * @return <code>boolean</code> Whether reloading resources is required
         */
        public boolean saveChanges();
    }
    
    private class AlertEditEntry extends ListEntry implements ISaveableEntry
    {
        private final File alertAudioFile;
        private final File audioResetFile = new File("");
        
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

            boolean audioExists = alertAudioFile.exists();
            boolean canPlayAudio = customAlertModeManager.isSubmodeActive(customModeId) || !audioExists;
            
            addLabel(EnumChatFormatting.YELLOW + alert.alertName, 0, 5, 0, 10);
            
            playButton = new GuiButton(0, 0, 20, 100, 20, "");
            playButton.enabled = canPlayAudio;
            addButton(playButton);
            
            String audioText;
            if (audioExists) audioText = "Custom audio set";
            else audioText = EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + "Default audio";
            if (canPlayAudio) addLabel(audioText, 105, 26, getListWidth() - 110, 10);
            else
            {
                addLabel(audioText, 105, 21, getListWidth() - 110, 10);
                addLabel(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + "(mode needs to be selected to play)", 105, 31, getListWidth() - 110, 10);
            }
            
            addButton(audioFileButton = new GuiButton(1, 0, 45, getListWidth() - 110, 20, ""));
            addButton(clearFileButton = new GuiButton(2, getListWidth() - 105, 45, 50, 20, "Clear"));
            addButton(resetAudioButton = new GuiButton(3, getListWidth() - 50, 45, 50, 20, "Reset"));
            updateAudioFileButtons();
            
            
            AlertTitle defaultTitle = alert.getDefaultTitle();
            boolean canEditTitle = defaultTitle.title != null;
            boolean canEditSubtitle = defaultTitle.subtitleType == AlertTitle.SubtitleType.NORMAL && defaultTitle.subtitle != null;
            initialTitle = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/title");
            if (initialTitle == null) initialTitle = "";
            initialSubtitle = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/subtitle");
            if (initialSubtitle == null) initialSubtitle = "";
            
            addLabel((canEditTitle ? EnumChatFormatting.GRAY : EnumChatFormatting.DARK_GRAY) + "Title", 0, 70, getListWidth() / 2 - 5, 10);
            
            titleTextField = new ScathaProTextField(0, mc.fontRendererObj, 0, 80, getListWidth() / 2 - 5, 20);
            if (canEditTitle) titleTextField.setText(initialTitle);
            else titleTextField.setEnabled(false);
            titleTextField.setPlaceholder(canEditTitle ? defaultTitle.title : EnumChatFormatting.ITALIC + "(alert has no title)");
            addTextField(titleTextField);
            
            addLabel((canEditSubtitle ? EnumChatFormatting.GRAY : EnumChatFormatting.DARK_GRAY) + "Subtitle", getListWidth() / 2 + 5, 70, getListWidth() / 2 - 5, 10);
            
            subtitleTextField = new ScathaProTextField(0, mc.fontRendererObj, getListWidth() / 2 + 5, 80, getListWidth() / 2 - 5, 20);
            if (canEditSubtitle) subtitleTextField.setText(initialSubtitle);
            else subtitleTextField.setEnabled(false);
            if (canEditSubtitle) subtitleTextField.setPlaceholder(defaultTitle.subtitle);
            else
            {
                String placeholderText;
                
                switch (defaultTitle.subtitleType)
                {
                    case NORMAL:
                        placeholderText = EnumChatFormatting.ITALIC + "(alert has no subtitle)";
                        break;
                    case VARIABLE:
                        placeholderText = EnumChatFormatting.ITALIC + "(automatic)";
                        break;
                    default:
                        placeholderText = EnumChatFormatting.ITALIC + "(cannot set)";
                }
                
                subtitleTextField.setPlaceholder(placeholderText);
            }
            addTextField(subtitleTextField);
        }
        
        @Override
        protected void onButtonPressed(GuiButton button)
        {
            switch (button.id)
            {
                case 0:
                    if (alert.isSoundPlaying()) alert.stopSound();
                    else if (customAlertModeManager.isSubmodeActive(customModeId)) alert.playSound();
                    else alert.playDefaultSound();
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
                    newSoundFile = audioResetFile;
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
                
                if (newSoundFile == audioResetFile) audioName = EnumChatFormatting.ITALIC + "Default";
                else audioName = newSoundFile.getName(); 
                
                audioFileButton.displayString = EnumChatFormatting.RESET + audioName + EnumChatFormatting.RESET + EnumChatFormatting.ITALIC + " (unsaved)";
                

                clearFileButton.enabled = true;
            }
            
            resetAudioButton.enabled = alertAudioFile.exists() && newSoundFile != audioResetFile;
        }
        
        @Override
        public boolean saveChanges()
        {
            boolean reloadRequired = false;
            
            // Title
            
            boolean titleChanged = false;
            
            String newTitle = titleTextField.getText().trim();
            if (alert.hasTitle() && !newTitle.equals(initialTitle))
            {
                JsonUtil.set(modeProperties, "titles/" + alert.alertId + "/title", new JsonPrimitive(newTitle));
                titleChanged = true;
            }
            
            String newSubtitle = subtitleTextField.getText().trim();
            if (alert.hasSubtitle() && !newSubtitle.equals(initialSubtitle))
            {
                JsonUtil.set(modeProperties, "titles/" + alert.alertId + "/subtitle", new JsonPrimitive(newSubtitle));
                titleChanged = true;
            }
            
            if (titleChanged)
            {
                customAlertModeManager.saveSubmodeProperties(customModeId, modeProperties);
                if (customAlertModeManager.isSubmodeActive(customModeId)) customAlertModeManager.loadCurrentSubmodeProperties();
            }
            
            // Sound
            
            if (newSoundFile != null)
            {
                if (newSoundFile == audioResetFile)
                {
                    if (alertAudioFile.exists())
                    {
                        if (customAlertModeManager.isSubmodeActive(customModeId)) Minecraft.getMinecraft().getSoundHandler().unloadSounds();
                        alertAudioFile.delete();
                        reloadRequired = true;
                    }
                }
                else
                {
                    if (!newSoundFile.getName().endsWith(".ogg"))
                    {
                        // TODO: add .mp3 to .ogg converter
                        ScathaPro.getInstance().logger.log(Level.WARN, alert.alertName + " audio file is not an .ogg file!");
                        return false;
                    }
                    
                    if (alertAudioFile.exists())
                    {
                        if (customAlertModeManager.isSubmodeActive(customModeId)) Minecraft.getMinecraft().getSoundHandler().unloadSounds();
                    }
                    else
                    {
                        alertAudioFile.getParentFile().mkdirs();
                    }
                    
                    try
                    {
                        Files.copy(newSoundFile, alertAudioFile);
                        reloadRequired = true;
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
