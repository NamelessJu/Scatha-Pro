package com.namelessju.scathapro.overlay.elements;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.util.TimeUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public class AnomalousDesireEffectProgressBar extends OverlayElement
{
    private static final ResourceLocation resourceLocation = new ResourceLocation(ScathaPro.MODID, "textures/overlay/anomalous_desire_progress_bar.png");
    
    private int width;
    private int height;
    
    private float progress = 0f;
    
    public AnomalousDesireEffectProgressBar(int x, int y, int width, int height, float scale)
    {
        super(x, y, scale);
        this.width = width;
        this.height = height;
    }
    
    @Override
    protected void drawSpecific()
    {
        int yOffset = (int) ((TimeUtil.now() / 100L) % 32);

        Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);
        Util.startImageRendering();
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, yOffset, Math.round(width * progress), height, 32, 32);
        Util.endImageRendering();
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
