package com.namelessju.scathapro.gui;

import com.namelessju.scathapro.ScathaPro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ImageButton extends GuiButton {

    private ResourceLocation resourceLocation;
    private int textureWidth, textureHeight;
    private float textureScale;

    public ImageButton(int buttonId, int x, int y, int widthIn, int heightIn, String texturePath, int textureWidth, int textureHeight, float textureScale) {
        super(buttonId, x, y, widthIn, heightIn, null);
        setImage(texturePath, textureWidth, textureHeight, textureScale);
    }
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);

        if (visible) {
            GlStateManager.enableAlpha();
            GlStateManager.color(1f, 1f, 1f);
            Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(xPosition + width/2 - (textureWidth * textureScale)/2, yPosition + height/2 - (textureHeight * textureScale)/2 - 0.5f, 0);
            GlStateManager.scale(textureScale, textureScale, 1);
            GuiIngame.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
            GlStateManager.popMatrix();
        }
    }

    public void setImage(String texturePath, int textureWidth, int textureHeight, float textureScale) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.textureScale = textureScale;
        resourceLocation = (texturePath != null && !texturePath.isEmpty()) ? new ResourceLocation(ScathaPro.MODID, "textures/" + texturePath) : null;
    }
}