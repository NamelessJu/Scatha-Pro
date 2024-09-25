package com.namelessju.scathapro.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class ScathaProButton extends GuiButton implements TooltipElement
{
    private final Tooltip tooltip = new Tooltip();
    
    public ScathaProButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        super.drawButton(mc, mouseX, mouseY);
        
        if (this.visible && this.hovered) this.tooltip.render();
    }
    
    public ScathaProButton setTooltip(String text)
    {
        tooltip.setTooltip(text);
        return this;
    }
    
    @Override
    public Tooltip getTooltip()
    {
        return tooltip;
    }
}
