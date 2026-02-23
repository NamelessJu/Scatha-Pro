package namelessju.scathapro.gui.overlay.elements;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class GuiElement
{
    public enum Alignment
    {
        LEFT("Left"), CENTER("Center"), RIGHT("Right");
        
        private final String name;
        
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
    protected @NonNull Alignment alignment = Alignment.LEFT;
    protected boolean visible = true;
    protected int marginRight = 0, marginBottom = 0;
    public boolean expandsContainerSize = true;
    
    public GuiElement(int x, int y, float scale)
    {
        this.x = x;
        this.y = y;
        this.scale = scale;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends GuiElement> T setMargin(int right, int bottom)
    {
        this.marginRight = right;
        this.marginBottom = bottom;
        return (T) this;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends GuiElement> T setAlignment(@NonNull Alignment alignment)
    {
        this.alignment = alignment;
        return (T) this;
    }
    
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        if (!visible) return;
        forceRender(guiGraphics, deltaTracker, true, true, this.alignment);
    }
    
    public void forceRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, boolean positioned, boolean scaled, @Nullable Alignment alignment)
    {
        guiGraphics.nextStratum();
        guiGraphics.pose().pushMatrix();
        if (positioned) guiGraphics.pose().translate(x, y);
        if (alignment == null) alignment = this.alignment;
        switch (alignment)
        {
            case CENTER:
                guiGraphics.pose().translate(-Mth.floor((getWidth() * scale) / 2f), 0);
                break;
            case RIGHT:
                guiGraphics.pose().translate(-Mth.floor(getWidth() * scale), 0);
                break;
            default:
                break;
        }
        if (scaled) guiGraphics.pose().scale(scale, scale);
        
        renderContent(guiGraphics, deltaTracker);
        
        guiGraphics.pose().popMatrix();
    }
    
    protected abstract void renderContent(GuiGraphics guiGraphics, DeltaTracker deltaTracker);

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
