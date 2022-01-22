package com.namelessju.scathapro.overlay;

import com.namelessju.scathapro.ScathaPro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class OverlayImage extends OverlayElement {
    
    protected ResourceLocation resourceLocation;
    protected int textureWidth, textureHeight;
    
    public OverlayImage(String texturePath, int textureWidth, int textureHeight, int x, int y, float scale) {
        super(x, y, scale);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        setImage(texturePath);
    }
    
    @Override
    protected void drawSpecific() {
        if (resourceLocation != null) {
            Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);
            GlStateManager.color(1f, 1f, 1f);
            GuiIngame.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        }
    }
    
    @Override
    public int getWidth(boolean scaled) {
        return (int) Math.round(textureWidth * (scaled ? scale : 1));
    }
    
    @Override
    public int getHeight(boolean scaled) {
        return (int) Math.round(textureHeight * (scaled ? scale : 1));
    }
    
    public void setImage(String texturePath) {
        resourceLocation = (texturePath != null && !texturePath.isEmpty()) ? new ResourceLocation(ScathaPro.MODID, "textures/" + texturePath) : null;
    }
    
}
