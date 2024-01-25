package com.namelessju.scathapro.gui.menus;

import com.namelessju.scathapro.gui.elements.DoneButton;

import com.namelessju.scathapro.gui.elements.CustomAlertModeGuiList;

import net.minecraft.client.gui.GuiScreen;

public class CustomAlertModeGui extends ScathaProGui {

    public String getTitle() {
        return "Custom Alert Modes";
    }
    
    public boolean hasBackground() {
        return false;
    }
    
    
	public CustomAlertModeGui(GuiScreen parentGui) {
		super(parentGui);
	}

    @Override
    public void initGui() {
        super.initGui();
        
        scrollList = new CustomAlertModeGuiList(this);
        
        buttonList.add(new DoneButton(504704599, this.width / 2 - 100, this.height - 30, 200, 20, "Done", this));
    }
    
}
