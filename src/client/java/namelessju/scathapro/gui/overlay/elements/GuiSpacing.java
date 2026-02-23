package namelessju.scathapro.gui.overlay.elements;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class GuiSpacing extends GuiElement
{
    private int width, height;
    
    public GuiSpacing(int x, int y, int width, int height, float scale)
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
    protected void renderContent(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        // nothing to see here
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
