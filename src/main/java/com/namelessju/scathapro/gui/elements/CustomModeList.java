package com.namelessju.scathapro.gui.elements;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.DateFormat;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.gui.menus.CustomAlertModeEditGui;
import com.namelessju.scathapro.util.Util;

@SideOnly(Side.CLIENT)
public class CustomModeList extends ScathaProGuiList
{
	private final CustomAlertModeManager customAlertModeManager;
	
    public CustomModeList(GuiScreen gui)
    {
        super(gui, 30);
        
        customAlertModeManager = ScathaPro.getInstance().customAlertModeManager;
        
        customAlertModeManager.loadAllMeta();
        
        String[] customModeIds = customAlertModeManager.getAllSubmodeIds();
        for (String customModeId : customModeIds)
        {
            this.listEntries.add(new CustomModeEntry(customModeId));
        }
    }
    
    @SideOnly(Side.CLIENT)
    public class CustomModeEntry extends ListEntry
    {
    	public final String customModeId;

        private CustomModeEntry(String customModeId)
        {
            this.customModeId = customModeId;
            
            String formatting = "";
            if (customAlertModeManager.getCurrentSubmodeId().equals(customModeId)) {
            	formatting = EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD;
            }
            
            String customModeName = formatting + customAlertModeManager.getSubmodeDisplayName(customModeId);
            long customModeLastUsed = customAlertModeManager.getSubmodeLastUsed(customModeId);
            
            addLabel(customModeName, 0, 5, getListWidth(), 10);
            addLabel(EnumChatFormatting.GRAY + "Last used: " + (customModeLastUsed >= 0L ? Util.formatTime(customModeLastUsed, DateFormat.SHORT, DateFormat.SHORT) : "unknown"), 0, 15, getListWidth(), 10);
            
            GuiButton btnSelect = new GuiButton(0, getListWidth() - 105, 5, 50, 20, "Select");
            btnSelect.enabled = !customAlertModeManager.getCurrentSubmodeId().equals(customModeId);
            addButton(btnSelect);
            
            addButton(new GuiButton(1, getListWidth() - 50, 5, 50, 20, "Edit..."));
        }
        
		@Override
		protected void onButtonPressed(GuiButton button) {
			switch (button.id) {
				case 0:
					customAlertModeManager.changeSubmode(customModeId);
	                gui.initGui();
	                break;
				case 1:
	            	mc.displayGuiScreen(new CustomAlertModeEditGui(gui, customModeId));
	                break;
			}
		}
    }
}