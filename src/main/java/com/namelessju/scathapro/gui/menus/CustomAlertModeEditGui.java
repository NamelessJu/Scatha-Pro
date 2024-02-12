package com.namelessju.scathapro.gui.menus;

import java.io.File;
import java.io.IOException;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.gui.elements.CustomAlertModeEditGuiList;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.ScathaProTextField;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class CustomAlertModeEditGui extends ScathaProGui
{
    private final CustomAlertModeManager customAlertModeManager;
    
    private final String customAlertModeId;
    private final File modeFolder;
    
    private String currentModeName;
    private ScathaProTextField nameTextField;
    
    private String editModeName = null;
    
    public CustomAlertModeEditGui(ScathaPro scathaPro, GuiScreen parentGui, String customAlertModeId)
    {
        super(scathaPro, parentGui);
        
        this.customAlertModeManager = scathaPro.getCustomAlertModeManager();
        this.customAlertModeId = customAlertModeId;
        modeFolder = CustomAlertModeManager.getSubModeFile(customAlertModeId);

        currentModeName = customAlertModeManager.getSubmodeName(customAlertModeId);
        if (currentModeName == null) currentModeName = "";
    }
    
    @Override
    public String getTitle()
    {
        return (modeFolder.exists() ? "Editing" : "New") +" custom alert mode";
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
        // Required as a bug fix to prevent stopped alert sounds from
        // stacking up and continuing to play after closing the GUI
        // (thanks, MC sound engine...)
        // Doesn't matter on Hypixel anyways, since the game can't be paused there
        // Regardless, I wanted to fix this as it is annoying in my singleplayer test world ;)
        return false;
    }
    
    @Override
    public void initGui()
    {
        super.initGui();

        
        if (editModeName == null) editModeName = currentModeName;
        
        GuiLabel customModeNameLabel = new GuiLabel(fontRendererObj, 0, width / 2 - 155, 35, 310, 10, Util.Color.GRAY.getValue());
        customModeNameLabel.func_175202_a("Mode Name");
        labelList.add(customModeNameLabel);
        nameTextField = new ScathaProTextField(0, mc.fontRendererObj, width / 2 - 155, 45, 310, 20);
        nameTextField.setText(editModeName);
        nameTextField.setPlaceholder("<unnamed>");
        textFieldList.add(nameTextField);
        
        
        if (scrollList == null)
        {
            scrollList = new CustomAlertModeEditGuiList(this, customAlertModeId);
        }
        else if (scrollList instanceof CustomAlertModeEditGuiList)
        {
            ((CustomAlertModeEditGuiList) scrollList).resize();
        }

        buttonList.add(new DoneButton(504704698, this.width / 2 - 155, this.height - 30, 150, 20, "Save", this));
        buttonList.add(new DoneButton(504704699, this.width / 2 + 5, this.height - 30, 150, 20, "Cancel", this));
        
        if (scathaPro.getConfig().getBoolean(Config.Key.devMode))
        {
            buttonList.add(new GuiButton(504704697, this.width / 2 + 170, this.height - 30, 50, 20, EnumChatFormatting.ITALIC + "Folder"));
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 504704698:
                modeFolder.mkdirs();
                
                String newName = nameTextField.getText();
                if (newName == null) newName = "";
                else if (newName.replace(" ", "").isEmpty()) newName = "";
                else newName = newName.trim();
                
                if (!newName.equals(currentModeName))
                {
                    customAlertModeManager.setSubmodeName(customAlertModeId, newName);
                    customAlertModeManager.saveMeta(customAlertModeId);
                }
                
                if (scrollList instanceof CustomAlertModeEditGuiList && ((CustomAlertModeEditGuiList) scrollList).saveChanges())
                {
                    CustomAlertModeManager customAlertModeManager = ScathaPro.getInstance().getCustomAlertModeManager();
                    if (customAlertModeManager.isSubmodeActive(customAlertModeId)) customAlertModeManager.reloadResourcePack();
                }
                
                break;

            case 504704697:
                Util.openFileInExplorer(CustomAlertModeManager.getSubModeFile(customAlertModeId));
                break;
        }

        super.actionPerformed(button);
    }
    
    @Override
    protected void textFieldTyped(ScathaProTextField textField)
    {
        if (textField == nameTextField)
        {
            editModeName = textField.getText();
        }
    }

    @Override
    public void onGuiClosed()
    {
        if (scrollList instanceof CustomAlertModeEditGuiList) ((CustomAlertModeEditGuiList) scrollList).onGuiClosed();
    }
}
