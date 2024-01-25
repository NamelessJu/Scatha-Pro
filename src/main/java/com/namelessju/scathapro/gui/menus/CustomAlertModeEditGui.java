package com.namelessju.scathapro.gui.menus;

import java.io.File;
import java.io.IOException;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.gui.elements.CustomAlertModeEditGuiList;
import com.namelessju.scathapro.gui.elements.DoneButton;
import com.namelessju.scathapro.gui.elements.ScathaProTextField;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;

public class CustomAlertModeEditGui extends ScathaProGui {

	private final CustomAlertModeManager customAlertModeManager;
	
	private final String customAlertModeId;
	
	private String currentModeName;
	private ScathaProTextField nameTextField;

    @Override
    public String getTitle() {
        return "Edit Custom Alert Mode";
    }
    
    @Override
    public boolean doesGuiPauseGame()
    {
    	// required as a bug fix to prevent stopped alert sounds from
    	// stacking up and continuing to play after closing the GUI
    	// (thanks, MC sound engine...)
    	// doesn't matter on Hypixel anyways, since the game can't be paused there
        return false;
    }
	
	public CustomAlertModeEditGui(GuiScreen parentGui, String customAlertModeId) {
		super(parentGui);

		this.customAlertModeManager = ScathaPro.getInstance().customAlertModeManager;
		
		this.customAlertModeId = customAlertModeId;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		

		currentModeName = customAlertModeManager.getSubmodeName(customAlertModeId);
		if (currentModeName == null) currentModeName = "";
    	
		GuiLabel customModeNameLabel = new GuiLabel(fontRendererObj, 0, width / 2 - 155, 30, 310, 10, Util.Color.WHITE.getValue());
		customModeNameLabel.func_175202_a("Mode Name");
        labelList.add(customModeNameLabel);
        nameTextField = new ScathaProTextField(0, mc.fontRendererObj, width / 2 - 155, 45, 310, 20);
        nameTextField.setText(currentModeName);
        nameTextField.setPlaceholder("<unnamed>");
    	textFieldList.add(nameTextField);
    	
        
		if (scrollList == null) {
			scrollList = new CustomAlertModeEditGuiList(this, customAlertModeId);
		}
		else if (scrollList instanceof CustomAlertModeEditGuiList) {
			((CustomAlertModeEditGuiList) scrollList).resize();
		}

		buttonList.add(new DoneButton(504704698, this.width / 2 - 155, this.height - 30, 150, 20, "Save", this));
		buttonList.add(new DoneButton(504704699, this.width / 2 + 5, this.height - 30, 150, 20, "Cancel", this));
		if (ScathaPro.getInstance().config.getBoolean(Config.Key.devMode)) {
			buttonList.add(new GuiButton(504704697, this.width / 2 + 165, this.height - 30, 50, 20, "Open dir"));
		}
	}
	
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
    	switch (button.id)
    	{
	    	case 504704698:
				String newName = nameTextField.getText();
				if (newName == null) newName = "";
				else if (newName.replace(" ", "").isEmpty()) newName = "";
				else newName = newName.trim();
				
				if (!newName.equals(currentModeName)) {
					customAlertModeManager.setSubmodeName(customAlertModeId, newName);
					customAlertModeManager.saveMeta(customAlertModeId);
				}
				
	    		if (scrollList instanceof CustomAlertModeEditGuiList && ((CustomAlertModeEditGuiList) scrollList).saveChanges()) {
	    			CustomAlertModeManager customAlertModeManager = ScathaPro.getInstance().customAlertModeManager;
	    			if (customAlertModeManager.isSubmodeActive(customAlertModeId)) customAlertModeManager.reloadResourcePack();
	    		}
	    		
	    		break;

	    	case 504704697:
	    		File directory = CustomAlertModeManager.getSubModeFile(customAlertModeId);
	    		Runtime.getRuntime().exec("explorer.exe " + directory.getAbsolutePath());
	    		break;
    	}

    	super.actionPerformed(button);
    }

	@Override
    public void onGuiClosed()
    {
		if (scrollList instanceof CustomAlertModeEditGuiList) ((CustomAlertModeEditGuiList) scrollList).onGuiClosed();
    }

}
