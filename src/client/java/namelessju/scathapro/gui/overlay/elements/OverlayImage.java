package namelessju.scathapro.gui.overlay.elements;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class OverlayImage extends OverlayElement
{
    protected @Nullable Identifier imageIdentifier;
    protected int textureWidth, textureHeight;
    protected int color = -1;

    public OverlayImage(String texturePath, int textureWidth, int textureHeight, int x, int y, float scale)
    {
        this(x, y, scale);
        setImage(texturePath, textureWidth, textureHeight);
    }

    public OverlayImage(int x, int y, float scale)
    {
        super(x, y, scale);
    }

    @Override
    protected void extractContent(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        if (imageIdentifier == null) return;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, imageIdentifier,
            0, 0, 0f, 0f,
            textureWidth, textureHeight,
            textureWidth, textureHeight, textureWidth, textureHeight,
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
        setImage(ScathaPro.getIdentifier("textures/" + texturePath),
            textureWidth, textureHeight
        );
    }

    public void setImage(Identifier identifier, int textureWidth, int textureHeight)
    {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        imageIdentifier = identifier;
    }

    public void clearImage()
    {
        setImage((Identifier) null, 0, 0);
    }

    public void setColor(int color)
    {
        this.color = color;
    }
}