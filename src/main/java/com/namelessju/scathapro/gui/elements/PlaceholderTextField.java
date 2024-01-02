package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.util.Util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class PlaceholderTextField extends GuiTextField {
	
	private String placeholder = null;
	private FontRenderer fontRendererInstance;

	public PlaceholderTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height) {
		super(componentId, fontrendererObj, x, y, width, height);
		fontRendererInstance = fontrendererObj;
	}
	
	@Override
    public void drawTextBox() {
		super.drawTextBox();
		
        if (placeholder != null && getVisible() && getText().isEmpty())
        {
        	this.fontRendererInstance.drawStringWithShadow(placeholder, xPosition + 4, yPosition + (height - 8) / 2, Util.Color.DARK_GRAY.getValue());
        }
	}
	
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

}
