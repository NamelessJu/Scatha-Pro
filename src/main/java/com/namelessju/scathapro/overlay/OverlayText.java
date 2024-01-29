package com.namelessju.scathapro.overlay;

import net.minecraft.client.Minecraft;

public class OverlayText extends OverlayElement
{
    protected String text;
    protected int color;
    
    public OverlayText(String text, int color, int x, int y, float scale)
    {
        super(x, y, scale);
        this.text = text;
        this.color = color;
    }

    @Override
    protected void drawSpecific()
    {
        if (text != null && !text.isEmpty()) Minecraft.getMinecraft().fontRendererObj.drawString(text, 0, 0, color, true);
    }

    @Override
    public int getWidth(boolean scaled)
    {
        return text != null ? (int) Math.round(Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) * (scaled ? scale : 1)) : 0;
    }

    @Override
    public int getHeight(boolean scaled)
    {
        return (int) Math.round(8 * (scaled ? scale : 1));
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public void setColor(int color)
    {
        this.color = color;
    }
}
