package com.namelessju.scathapro.overlay;

import net.minecraft.client.renderer.GlStateManager;

public abstract class OverlayElement
{
    public static enum Alignment
    {
        AUTOMATIC, LEFT, CENTER, RIGHT;
    }
    
    protected int x, y;
    protected float scale;
    protected Alignment alignment = Alignment.AUTOMATIC;
    protected boolean visible = true;
    
    public OverlayElement(int x, int y, float scale)
    {
        this.x = x;
        this.y = y;
        this.scale = scale;
    }
    
    public OverlayElement setAlignment(Alignment alignment)
    {
        this.alignment = alignment;
        return this;
    }
    
    public void draw()
    {
        if (!visible) return;

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        
        GlStateManager.pushMatrix();
        
        switch (alignment)
        {
            case CENTER:
                GlStateManager.translate(x - getWidth(false) / 2, y, 0);
                break;
            case RIGHT:
                GlStateManager.translate(x - getWidth(false), y, 0);
                break;
            default:
                GlStateManager.translate(x, y, 0);
                break;
        }
        
        GlStateManager.scale(scale, scale, scale);
        
        drawSpecific();
        
        GlStateManager.popMatrix();
        // Reset color to white, otherwise Minecraft's rendering might incorrectly color something as it doesn't always reset the color itself
        GlStateManager.color(1f, 1f, 1f);
    }
    
    protected abstract void drawSpecific();

    public void setPosition(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    public void setScale(float scale)
    {
        this.scale = scale;
    }
    
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public int getWidth()
    {
        return getWidth(true);
    }
    
    public int getHeight()
    {
        return getHeight(true);
    }
    
    public abstract int getWidth(boolean scaled);
    public abstract int getHeight(boolean scaled);

    public boolean isVisible()
    {
        return visible;
    }
}
