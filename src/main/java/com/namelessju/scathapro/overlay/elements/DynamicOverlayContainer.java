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
    protected Alignment contentAlignment = null;
    
    public DynamicOverlayContainer(int x, int y, float scale, Direction direction)
    {
        super(x, y, scale);
        this.direction = direction;
    }
    
    public void setContentAlignment(Alignment alignment)
    {
        this.contentAlignment = alignment;
    }
    
    public Alignment getContentAlignment()
    {
        return this.contentAlignment;
    }

    @Override
    protected void drawSpecific()
    {
        int contentWidth = getContentWidth();
        
        if (backgroundColor >= 0) Gui.drawRect(0, 0, getWidth(contentWidth), getHeight(), backgroundColor);
        
        boolean firstVisible = true;
        int previousChildMargin = 0;
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(padding, padding, 0);
        
        for (int i = 0; i < children.size(); i ++)
        {
            OverlayElement child = children.get(i);
            if (!child.isVisible() || isElementEmptyContainer(child)) continue;
            
            int directionOffset = 0;
            
            GlStateManager.pushMatrix();
            switch (direction)
            {
                case VERTICAL:
                    directionOffset = firstVisible ? 0 : Math.max(child.getY(), previousChildMargin);
                    switch (contentAlignment)
                    {
                        case CENTER:
                            GlStateManager.translate(contentWidth / 2, directionOffset, 0);
                            break;
                        case RIGHT:
                            GlStateManager.translate(contentWidth - child.getX(), directionOffset, 0);
                            break;
                        default:
                            GlStateManager.translate(child.getX(), directionOffset, 0);
                            break;
                    }
                    
                    child.draw(false, contentAlignment);
                    
                    previousChildMargin = child.marginBottom;
                    break;
                    
                case HORIZONTAL:
                    directionOffset = firstVisible ? 0 : Math.max(child.getX(), previousChildMargin);
                    GlStateManager.translate(directionOffset, child.getY(), 0);
                    
                    child.drawUnaligned();
                    
                    previousChildMargin = child.marginRight;
                    break;
            }
            GlStateManager.popMatrix();
            
            firstVisible = false;
            
            switch (direction)
            {
                case VERTICAL:
                    GlStateManager.translate(0, directionOffset + child.getScaledHeight(), 0);
                    break;
                    
                case HORIZONTAL:
                    GlStateManager.translate(directionOffset + child.getScaledWidth(), 0, 0);
                    break;
            }
        }
        
        GlStateManager.popMatrix();
    }
    
    @Override
    public int getWidth()
    {
        return getWidth(getContentWidth());
    }

    private int getWidth(int contentWidth)
    {
        return contentWidth + padding * 2;
    }
    
    public int getContentWidth()
    {
        int width = 0;
        
        boolean firstVisible = true;
        int previousChildMargin = 0;
        
        for (OverlayElement child : children)
        {
            if (!child.isVisible() || isElementEmptyContainer(child)) continue;
            
            int currentWidth;
            switch (direction)
            {
                case VERTICAL:
                    currentWidth = child.getX() + child.getScaledWidth() + child.marginRight;
                    if (currentWidth > width) width = currentWidth;
                    break;
                    
                case HORIZONTAL:
                    int directionOffset = firstVisible ? 0 : Math.max(child.getX(), previousChildMargin);
                    currentWidth = directionOffset + child.getScaledWidth();
                    
                    width += currentWidth;
                    
                    firstVisible = false;
                    previousChildMargin = child.marginRight;
                    break;
            }
        }
        
        return width;
    }
    
    @Override
    public int getHeight()
    {
        return getHeight(getContentHeight());
    }

    public int getHeight(int contentHeight)
    {
        return contentHeight + padding * 2;
    }
    
    public int getContentHeight()
    {
        int height = 0;
        
        boolean firstVisible = true;
        int previousChildMargin = 0;
        
        for (OverlayElement child : children)
        {
            if (!child.isVisible() || isElementEmptyContainer(child)) continue;
            
            int currentHeight;
            switch (direction)
            {
                case VERTICAL:
                    int directionOffset = firstVisible ? 0 : Math.max(child.getY(), previousChildMargin);
                    currentHeight = directionOffset + child.getScaledHeight();
                    
                    height += currentHeight;
                    
                    firstVisible = false;
                    previousChildMargin = child.marginBottom;
                    break;
                    
                case HORIZONTAL:
                    currentHeight = child.getY() + child.getScaledHeight() + child.marginBottom;
                    if (currentHeight > height) height = currentHeight;
                    break;
            }
        }
        
        return height;
    }
}
