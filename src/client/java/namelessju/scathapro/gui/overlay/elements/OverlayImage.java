package namelessju.scathapro.gui.overlay.elements;

import namelessju.scathapro.miscellaneous.data.Texture;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class OverlayImage extends OverlayElement
{
    protected int width;
    protected int height;
    protected @Nullable Texture texture;
    protected int color = -1;

    public OverlayImage(Texture texture, int x, int y, float scale)
    {
        this(texture, x, y, texture.width(), texture.height(), scale);
    }

    public OverlayImage(Texture texture, int x, int y, int width, int height, float scale)
    {
        this(x, y, width, height, scale);
        setImage(texture);
    }

    public OverlayImage(int x, int y, int width, int height, float scale)
    {
        super(x, y, scale);
        this.width = width;
        this.height = height;
    }

    @Override
    protected void extractContent(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        if (texture == null) return;
        texture.extract(guiGraphics, 0, 0, width, height, color);
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

    public void setImage(Texture texture)
    {
        this.texture = texture;
    }

    public void clearImage()
    {
        this.texture = null;
    }

    public void setColor(int color)
    {
        this.color = color;
    }
}