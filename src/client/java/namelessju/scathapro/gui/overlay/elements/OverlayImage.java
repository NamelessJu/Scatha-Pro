package namelessju.scathapro.gui.overlay.elements;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class OverlayImage extends OverlayElement
{
    protected Identifier imageIdentifier;
    protected int textureWidth, textureHeight;
    protected int color = -1;
    
    public OverlayImage(String texturePath, int textureWidth, int textureHeight, int x, int y, float scale)
    {
        super(x, y, scale);
        setImage(texturePath, textureWidth, textureHeight);
    }
    
    public OverlayImage(int x, int y, float scale)
    {
        super(x, y, scale);
        setImage(null, 1, 1);
    }
    
    @Override
    protected void extractContent(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        if (imageIdentifier == null) return;
        extractColoredImage(guiGraphics,
            0f, 0f,
            textureWidth, textureHeight,
            textureWidth, textureHeight
        );
    }
    
    @SuppressWarnings("SameParameterValue")
    protected void extractColoredImage(GuiGraphicsExtractor guiGraphics,
                                       float u, float v,
                                       int renderWidth, int renderHeight,
                                       int textureWidth, int textureHeight)
    {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, imageIdentifier,
            0, 0, u, v,
            renderWidth, renderHeight, renderWidth, renderHeight,
            textureWidth, textureHeight,
            color
        );
    }
    
    @Override
    public int getWidth()
    {
        return textureWidth;
    }
    
    @Override
    public int getHeight()
    {
        return textureHeight;
    }
    
    public void setImage(String texturePath, int textureWidth, int textureHeight)
    {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        imageIdentifier = (texturePath != null && !texturePath.isEmpty())
            ? ScathaPro.getIdentifier("textures/" + texturePath)
            : null;
    }
    
    public void setColor(int color)
    {
        this.color = color;
    }
}
