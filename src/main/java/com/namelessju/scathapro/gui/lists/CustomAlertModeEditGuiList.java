package com.namelessju.scathapro.gui.lists;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JFrame;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.alerts.AlertTitle;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.ICustomAlertModeSaveable;
import com.namelessju.scathapro.gui.elements.ScathaProButton;
import com.namelessju.scathapro.gui.elements.ScathaProLabel;
import com.namelessju.scathapro.gui.elements.ScathaProTextField;
import com.namelessju.scathapro.gui.menus.CustomAlertModeEditGui;
import com.namelessju.scathapro.gui.menus.ScathaProGui;
import com.namelessju.scathapro.managers.FFmpegWrapper;
import com.namelessju.scathapro.miscellaneous.FileChooser;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

public class CustomAlertModeEditGuiList extends ScathaProGuiList implements ICustomAlertModeSaveable
{
    private final CustomAlertModeManager customAlertModeManager;

    private final String customModeId;
    private final JsonObject modeProperties;
    
    private JFrame activeFileDialog = null;
    private Alert lastAlertSoundPlayed = null;

    private List<ICustomAlertModeSaveable.SaveResults.AudioConversion> audioConversions = Lists.newArrayList();
    
    public CustomAlertModeEditGuiList(ScathaProGui gui, String customModeId)
    {
        super(gui, 75, 0, 115);
        resize();
        
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
        this.bottom = gui.height - 40;
        this.right = this.width;
    }
    
    public void onGuiClosed()
    {
        if (activeFileDialog != null) activeFileDialog.dispose();

        Alert[] alerts = Alert.values();
        for (Alert alert : alerts)
        {
            alert.stopSound();
        }
    }

    @Override
    public void saveChanges(ICustomAlertModeSaveable.SaveResults saveResults)
    {
        for (ListEntry entry : listEntries)
        {
            if (!(entry instanceof ICustomAlertModeSaveable)) continue;
            ((ICustomAlertModeSaveable) entry).saveChanges(saveResults);
        }
        
        if (audioConversions.size() > 0)
        {
            for (ICustomAlertModeSaveable.SaveResults.AudioConversion conversion : audioConversions)
            {
                saveResults.addAudioConversion(conversion);
            }
            audioConversions.clear();
        }
    }
    
    
    /*
     * #####################
     * #                   #
     * #   E N T R I E S   #
     * #                   #
     * #####################
     */
    
    private class AlertEditEntry extends ListEntry implements ICustomAlertModeSaveable
    {
        private final FileChooser audioFileChooser;
        
        private final File audioResetFlag = new File("");
        private final File alertAudioFile;
        
        public final Alert alert;
        
        private final ScathaProButton playButton;
        private final ScathaProButton audioFileButton;
        private String audioFileButtonDefaultTooltip = null;
        private final GuiButton clearFileButton;
        private final GuiButton resetAudioButton;
        private final ScathaProLabel titleLabel;
        private final ScathaProTextField titleTextField;
        private final ScathaProLabel subtitleLabel;
        private final ScathaProTextField subtitleTextField;
        
        private String initialTitle;
        private String initialSubtitle;
        
        private File newSoundFile = null;
        
        
        public AlertEditEntry(Alert alert)
        {
            this.alert = alert;
            alertAudioFile = CustomAlertModeManager.getAlertAudioFile(customModeId, alert);
            
            addLabel(new ScathaProLabel(0, 0, 5, getListWidth(), 10, alert.alertName, Util.Color.YELLOW.getValue()).setCentered());
            
            // Titles
            
            final String tooltip = EnumChatFormatting.GRAY + "Supports formatting codes using '&'";
            
            AlertTitle defaultTitle = alert.getDefaultTitle();
            boolean canEditSubtitle = !defaultTitle.hasVariableSubtitle;
            initialTitle = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/title");
            if (initialTitle == null) initialTitle = "";
            initialSubtitle = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/subtitle");
            if (initialSubtitle == null) initialSubtitle = "";
            
            addLabel(titleLabel = new ScathaProLabel(1, 0, 23, getListWidth() / 2 - 5, 10, "Title", Util.Color.GRAY.getValue()));
            
            titleTextField = new ScathaProTextField(0, mc.fontRendererObj, 0, 33, getListWidth() / 2 - 5, 20);
            titleTextField.setSupportsFormatting(true);
            titleTextField.setText(initialTitle);
            titleTextField.setPlaceholder(StringUtils.isNullOrEmpty(defaultTitle.title) ? EnumChatFormatting.ITALIC + "(no default title)" : defaultTitle.title);
            titleTextField.setTooltip(tooltip);
            addTextField(titleTextField);
            
            addLabel(subtitleLabel = new ScathaProLabel(2, getListWidth() / 2 + 5, 23, getListWidth() / 2 - 5, 10, "Subtitle", (canEditSubtitle ? Util.Color.GRAY : Util.Color.DARK_GRAY).getValue()));
            
            subtitleTextField = new ScathaProTextField(1, mc.fontRendererObj, getListWidth() / 2 + 5, 33, getListWidth() / 2 - 5, 20);
            if (canEditSubtitle)
            {
                subtitleTextField.setSupportsFormatting(true);
                subtitleTextField.setText(initialSubtitle);
                subtitleTextField.setPlaceholder(StringUtils.isNullOrEmpty(defaultTitle.subtitle) ? EnumChatFormatting.ITALIC + "(no default subtitle)" : defaultTitle.subtitle);
                subtitleTextField.setTooltip(tooltip);
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
            
            playButton = new ScathaProButton(0, getListWidth() - 105, 60, 105, 20, "");
            if (!canPlayAudio)
            {
                playButton.enabled = false;
                playButton.setTooltip(EnumChatFormatting.YELLOW + "Mode needs to be selected to play custom audio");
            }
            addButton(playButton);
            
            String audioText;
            if (audioExists) audioText = "Custom audio set";
            else audioText = EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC + "Default audio";
            addLabel(new ScathaProLabel(3, 0, 60, getListWidth() - 110, 10, "Audio", Util.Color.GRAY.getValue()));
            addLabel(new ScathaProLabel(4, 0, 72, getListWidth() - 110, 10, audioText));
            
            addButton(audioFileButton = new ScathaProButton(1, 0, 85, getListWidth() - 110, 20, ""));
            if (!FFmpegWrapper.isFFmpegInstalled())
            {
                audioFileButtonDefaultTooltip = EnumChatFormatting.YELLOW + "No FFmpeg installation found, only *.ogg files supported";
            }
            addButton(resetAudioButton = new GuiButton(3, getListWidth() - 105, 85, 50, 20, "Reset"));
            addButton(clearFileButton = new GuiButton(2, getListWidth() - 50, 85, 50, 20, "Clear"));
            updateAudioFileButtons();
            
            String[] supportedFileFormats = FFmpegWrapper.isFFmpegInstalled() ? new String[] {"ogg", "mp3", "wav"} : new String[] {"ogg"};
            audioFileChooser = new FileChooser("Select \"" + alert.alertName + "\" audio...", supportedFileFormats, new Consumer<File>() {
                @Override
                public void accept(File t)
                {
                    onAudioFileChosen(t);
                }
            });
        }
        
        @Override
        protected void onTextFieldTyped(ScathaProTextField textField)
        {
            switch (textField.getId())
            {
                case 0:
                    boolean titleChanged = !titleTextField.getText().trim().equals(initialTitle);
                    if (titleChanged)
                    {
                        titleLabel.setSuffix(CustomAlertModeEditGui.unsavedChangesSuffix);
                        titleLabel.getTooltip().setTooltip(CustomAlertModeEditGui.unsavedChangesExplanation);
                    }
                    else
                    {
                        titleLabel.setSuffix(null);
                        titleLabel.getTooltip().setTooltip(null);
                    }
                    break;

                case 1:
                    boolean subtitleChanged = !subtitleTextField.getText().trim().equals(initialSubtitle);
                    if (subtitleChanged)
                    {
                        subtitleLabel.setSuffix(CustomAlertModeEditGui.unsavedChangesSuffix);
                        subtitleLabel.getTooltip().setTooltip(CustomAlertModeEditGui.unsavedChangesExplanation);
                    }
                    else
                    {
                        subtitleLabel.setSuffix(null);
                        subtitleLabel.getTooltip().setTooltip(null);
                    }
                    break;
            }
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            if (!isEntryWithinSlotBounds(y, slotHeight)) return;
            
            if (playButton.enabled || playButton.displayString.isEmpty()) playButton.displayString = alert.isSoundPlaying() ? "Stop" : "Play current";
            
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
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
                    audioFileChooser.show();
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
                audioFileButton.getTooltip().setTooltip(audioFileButtonDefaultTooltip);
                clearFileButton.enabled = false;
            }
            else
            {
                String audioName;
                
                if (newSoundFile == audioResetFlag) audioName = EnumChatFormatting.ITALIC + "Resetting to default";
                else audioName = newSoundFile.getName().replace(TextUtil.formattingStartCharacter, "");
                
                audioFileButton.displayString = EnumChatFormatting.RESET + TextUtil.ellipsis(audioName, audioFileButton.getButtonWidth() - TextUtil.getStringWidth(CustomAlertModeEditGui.unsavedChangesSuffix) - 8) + CustomAlertModeEditGui.unsavedChangesSuffix;
                audioFileButton.getTooltip().setTooltip(CustomAlertModeEditGui.unsavedChangesExplanation);
                
                clearFileButton.enabled = true;
            }
            
            resetAudioButton.enabled = alertAudioFile.exists() && newSoundFile != audioResetFlag;
        }
        
        @Override
        public void saveChanges(ICustomAlertModeSaveable.SaveResults results)
        {
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
                        }
                        alertAudioFile.delete();
                        results.requestResourceReload();
                    }
                }
                else
                {
                    if (alertAudioFile.exists())
                    {
                        if (customAlertModeManager.isSubmodeActive(customModeId))
                        {
                            Minecraft.getMinecraft().getSoundHandler().unloadSounds();
                            // results.requestResourceReload();
                        }
                    }
                    else
                    {
                        alertAudioFile.getParentFile().mkdirs();
                    }
                    
                    if (newSoundFile.getName().toLowerCase().endsWith(".ogg"))
                    {
                        try
                        {
                            Files.copy(newSoundFile, alertAudioFile);
                            results.requestResourceReload();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        ScathaPro.getInstance().logDebug("New custom audio for " + alert.alertName + " is not an ogg-file, added to conversions");
                        audioConversions.add(new ICustomAlertModeSaveable.SaveResults.AudioConversion(newSoundFile, alertAudioFile));
                    }
                }
            }
        }
    }

}
