package com.namelessju.scathapro.gui.menus;

import java.io.File;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
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
    private final JsonObject modeProperties;
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
        modeProperties = customAlertModeManager.loadSubmodeProperties(customAlertModeId);
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
        return false;
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        customModeNameLabel = new ScathaProLabel(1, this.width / 2 - 155, 35, 225, 10, "Mode Name", Util.Color.GRAY.getValue());
        elements.add(customModeNameLabel);
        
        String editModeName = nameTextField != null ? nameTextField.getText() : currentModeName;
        nameTextField = new ScathaProTextField(1, this.width / 2 - 155, 45, 225, 20);
        nameTextField.setText(editModeName);
        nameTextField.setPlaceholder(EnumChatFormatting.ITALIC + "(unnamed)");
        elements.add(nameTextField);
        
        updateNameTextFieldLabel();
        
        ScathaProButton exportButton;
        elements.add(exportButton = new ScathaProButton(1, this.width / 2 + 80, 45, 75, 20, "Export..."));
        if (isNewMode)
        {
            exportButton.enabled = false;
            exportButton.getTooltip().setText(EnumChatFormatting.YELLOW + "Save your new mode before exporting!");
        }
        else
        {
            exportButton.getTooltip().setText(EnumChatFormatting.GRAY + "Unsaved changes will not be exported!");
        }
        
        if (scrollList == null)
        {
            scrollList = new CustomAlertModeEditGuiList(this, customAlertModeId, modeProperties);
        }
        else if (scrollList instanceof CustomAlertModeEditGuiList)
        {
            ((CustomAlertModeEditGuiList) scrollList).resize();
        }
        
        elements.add(new ScathaProButton(999, this.width / 2 - 155, this.height - 30, 150, 20, "Save"));
        elements.add(new DoneButton(998, this.width / 2 + 5, this.height - 30, 150, 20, "Cancel", this));
    }
    
    @Override
    protected void actionPerformed(GuiButton button)
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
                
                if (results.isPropertiesSavingRequired())
                {
                    customAlertModeManager.saveSubmodeProperties(customAlertModeId, modeProperties);
                    if (customAlertModeManager.isSubmodeActive(customAlertModeId)) customAlertModeManager.loadCurrentSubmodeProperties();
                    scathaPro.logDebug("Custom alert mode properties saving was requested, save performed");
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
    protected void onTextFieldTyped(ScathaProTextField textField)
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
            customModeNameLabel.getTooltip().setText(unsavedChangesExplanation);
        }
        else
        {
            customModeNameLabel.setSuffix(null);
            customModeNameLabel.getTooltip().setText(null);
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
