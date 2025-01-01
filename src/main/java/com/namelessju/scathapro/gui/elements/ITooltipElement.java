package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.gui.menus.ScathaProGui;
import com.namelessju.scathapro.util.TextUtil;

import net.minecraft.client.Minecraft;

public interface ITooltipElement
{
    public Tooltip getTooltip();
    
    public static class Tooltip
    {
        private String[] tooltipLines = null;
        private int maxWidth = -1;
        
        public void requestRender()
        {
            if (Minecraft.getMinecraft().currentScreen instanceof ScathaProGui)
            {
                ((ScathaProGui) Minecraft.getMinecraft().currentScreen).tooltipToRender = this;
            }
        }
        
        public void setText(String text)
        {
            setText(text, 200);
        }
        
        public void setText(String text, int maxWidth)
        {
            if (text != null && !text.isEmpty())
            {
                tooltipLines = TextUtil.splitOnLineBreaks(text);
            }
            else tooltipLines = null;
            
            this.maxWidth = maxWidth;
        }
        
        public String[] getTextLines()
        {
            return tooltipLines;
        }
        
        public int getMaxWidth()
        {
            return maxWidth;
        }
    }
}
