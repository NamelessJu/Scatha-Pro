package com.namelessju.scathapro.gui.elements;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.logging.log4j.Level;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.gui.menus.CustomAlertModeEditGui;
import com.namelessju.scathapro.util.Util;

@SideOnly(Side.CLIENT)
public class CustomAlertModeGuiList extends ScathaProGuiList
{
    private final ScathaPro scathaPro;
    
    @Override
    protected boolean areEntriesSelectable()
    {
        return false;
    }
    
    private final CustomAlertModeManager customAlertModeManager;
    
    public CustomAlertModeGuiList(ScathaPro scathaPro, GuiScreen gui)
    {
        super(gui, 30);
        
        this.scathaPro = scathaPro;

        this.listEntries.add(new CreateCustomModeEntry());
        
        customAlertModeManager = scathaPro.customAlertModeManager;
        customAlertModeManager.loadAllMeta();
        
        String[] customModeIds = CustomAlertModeManager.getAllSubmodeIds();
        Arrays.sort(customModeIds, new Comparator<String>() {
            @Override
            public int compare(String customModeId1, String customModeId2)
            {
                long lastUsedTime1 = customAlertModeManager.getSubmodeLastUsed(customModeId1);
                long lastUsedTime2 = customAlertModeManager.getSubmodeLastUsed(customModeId2);
                
                if (lastUsedTime2 > lastUsedTime1) return 1;
                else if (lastUsedTime2 < lastUsedTime1) return -1;
                return 0;
            }
        });
        
        for (String customModeId : customModeIds)
        {
            this.listEntries.add(new CustomModeEntry(customModeId));
        }
    }
    
    
    @SideOnly(Side.CLIENT)
    public class CreateCustomModeEntry extends ListEntry
    {
        private CreateCustomModeEntry()
        {
            addButton(new GuiButton(0, 0, 5, getListWidth(), 20, "Create new..."));
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
                        scathaPro.logger.log(Level.ERROR, "Couldn't create new custom alert mode - generating a new unique ID failed");
                        break;
                    }
                    
                    File modeFolder = CustomAlertModeManager.getSubModeFile(newModeId);
                    modeFolder.mkdirs();
                    
                    mc.displayGuiScreen(new CustomAlertModeEditGui(scathaPro, gui, newModeId));
                    
                    break;
            }
        }
    }
    
    
    @SideOnly(Side.CLIENT)
    public class CustomModeEntry extends ListEntry
    {
        public final String customModeId;
        public final String customModeName;

        private CustomModeEntry(String customModeId)
        {
            this.customModeId = customModeId;
            
            customModeName = customAlertModeManager.getSubmodeDisplayName(customModeId);
            boolean isModeActive = customAlertModeManager.isSubmodeActive(customModeId);
            long lastUsed = customAlertModeManager.getSubmodeLastUsed(customModeId);
            
            addLabel(customModeName, 0, 5, getListWidth(), 10);
            
            String detailsString;
            if (isModeActive) detailsString = EnumChatFormatting.GREEN.toString() + EnumChatFormatting.ITALIC + "Selected";
            else detailsString = EnumChatFormatting.DARK_GRAY + "Last used: " + (lastUsed >= 0L ? Util.formatTime(lastUsed) : EnumChatFormatting.ITALIC + "never");
            addLabel(detailsString, 0, 15, getListWidth(), 10);
            
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