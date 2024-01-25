package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.gui.menus.ScathaProGui;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DoneButton extends GuiButton implements IClickActionButton {
	
	public ScathaProGui gui;
	
    public DoneButton(int buttonId, int x, int y, int widthIn, int heightIn, String displayString, ScathaProGui gui) {
        super(buttonId, x, y, widthIn, heightIn, displayString);
        
        this.gui = gui;
    }
    
	@Override
	public void click() {
		gui.openParentGui();
	}
}
