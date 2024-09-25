package com.namelessju.scathapro.gui.elements;

import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.gui.menus.ScathaProGui;
import com.namelessju.scathapro.util.TextUtil;

import net.minecraft.client.Minecraft;

public interface TooltipElement
{
    // ugly nesting because interfaces can't do shit (this drove me mad)
    public Tooltip getTooltip();
    
    public static class Tooltip
    {
        private String[] tooltipLines = null;
        
        public void render()
        {
            if (Minecraft.getMinecraft().currentScreen instanceof ScathaProGui)
            {
                ((ScathaProGui) Minecraft.getMinecraft().currentScreen).tooltipToRender = this;
            }
        }
        
        public void setTooltip(String text)
        {
            if (text != null && !text.isEmpty())
            {
                tooltipLines = TextUtil.splitOnLineBreaks(text);
            }
            else tooltipLines = null;
        }
        
        public List<String> getTooltipLines()
        {
            return tooltipLines != null ? Lists.newArrayList(tooltipLines) : null;
        }
    }
}
