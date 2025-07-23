package namelessju.scathapro.overlay.elements;

import net.minecraft.client.gui.Gui;

public class OverlayProgressBar extends OverlayElement
{
    private int width;
    private int height;
    private int foregroundColor;
    private int backgroundColor;
    
    private float progress = 0f;

    public OverlayProgressBar(int x, int y, int width, int height, float scale, int foregroundColor, int backgroundColor)
    {
        super(x, y, scale);
        this.width = width;
        this.height = height;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    @Override
    protected void drawSpecific()
    {
        if (backgroundColor >= 0) Gui.drawRect(0, 0, width, height, backgroundColor);
        if (foregroundColor >= 0) Gui.drawRect(0, 0, Math.round(width * progress), height, foregroundColor);
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
