package namelessju.scathapro.overlay.elements;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class OverlayImage extends OverlayElement
{
    protected ResourceLocation resourceLocation;
    protected int textureWidth, textureHeight;
    protected float r = 1f;
    protected float g = 1f;
    protected float b = 1f;
    
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
    protected void drawSpecific()
    {
        if (resourceLocation == null) return;
        
        Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);
        Util.startImageRendering();
        GlStateManager.color(r, g, b, 1f);
        GuiIngame.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        Util.endImageRendering();
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
        resourceLocation = (texturePath != null && !texturePath.isEmpty()) ? new ResourceLocation(ScathaPro.MODID, "textures/" + texturePath) : null;
    }
    
    public void setColor(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }
    
    public void setColor(int color)
    {
        this.r = ((color >> 16) & 0xFF) / 255f;
        this.g = ((color >> 8) & 0xFF) / 255f;
        this.b = (color & 0xFF) / 255f;
    }
}
