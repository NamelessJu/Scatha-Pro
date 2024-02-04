package com.namelessju.scathapro.overlay;

public class OverlaySpacing extends OverlayElement
{
    public int width;
    public int height;

    public OverlaySpacing(int width, int height)
    {
        super(0, 0, 1f);
        this.width = width;
        this.height = height;
    }

    @Override
    protected void drawSpecific() {}
    
    @Override
    public int getWidth(boolean scaled)
    {
        return Math.round(Math.max(0, width) * (scaled ? scale : 1));
    }

    @Override
    public int getHeight(boolean scaled)
    {
        return Math.round(Math.max(0, height) * (scaled ? scale : 1));
    }
    
}
