package com.namelessju.scathapro.gui.menus;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.ICustomAlertModeSaveable;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.ScathaProButton;
import com.namelessju.scathapro.gui.elements.ScathaProLabel;
import com.namelessju.scathapro.gui.elements.ScathaProTextField;
import com.namelessju.scathapro.gui.lists.CustomAlertModeEditGuiList;
import com.namelessju.scathapro.miscellaneous.FileChooser;
import com.namelessju.scathapro.util.FileUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.EnumChatFormatting;

public class CustomAlertModeEditGui extends ScathaProGui implements GuiYesNoCallback, ICustomAlertModeSaveable
{
    public static final String unsavedChangesSuffix = EnumChatFormatting.YELLOW + " *";
    public static final String unsavedChangesExplanation = EnumChatFormatting.YELLOW + "Unsaved changes";
    
    
    private final CustomAlertModeManager customAlertModeManager;
    
    private final String customAlertModeId;
    private final File modeFolder;
    private final boolean isNewMode;
    
    private String currentModeName;
    private ScathaProLabel customModeNameLabel;
    private ScathaProTextField nameTextField;
    
    private final FileChooser exportFileChooser;
    
    public CustomAlertModeEditGui(ScathaPro scathaPro, GuiScreen parentGui, String customAlertModeId)
    {
        super(scathaPro, parentGui);
        
        this.customAlertModeManager = scathaPro.getCustomAlertModeManager();
        this.customAlertModeId = customAlertModeId;
        modeFolder = CustomAlertModeManager.getSubModeFile(customAlertModeId);

        currentModeName = customAlertModeManager.getSubmodeName(customAlertModeId);
        if (currentModeName == null) currentModeName = "";
        
        isNewMode = !modeFolder.exists();
        
        if (isNewMode)
        {
            exportFileChooser = null;
        }
        else
        {
            exportFileChooser = new FileChooser("Export custom alert mode...", new String[] {"spmode"}, new Consumer<File>() {
                @Override
                public void accept(File file)
                {
                    if (!file.getName().endsWith(".spmode"))
                    {
                        file = new File(file.getAbsolutePath() + ".spmode");
                    }
                    
                    boolean success = FileUtil.zip(CustomAlertModeManager.getSubModeFile(customAlertModeId), file.getAbsolutePath(), false);
                    if (success) openGui(new InfoMessageGui(CustomAlertModeEditGui.this, EnumChatFormatting.GREEN + "Custom alert mode exported successfully!", "Exported as \"" + file.getName() + "\""));
                    else openGui(new InfoMessageGui(CustomAlertModeEditGui.this, EnumChatFormatting.RED + "Custom alert mode export failed!", "Something went wrong while writing the file"));
                }
            }).makeSaveDialog(currentModeName + ".spmode");
        }
    }
    
    @Override
    public String getTitle()
    {
        return (modeFolder.exists() ? "Edit" : "Create") + " Custom Alert Mode";
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
        // Required as a fix to prevent stopped alert sounds from
        // stacking up and continuing to play after closing the GUI (thanks, MC sound engine...)
        // Although it doesn't matter on Hypixel since the game can't be paused in Multiplayer
        // I wanted to fix this regardless as it is annoying in my Singleplayer test world lol
        return false;
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        customModeNameLabel = new ScathaProLabel(1, this.width / 2 - 155, 35, 225, 10, "Mode Name", Util.Color.GRAY.getValue());
        labelList.add(customModeNameLabel);
        
        String editModeName = nameTextField != null ? nameTextField.getText() : currentModeName;
        nameTextField = new ScathaProTextField(1, mc.fontRendererObj, this.width / 2 - 155, 45, 225, 20);
        nameTextField.setText(editModeName);
        nameTextField.setPlaceholder(EnumChatFormatting.ITALIC + "(unnamed)");
        textFieldList.add(nameTextField);
        
        updateNameTextFieldLabel();
        
        ScathaProButton exportButton;
        buttonList.add(exportButton = new ScathaProButton(1, this.width / 2 + 80, 45, 75, 20, "Export..."));
        if (isNewMode)
        {
            exportButton.enabled = false;
            exportButton.setTooltip(EnumChatFormatting.YELLOW + "Save your new mode before exporting!");
        }
        else
        {
            exportButton.setTooltip(EnumChatFormatting.GRAY + "Unsaved changes will not be exported!");
        }
        
        if (scrollList == null)
        {
            scrollList = new CustomAlertModeEditGuiList(this, customAlertModeId);
        }
        else if (scrollList instanceof CustomAlertModeEditGuiList)
        {
            ((CustomAlertModeEditGuiList) scrollList).resize();
        }
        
        buttonList.add(new ScathaProButton(999, this.width / 2 - 155, this.height - 30, 150, 20, "Save"));
        buttonList.add(new DoneButton(998, this.width / 2 + 5, this.height - 30, 150, 20, "Cancel", this));
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 1:
                exportFileChooser.show();
                break;
                
            case 999:
                ICustomAlertModeSaveable.SaveResults results = new ICustomAlertModeSaveable.SaveResults(customAlertModeId);
                
                saveChanges(results);
                
                if (results.isResourceReloadRequired() || results.hasAudioConversions())
                {
                    mc.displayGuiScreen(new CustomAlertModeResourceLoadingGui(parentGui, results));
                    return;
                }
                
                openParentGui();
                break;
        }
        
        super.actionPerformed(button);
    }
    
    private String getEnteredModeName()
    {
        String newName = nameTextField.getText();
        if (newName == null) newName = "";
        else if (newName.replace(" ", "").isEmpty()) newName = "";
        else newName = newName.trim();
        return newName;
    }
    
    @Override
    public void confirmClicked(boolean result, int id)
    {
        if (id == 1)
        {
            if (result) openParentGui();
            else openGui(this);
        }
    }
    
    @Override
    protected void textFieldTyped(ScathaProTextField textField)
    {
        if (textField == nameTextField)
        {
            updateNameTextFieldLabel();
        }
    }
    
    private void updateNameTextFieldLabel()
    {
        String newName = getEnteredModeName();
        if (!newName.equals(currentModeName))
        {
            customModeNameLabel.setSuffix(unsavedChangesSuffix);
            customModeNameLabel.getTooltip().setTooltip(unsavedChangesExplanation);
        }
        else
        {
            customModeNameLabel.setSuffix(null);
            customModeNameLabel.getTooltip().setTooltip(null);
        }
    }

    @Override
    public void onGuiClosed()
    {
        if (scrollList instanceof CustomAlertModeEditGuiList) ((CustomAlertModeEditGuiList) scrollList).onGuiClosed();
    }
    
    @Override
    public void saveChanges(SaveResults results)
    {
        modeFolder.mkdirs();
        
        String newName = getEnteredModeName();
        
        boolean metaChanged = false;
        
        if (!newName.equals(currentModeName))
        {
            customAlertModeManager.setSubmodeName(customAlertModeId, newName);
            metaChanged = true;
        }
        
        if (isNewMode)
        {
            customAlertModeManager.updateSubmodeLastUsed(customAlertModeId);
            metaChanged = true;
        }
        
        if (metaChanged) customAlertModeManager.saveMeta(customAlertModeId);
        
        
        if (scrollList instanceof ICustomAlertModeSaveable)
        {
            ((ICustomAlertModeSaveable) scrollList).saveChanges(results);
        }
    }
}
