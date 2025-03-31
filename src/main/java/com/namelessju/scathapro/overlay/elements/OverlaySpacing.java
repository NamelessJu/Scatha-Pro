package com.namelessju.scathapro.overlay.elements;

public class OverlaySpacing extends OverlayElement
{
    private int width, height;
    
    public OverlaySpacing(int x, int y, int width, int height, float scale)
    {
        super(x, y, scale);
        
        setWidth(width);
        setHeight(height);
    }
    
    public void setWidth(int width)
    {
        this.width = Math.max(width, 0);
    }
    
    public void setHeight(int height)
    {
        this.height = Math.max(height, 0);
    }
    
    @Override
    protected void drawSpecific()
    {
        // nothing here, just emptiness...
    }
    
    @Override
    public int getWidth()
    {
        return width;
    }
    
    @Override
    public int getHeight()
    {
        return height;
    }
}
