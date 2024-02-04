package com.namelessju.scathapro.overlay;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

public class OverlayContainer extends OverlayElement
{
    protected List<OverlayElement> elements = new ArrayList<OverlayElement>();
    public int backgroundColor = -1;
    public int padding = 0;

    public OverlayContainer(int x, int y, float scale)
    {
        super(x, y, scale);
    }
    
    @Override
    protected void drawSpecific()
    {
        if (backgroundColor >= 0) Gui.drawRect(0, 0, getWidth(false), getHeight(false), backgroundColor);

        GlStateManager.pushMatrix();
        GlStateManager.translate(padding, padding, 0);
        
        for (OverlayElement element : elements)
        {
            element.draw();
        }
        
        GlStateManager.popMatrix();
    }
    
    public void add(OverlayElement element)
    {
        elements.add(element);
    }
    
    @Override
    public int getWidth(boolean scaled)
    {
        int width = 0;
        
        for (OverlayElement element : elements)
        {
            if (!element.isVisible()) continue;
            
            int elementWidth = element.getWidth();
            int elementRequiredWidth = element.getX() + elementWidth;
            if (elementRequiredWidth > width) width = elementRequiredWidth;
        }
        
        return Math.round((width + padding * 2) * (scaled ? scale : 1));
    }

    @Override
    public int getHeight(boolean scaled)
    {
        int height = 0;
        
        for (OverlayElement element : elements)
        {
            if (!element.isVisible()) continue;
            int elementRequiredHeight = element.getY() + element.getHeight();
            if (elementRequiredHeight > height) height = elementRequiredHeight;
        }
        
        return Math.round((height + padding * 2) * (scaled ? scale : 1));
    }
}
