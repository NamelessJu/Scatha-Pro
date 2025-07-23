package namelessju.scathapro.gui.lists;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JFrame;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.alerts.AlertTitle;
import namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import namelessju.scathapro.alerts.alertmodes.customalertmode.ICustomAlertModeSaveable;
import namelessju.scathapro.gui.elements.ScathaProButton;
import namelessju.scathapro.gui.elements.ScathaProLabel;
import namelessju.scathapro.gui.elements.ScathaProSlider;
import namelessju.scathapro.gui.elements.ScathaProTextField;
import namelessju.scathapro.gui.menus.CustomAlertModeEditGui;
import namelessju.scathapro.gui.menus.ScathaProGui;
import namelessju.scathapro.managers.FFmpegWrapper;
import namelessju.scathapro.miscellaneous.FileChooser;
import namelessju.scathapro.util.JsonUtil;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.client.Minecraft;
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
    
    public CustomAlertModeEditGuiList(ScathaProGui gui, String customModeId, JsonObject modeProperties)
    {
        super(gui, 75, 0, 125);
        resize();
        
        customAlertModeManager = ScathaPro.getInstance().getCustomAlertModeManager();
        
        this.customModeId = customModeId;
        this.modeProperties = modeProperties;
        
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
        private final ScathaProSlider audioVolumeSlider;
        private final ScathaProButton clearFileButton;
        private final ScathaProButton resetAudioButton;
        private final ScathaProLabel titleLabel;
        private final ScathaProTextField titleTextField;
        private final ScathaProLabel subtitleLabel;
        private final ScathaProTextField subtitleTextField;
        
        private final String initialTitle;
        private final String initialSubtitle;
        private final int initialAudioVolume;
        
        private File newSoundFile = null;
        
        
        public AlertEditEntry(Alert alert)
        {
            this.alert = alert;
            alertAudioFile = CustomAlertModeManager.getAlertAudioFile(customModeId, alert);
            
            addElement(new ScathaProLabel(0, 0, 5, getListWidth(), 10, alert.alertName, Util.Color.YELLOW).setCentered());
            
            // Titles
            
            final String titleTextFieldTooltip = EnumChatFormatting.GRAY + "Supports formatting codes using '&'";
            
            AlertTitle defaultTitle = alert.getDefaultTitle();
            boolean canEditSubtitle = !defaultTitle.hasVariableSubtitle;
            String title = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/title");
            initialTitle = title != null ? title : "";
            String subtitle = JsonUtil.getString(modeProperties, "titles/" + alert.alertId + "/subtitle");
            initialSubtitle = subtitle != null ? subtitle : "";
            
            addElement(titleLabel = new ScathaProLabel(1, 0, 23, getListWidth() / 2 - 5, 10, "Title", Util.Color.GRAY));
            
            titleTextField = new ScathaProTextField(0, 0, 33, getListWidth() / 2 - 5, 20);
            titleTextField.setDefaultFormatting(alert.getDefaultTitle().titleFormatting);
            titleTextField.setSupportsFormatting(true);
            titleTextField.setText(initialTitle);
            titleTextField.setPlaceholder(StringUtils.isNullOrEmpty(defaultTitle.title) ? EnumChatFormatting.ITALIC + "(no default title)" : defaultTitle.title);
            titleTextField.getTooltip().setText(titleTextFieldTooltip);
            addElement(titleTextField);
            
            addElement(subtitleLabel = new ScathaProLabel(2, getListWidth() / 2 + 5, 23, getListWidth() / 2 - 5, 10, "Subtitle", (canEditSubtitle ? Util.Color.GRAY : Util.Color.DARK_GRAY)));
            
            subtitleTextField = new ScathaProTextField(1, getListWidth() / 2 + 5, 33, getListWidth() / 2 - 5, 20);
            subtitleTextField.setDefaultFormatting(alert.getDefaultTitle().subtitleFormatting);
            if (canEditSubtitle)
            {
                subtitleTextField.setSupportsFormatting(true);
                subtitleTextField.setText(initialSubtitle);
                subtitleTextField.setPlaceholder(StringUtils.isNullOrEmpty(defaultTitle.subtitle) ? EnumChatFormatting.ITALIC + "(no default subtitle)" : defaultTitle.subtitle);
                subtitleTextField.getTooltip().setText(titleTextFieldTooltip);
            }
            else
            {
                subtitleTextField.setEnabled(false);
                String placeholderText;
                if (defaultTitle.hasVariableSubtitle) placeholderText = EnumChatFormatting.ITALIC + "(dynamic)";
                else placeholderText = EnumChatFormatting.ITALIC + "(not editable)";
                subtitleTextField.setPlaceholder(placeholderText);
            }
            addElement(subtitleTextField);
            
            // Audio

            boolean audioExists = alertAudioFile.exists();
            boolean canPlayAudio = customAlertModeManager.isSubmodeActive(customModeId) || !audioExists;
            
            String audioText;
            if (audioExists) audioText = "Custom audio";
            else audioText = EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + "Default";
            int audioTitleWidth = TextUtil.getStringWidth("Audio: ");
            addElement(new ScathaProLabel(3, 0, 62, audioTitleWidth, "Audio: ", Util.Color.GRAY));
            addElement(new ScathaProLabel(4, audioTitleWidth, 62, getListWidth() - audioTitleWidth, audioText));
            
            playButton = new ScathaProButton(0, 0, 75, 150, 20, "");
            if (!canPlayAudio)
            {
                playButton.enabled = false;
                playButton.getTooltip().setText(EnumChatFormatting.YELLOW + "Mode needs to be selected to play custom audio");
            }
            addElement(playButton);
            
            Double audioVolume = JsonUtil.getDouble(modeProperties, "soundVolumes/" + alert.alertId);
            initialAudioVolume = audioVolume != null ? (int) Math.round(audioVolume * 100) : 100;
            addElement(audioVolumeSlider = new ScathaProSlider(5, 160, 75, 150, 20, "Volume: ", "%", 0, 100, initialAudioVolume, false, true));
            
            addElement(audioFileButton = new ScathaProButton(1, 0, 100, getListWidth() - 110, 20, ""));
            if (!FFmpegWrapper.isFFmpegInstalled())
            {
                audioFileButtonDefaultTooltip = EnumChatFormatting.YELLOW + "Note:\n" + EnumChatFormatting.GRAY + "No FFmpeg installation found,\nonly *.ogg files supported";
            }
            addElement(resetAudioButton = new ScathaProButton(3, getListWidth() - 105, 100, 50, 20, "Reset"));
            addElement(clearFileButton = new ScathaProButton(2, getListWidth() - 50, 100, 50, 20, "Clear"));
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
                        titleLabel.getTooltip().setText(CustomAlertModeEditGui.unsavedChangesExplanation);
                    }
                    else
                    {
                        titleLabel.setSuffix(null);
                        titleLabel.getTooltip().setText(null);
                    }
                    break;

                case 1:
                    boolean subtitleChanged = !subtitleTextField.getText().trim().equals(initialSubtitle);
                    if (subtitleChanged)
                    {
                        subtitleLabel.setSuffix(CustomAlertModeEditGui.unsavedChangesSuffix);
                        subtitleLabel.getTooltip().setText(CustomAlertModeEditGui.unsavedChangesExplanation);
                    }
                    else
                    {
                        subtitleLabel.setSuffix(null);
                        subtitleLabel.getTooltip().setText(null);
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
        protected void onButtonPressed(ScathaProButton button)
        {
            switch (button.id)
            {
                case 0:
                    if (alert.isSoundPlaying()) alert.stopSound();
                    else
                    {
                        if (lastAlertSoundPlayed != null) lastAlertSoundPlayed.stopSound();
                        
                        float volume = audioVolumeSlider.getValueInt() / 100f;
                        if (customAlertModeManager.isSubmodeActive(customModeId)) alert.playSound(volume);
                        else alert.playDefaultSound(volume);
                        
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
                audioFileButton.getTooltip().setText(audioFileButtonDefaultTooltip);
                clearFileButton.enabled = false;
            }
            else
            {
                String audioName;
                
                if (newSoundFile == audioResetFlag) audioName = EnumChatFormatting.ITALIC + "Resetting to default";
                else audioName = newSoundFile.getName().replace(TextUtil.formattingStartCharacter, "");
                
                audioFileButton.displayString = EnumChatFormatting.RESET + TextUtil.ellipsis(audioName, audioFileButton.getButtonWidth() - TextUtil.getStringWidth(CustomAlertModeEditGui.unsavedChangesSuffix) - 8) + CustomAlertModeEditGui.unsavedChangesSuffix;
                audioFileButton.getTooltip().setText(CustomAlertModeEditGui.unsavedChangesExplanation);
                
                clearFileButton.enabled = true;
            }
            
            resetAudioButton.enabled = alertAudioFile.exists() && newSoundFile != audioResetFlag;
        }
        
        @Override
        public void saveChanges(ICustomAlertModeSaveable.SaveResults results)
        {
            // Title
            
            String newTitle = titleTextField.getText().trim();
            if (!newTitle.equals(initialTitle))
            {
                JsonUtil.set(modeProperties, "titles/" + alert.alertId + "/title", new JsonPrimitive(newTitle));
                results.requestPropertiesSave();
            }
            
            String newSubtitle = subtitleTextField.getText().trim();
            if (!newSubtitle.equals(initialSubtitle))
            {
                JsonUtil.set(modeProperties, "titles/" + alert.alertId + "/subtitle", new JsonPrimitive(newSubtitle));
                results.requestPropertiesSave();
            }
            
            int newVolume = audioVolumeSlider.getValueInt();
            if (newVolume != initialAudioVolume)
            {
                JsonUtil.set(modeProperties, "soundVolumes/" + alert.alertId, new JsonPrimitive(newVolume / 100f));
                results.requestPropertiesSave();
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
