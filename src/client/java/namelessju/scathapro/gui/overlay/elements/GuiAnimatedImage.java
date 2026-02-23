package namelessju.scathapro.gui.overlay.elements;

import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;

public class GuiAnimatedImage extends GuiImage
{
    public int frameCount = 1;
    public int frameTimeMs = 1;
    
    public GuiAnimatedImage(String texturePath, int frameCount, int frameTimeMs, int textureWidth, int frameHeight, int x, int y, float scale)
    {
        super(texturePath, textureWidth, frameHeight, x, y, scale);
        setFrame(frameCount, frameTimeMs);
    }
    
    public GuiAnimatedImage(int x, int y, float scale)
    {
        super(x, y, scale);
        setFrame(1, 1);
    }
    
    @Override
    protected void renderContent(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        if (imageIdentifier == null) return;
        
        int currentFrame = (int) (TimeUtil.now() / frameTimeMs % frameCount);
        float v = currentFrame * textureHeight;
        
        renderColoredImage(guiGraphics,
            0f, v,
            textureWidth, textureHeight,
            textureWidth, textureHeight * frameCount
        );
    }
    
    @Override
    public void setImage(String texturePath, int textureWidth, int textureHeight)
    {
        setImage(texturePath, textureWidth, textureHeight, 1, 1);
    }
    
    public void setImage(String texturePath, int textureWidth, int textureHeight, int frameCount, int frameTimeMs)
    {
        super.setImage(texturePath, textureWidth, textureHeight);
        setFrame(frameCount, frameTimeMs);
    }
    
    protected void setFrame(int frameCount, int frameTimeMs)
    {
        this.frameCount = frameCount > 0 ? frameCount : 1;
        this.frameTimeMs = frameTimeMs > 0 ? frameTimeMs : 1;
    }
}
