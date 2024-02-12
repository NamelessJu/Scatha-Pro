package com.namelessju.scathapro.overlay.elements;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

public class DynamicOverlayContainer extends OverlayContainer
{
    public static enum Direction
    {
        VERTICAL, HORIZONTAL;
    }
    
    public Direction direction;
    public int cellMargin = 0;
    
    public DynamicOverlayContainer(int x, int y, float scale, Direction direction)
    {
        super(x, y, scale);
        this.direction = direction;
    }

    @Override
    protected void drawSpecific()
    {
        if (backgroundColor >= 0) Gui.drawRect(0, 0, getWidth(false), getHeight(false), backgroundColor);

        GlStateManager.pushMatrix();
        GlStateManager.translate(padding, padding, 0);
        
        for (OverlayElement element : elements)
        {
            if (!element.isVisible() || isElementEmptyContainer(element))  continue;
            
            element.draw();
            
            switch (direction)
            {
                case VERTICAL:
                    GlStateManager.translate(0, element.getY() + element.getHeight() + cellMargin, 0);
                    break;
                    
                case HORIZONTAL:
                    GlStateManager.translate(element.getX() + element.getWidth() + cellMargin, 0, 0);
            }
        }
        
        GlStateManager.popMatrix();
    }

    @Override
    public int getWidth(boolean scaled)
    {
        int width = 0;
        
        for (OverlayElement element : elements)
        {
            if (!element.isVisible() || isElementEmptyContainer(element)) continue;
            
            int elementWidth = element.getWidth();
            int elementRequiredWidth = element.getX() + elementWidth;

            switch (direction)
            {
                case HORIZONTAL:
                    width += elementRequiredWidth;
                    break;

                case VERTICAL:
                    if (elementRequiredWidth > width) width = elementRequiredWidth;
            }
        }
        
        return Math.round((width + padding * 2) * (scaled ? scale : 1));
    }
    
    @Override
    public int getHeight(boolean scaled)
    {
        int height = 0;
        
        for (OverlayElement element : elements)
        {
            if (!element.isVisible() || isElementEmptyContainer(element)) continue;
            int elementRequiredHeight = element.getY() + element.getHeight();

            switch (direction)
            {
                case VERTICAL:
                    height += elementRequiredHeight;
                    break;
                    
                case HORIZONTAL:
                    if (elementRequiredHeight > height) height = elementRequiredHeight;
            }
        }
        
        return Math.round((height + padding * 2) * (scaled ? scale : 1));
    }
    
    private boolean isElementEmptyContainer(OverlayElement element)
    {
        if (!(element instanceof OverlayContainer)) return false;
        
        OverlayContainer container = (OverlayContainer) element;
        for (OverlayElement child : container.elements)
        {
            if (child.isVisible() && !(isElementEmptyContainer(child))) return false;
        }
        
        return true;
    }
}
