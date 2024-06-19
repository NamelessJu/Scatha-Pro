package com.namelessju.scathapro.gui.elements;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.Comparator;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.gui.menus.InfoMessageGui;
import com.namelessju.scathapro.gui.menus.CustomAlertModeEditGui;

public class CustomAlertModeGuiList extends ScathaProGuiList
{
    private final ScathaPro scathaPro;
    
    private final CustomAlertModeManager customAlertModeManager;
    
    public CustomAlertModeGuiList(ScathaPro scathaPro, GuiScreen gui)
    {
        super(gui, 30);
        
        this.scathaPro = scathaPro;

        this.listEntries.add(new CreateCustomModeEntry());
        
        customAlertModeManager = scathaPro.getCustomAlertModeManager();
        customAlertModeManager.loadAllMeta();
        
        String[] customModeIds = CustomAlertModeManager.getAllSubmodeIds();
        Arrays.sort(customModeIds, new Comparator<String>() {
            @Override
            public int compare(String customModeId1, String customModeId2)
            {
                long lastUsedTime1 = customAlertModeManager.getSubmodeLastUsed(customModeId1);
                long lastUsedTime2 = customAlertModeManager.getSubmodeLastUsed(customModeId2);

                if (customAlertModeManager.isSubmodeActive(customModeId2)) return 1;
                if (customAlertModeManager.isSubmodeActive(customModeId1)) return -1;
                
                if (lastUsedTime2 > lastUsedTime1) return 1;
                if (lastUsedTime2 < lastUsedTime1) return -1;
                return 0;
            }
        });
        
        for (String customModeId : customModeIds)
        {
            this.listEntries.add(new CustomModeEntry(customModeId));
        }
    }
    
    
    private class CreateCustomModeEntry extends ListEntry
    {
        public CreateCustomModeEntry()
        {
            addButton(new GuiButton(0, getListWidth() / 2 - 100, 5, 200, 20, "Create New Custom Alert Mode..."));
        }
        
        @Override
        protected void onButtonPressed(GuiButton button)
        {
            switch (button.id)
            {
                case 0:
                    String newModeId = CustomAlertModeManager.getNewSubmodeId();
                    if (newModeId == null)
                    {
                        mc.displayGuiScreen(new InfoMessageGui(CustomAlertModeGuiList.this.gui, EnumChatFormatting.RED + "Failed to set up new custom alert mode", "Generating a new unique ID failed!\n(Exceeded maximum number of tries)"));
                        break;
                    }
                    
                    mc.displayGuiScreen(new CustomAlertModeEditGui(scathaPro, gui, newModeId));
                    break;
            }
        }
    }
    
    
    private class CustomModeEntry extends ListEntry
    {
        public final String customModeId;
        public final String customModeName;

        public CustomModeEntry(String customModeId)
        {
            this.customModeId = customModeId;
            
            customModeName = customAlertModeManager.getSubmodeDisplayName(customModeId);
            boolean isModeActive = customAlertModeManager.isSubmodeActive(customModeId);

            addLabel(customModeName, 0, isModeActive ? 5 : 10, getListWidth(), 10);
            
            if (isModeActive)
            {
                addLabel(EnumChatFormatting.GREEN + "Selected", 0, 15, getListWidth(), 10);
            }
            
            GuiButton btnSelect = new GuiButton(0, getListWidth() - 160, 5, 50, 20, "Select");
            btnSelect.enabled = !isModeActive;
            addButton(btnSelect);
            
            addButton(new GuiButton(1, getListWidth() - 105, 5, 50, 20, "Edit..."));
            addButton(new DeleteCustomAlertModeButton(2, getListWidth() - 50, 5, 50, 20, "Delete", customModeId, gui));
        }
        
        @Override
        protected void onButtonPressed(GuiButton button)
        {
            switch (button.id)
            {
                case 0:
                    customAlertModeManager.changeSubmode(customModeId);
                    gui.initGui();
                    break;
                
                case 1:
                    mc.displayGuiScreen(new CustomAlertModeEditGui(scathaPro, gui, customModeId));
                    break;
            }
        }
    }
}