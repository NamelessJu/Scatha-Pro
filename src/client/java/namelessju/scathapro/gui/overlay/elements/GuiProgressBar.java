package namelessju.scathapro.gui.overlay.elements;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class GuiProgressBar extends GuiElement
{
    private final int width;
    private final int height;
    private final int foregroundColor;
    private final int backgroundColor;
    
    private float progress = 0f;

    public GuiProgressBar(int x, int y, int width, int height, float scale, int foregroundColor, int backgroundColor)
    {
        super(x, y, scale);
        this.width = width;
        this.height = height;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        if (backgroundColor >= 0) guiGraphics.fill(0, 0, width, height, backgroundColor);
        if (foregroundColor >= 0) guiGraphics.fill(0, 0, Mth.floor(width * progress), height, foregroundColor);
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
    
    public void setProgress(float progress)
    {
        this.progress = Math.min(Math.max(progress, 0f), 1f); 
    }
}
