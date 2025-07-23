package namelessju.scathapro.overlay.elements;

import net.minecraft.client.renderer.GlStateManager;

public abstract class OverlayElement
{
    public static enum Alignment
    {
        LEFT("Left"), CENTER("Center"), RIGHT("Right");
        
        private String name;
        
        Alignment(String name)
        {
            this.name = name;
        }
        
        @Override
        public String toString()
        {
            return name;
        }
    }
    
    protected int x, y;
    protected float scale;
    protected Alignment alignment = Alignment.LEFT;
    protected boolean visible = true;
    protected int marginRight = 0, marginBottom = 0;
    public boolean expandsContainerSize = true;
    
    public OverlayElement(int x, int y, float scale)
    {
        this.x = x;
        this.y = y;
        this.scale = scale;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends OverlayElement> T setMargin(int right, int bottom)
    {
        this.marginRight = right;
        this.marginBottom = bottom;
        return (T) this;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends OverlayElement> T setAlignment(Alignment alignment)
    {
        this.alignment = alignment;
        return (T) this;
    }
    
    public void draw()
    {
        draw(true, true, this.alignment);
    }
    
    public void draw(boolean positioned, boolean scaled, Alignment alignment)
    {
        if (!visible) return;
        
        GlStateManager.pushMatrix();
        
        if (positioned) GlStateManager.translate(x, y, 0);
        
        if (alignment != null)
        {
            switch (alignment)
            {
                case CENTER:
                    GlStateManager.translate(-Math.floor((getWidth() * scale) / 2f), 0, 0);
                    break;
                case RIGHT:
                    GlStateManager.translate(-Math.floor(getWidth() * scale), 0, 0);
                    break;
                default: break;
            }
        }
        
        if (scaled) GlStateManager.scale(scale, scale, 1f);
        
        drawSpecific();
        
        GlStateManager.popMatrix();
        GlStateManager.resetColor();
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
    
    public float getScale()
    {
        return this.scale;
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
    
    public int getScaledWidth()
    {
        return (int) Math.ceil(getWidth() * scale);
    }
    
    public int getScaledHeight()
    {
        return (int) Math.ceil(getHeight() * scale);
    }
    
    public abstract int getWidth();
    public abstract int getHeight();

    public boolean isVisible()
    {
        return visible;
    }
}
