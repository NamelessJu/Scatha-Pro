package namelessju.scathapro.overlay.elements;

import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;

public class AnimatedOverlayImage extends OverlayImage
{
    public int frameCount = 1;
    public int frameTimeMs = 1;
    
    public AnimatedOverlayImage(String texturePath, int textureWidth, int frameHeight, int x, int y, float scale, int frameCount, int frameTimeMs)
    {
        super(texturePath, textureWidth, frameHeight, x, y, scale);
        setFrame(frameCount, frameTimeMs);
    }
    
    public AnimatedOverlayImage(int x, int y, float scale)
    {
        super(x, y, scale);
        setFrame(1, 1);
    }
    
    @Override
    protected void drawSpecific()
    {
        if (resourceLocation == null) return;
        
        int currentFrame = (int) (TimeUtil.getAnimationTime() / frameTimeMs % frameCount);
        int v = currentFrame * textureHeight;
        
        Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);
        Util.startImageRendering();
        GlStateManager.color(r, g, b, 1f);
        GuiIngame.drawModalRectWithCustomSizedTexture(0, 0, 0, v, textureWidth, textureHeight, textureWidth, textureHeight * frameCount);
        Util.endImageRendering();
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
