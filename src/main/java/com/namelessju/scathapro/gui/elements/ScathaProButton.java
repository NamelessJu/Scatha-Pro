package com.namelessju.scathapro.gui.elements;

import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.gui.menus.ScathaProGui;
import com.namelessju.scathapro.util.MessageUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class ScathaProButton extends GuiButton
{
    private String[] tooltipLines = null;
    
    public ScathaProButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        super.drawButton(mc, mouseX, mouseY);
        
        handleTooltipRendering(mc);
    }
    
    public ScathaProButton setTooltip(String text)
    {
        if (text != null && !text.isEmpty())
        {
            tooltipLines = MessageUtil.splitOnLineBreaks(text);
        }
        else tooltipLines = null;
        
        return this;
    }
    
    public List<String> getTooltipLines()
    {
        return tooltipLines != null ? Lists.newArrayList(tooltipLines) : null;
    }
    
    protected void handleTooltipRendering(Minecraft mc)
    {
        if (visible && hovered && mc.currentScreen instanceof ScathaProGui)
        {
            ((ScathaProGui) mc.currentScreen).hoveredButton = this;
        }
    }
}
