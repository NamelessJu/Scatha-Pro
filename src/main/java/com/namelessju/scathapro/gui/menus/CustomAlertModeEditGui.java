package com.namelessju.scathapro.gui.menus;

import java.io.File;
import java.io.IOException;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.gui.elements.CustomModeEditList;
import com.namelessju.scathapro.gui.elements.DoneButton;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class CustomAlertModeEditGui extends ScathaProGui {

	private final String customAlertModeId;
	
    public String getTitle() {
        return "Edit Custom Alert Mode";
    }
	
	public CustomAlertModeEditGui(GuiScreen parentGui, String customAlertModeId) {
		super(parentGui);
		
		this.customAlertModeId = customAlertModeId;
	}
	
	@Override
	public void initGui() {
		super.initGui();
        
		if (scrollList == null) {
			scrollList = new CustomModeEditList(this, customAlertModeId);
		}
		else if (scrollList instanceof CustomModeEditList) {
			((CustomModeEditList) scrollList).resize();
		}

		buttonList.add(new DoneButton(504704698, this.width / 2 - 155, this.height - 29, 150, 20, "Save", this));
		buttonList.add(new DoneButton(504704699, this.width / 2 + 5, this.height - 29, 150, 20, "Cancel", this));
		if (ScathaPro.getInstance().config.getBoolean(Config.Key.devMode)) {
			buttonList.add(new GuiButton(504704697, this.width / 2 + 165, this.height - 29, 50, 20, "Open dir"));
		}
	}
	
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
    	super.actionPerformed(button);
    	
    	switch (button.id)
    	{
	    	case 504704698:
	    		if (scrollList instanceof CustomModeEditList && ((CustomModeEditList) scrollList).saveChanges()) {
	    			CustomAlertModeManager customAlertModeManager = ScathaPro.getInstance().customAlertModeManager;
	    			if (customAlertModeManager.getCurrentSubmodeId().equals(customAlertModeId)) customAlertModeManager.reloadResourcePack();
	    		}
	    		break;

	    	case 504704697:
	    		File directory = CustomAlertModeManager.getSubModeFile(customAlertModeId);
	    		Runtime.getRuntime().exec("explorer.exe " + directory.getAbsolutePath());
	    		break;
    	}
    }

	@Override
    public void onGuiClosed()
    {
		if (scrollList instanceof CustomModeEditList) ((CustomModeEditList) scrollList).onGuiClosed();
    }

}
