package com.namelessju.scathapro.gui.lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumChatFormatting;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.gui.menus.InfoMessageGui;
import com.namelessju.scathapro.gui.menus.ScathaProGui;
import com.namelessju.scathapro.miscellaneous.FileChooser;
import com.namelessju.scathapro.util.FileUtil;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.Util;
import com.namelessju.scathapro.gui.elements.DeleteCustomAlertModeButton;
import com.namelessju.scathapro.gui.elements.ScathaProLabel;
import com.namelessju.scathapro.gui.menus.CustomAlertModeEditGui;

public class CustomAlertModeGuiList extends ScathaProGuiList
{
    private final ScathaPro scathaPro;
    
    private final CustomAlertModeManager customAlertModeManager;
    
    public CustomAlertModeGuiList(ScathaProGui gui)
    {
        super(gui, 30);
        
        this.scathaPro = gui.scathaPro;

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
            addButton(new GuiButton(0, 0, 5, getListWidth() - 83, 20, "Create New Custom Alert Mode..."));
            addButton(new GuiButton(1, getListWidth() - 78, 5, 78, 20, "Import..."));
        }
        
        private FileChooser modeFileChooser = new FileChooser("Select custom alert mode file...", new String[] {"spmode"}, new Consumer<File>() {
            @Override
            public void accept(File file)
            {
                if (!file.exists())
                {
                    mc.displayGuiScreen(new InfoMessageGui(gui, EnumChatFormatting.RED + "Custom alert mode import failed", "Selected file doesn't exist!"));
                    return;
                }
                
                String modeFolderName = file.getName();
                // remove file extension
                int dotIndex = modeFolderName.lastIndexOf('.');
                if (dotIndex >= 0) modeFolderName = modeFolderName.substring(0, dotIndex);
                
                File importDirectory = FileUtil.getNonexistentFile(CustomAlertModeManager.submodesDirectory, modeFolderName);
                
                boolean success = FileUtil.unzip(file, importDirectory.toPath(), null);
                if (success)
                {
                    String submodeId = importDirectory.getName().toString();
                    scathaPro.getCustomAlertModeManager().loadMeta(submodeId);
                    scathaPro.getCustomAlertModeManager().updateSubmodeLastUsed(submodeId);
                    scathaPro.getCustomAlertModeManager().saveMeta(submodeId);
                    gui.initGui();
                }
                else mc.displayGuiScreen(new InfoMessageGui(gui, EnumChatFormatting.RED + "Custom alert mode import failed!", "Couldn't read or process the selected file"));
            }
        });
        
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
                
                case 1:
                    modeFileChooser.show();
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
            
            int modeNameWidth = getListWidth() - 165;
            addLabel(new ScathaProLabel(0, 0, isModeActive ? 5 : 10, modeNameWidth, 10, TextUtil.ellipsis(customModeName, modeNameWidth)));
            if (isModeActive) addLabel(new ScathaProLabel(1, 0, 15, modeNameWidth, 10, "Selected", Util.Color.GREEN.getValue()));
            
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
                    break;
                
                case 1:
                    mc.displayGuiScreen(new CustomAlertModeEditGui(scathaPro, gui, customModeId));
                    break;
            }
        }
    }
}