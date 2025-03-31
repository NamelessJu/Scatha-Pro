package com.namelessju.scathapro.overlay.elements;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

public class OverlayContainer extends OverlayElement
{
    protected List<OverlayElement> children = new ArrayList<OverlayElement>();
    public Integer backgroundColor = null;
    public int padding = 0;
    
    public OverlayContainer(int x, int y, float scale)
    {
        super(x, y, scale);
    }
    
    @Override
    protected void drawSpecific()
    {
        if (backgroundColor != null) Gui.drawRect(0, 0, getWidth(), getHeight(), backgroundColor);
        
        GlStateManager.pushMatrix();
        if (padding != 0) GlStateManager.translate(padding, padding, 0);
        
        for (OverlayElement element : children)
        {
            element.draw();
        }
        
        GlStateManager.popMatrix();
    }
    
    public void add(OverlayElement element)
    {
        children.add(element);
    }
    
    public void clearChildren()
    {
        children.clear();
    }
    
    public List<OverlayElement> getChildren()
    {
        return children;
    }
    
    @Override
    public int getWidth()
    {
        int width = 0;
        
        for (OverlayElement child : children)
        {
            if (!child.expandsContainerSize || !child.isVisible() || isElementEmptyContainer(child)) continue;
            
            int elementRequiredWidth = child.getX() + child.getScaledWidth() + child.marginRight;
            if (elementRequiredWidth > width) width = elementRequiredWidth;
        }
        
        return width + padding * 2;
    }
    
    @Override
    public int getHeight()
    {
        int height = 0;
        
        for (OverlayElement child : children)
        {
            if (!child.expandsContainerSize || !child.isVisible() || isElementEmptyContainer(child)) continue;
            
            int elementRequiredHeight = child.getY() + child.getScaledHeight() + child.marginBottom;
            if (elementRequiredHeight > height) height = elementRequiredHeight;
        }
        
        return height + padding * 2;
    }
    
    public static boolean isElementEmptyContainer(OverlayElement element)
    {
        if (!(element instanceof OverlayContainer)) return false;
        
        OverlayContainer container = (OverlayContainer) element;
        
        if (container.children.size() == 0) return true;
        
        for (OverlayElement child : container.children)
        {
            if (child.isVisible() && !(isElementEmptyContainer(child))) return false;
        }
        
        return true;
    }
}
