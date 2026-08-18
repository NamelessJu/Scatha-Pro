package namelessju.scathapro.gui.overlay.elements;

import namelessju.scathapro.miscellaneous.data.Texture;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OverlayAnimatedImage extends OverlayImage
{
    public int frameCount = 1;
    public int frameTimeMs = 1;

    public OverlayAnimatedImage(Texture texture, int frameCount, int frameTimeMs, int x, int y, int width, int height, float scale)
    {
        super(texture, x, y, width, height, scale);
        setFrameInfo(frameCount, frameTimeMs);
    }

    public OverlayAnimatedImage(int x, int y, int width, int height, float scale)
    {
        super(x, y, width, height, scale);
    }

    @Override
    protected void extractContent(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        if (texture == null) return;

        int currentFrame = (int) (TimeUtil.getEpochMilliseconds() / frameTimeMs % frameCount);
        float v = currentFrame * texture.height();

        texture.extractAnimated(guiGraphics, 0, 0, width, height, v, frameCount, color);
    }

    @Override
    public void setImage(Texture texture)
    {
        setImage(texture, 1, 1);
    }

    public void setImage(Texture texture, int frameCount, int frameTimeMs)
    {
        super.setImage(texture);
        setFrameInfo(frameCount, frameTimeMs);
    }

    protected void setFrameInfo(int frameCount, int frameTimeMs)
    {
        this.frameCount = frameCount > 0 ? frameCount : 1;
        this.frameTimeMs = frameTimeMs > 0 ? frameTimeMs : 1;
    }
}