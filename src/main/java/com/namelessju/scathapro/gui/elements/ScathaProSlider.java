package com.namelessju.scathapro.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiSlider;

public class ScathaProSlider extends GuiSlider implements IGuiElement
{
    public ScathaProSlider(int id, int xPos, int yPos, int width, int height, String prefix, String suf, double minVal,
        double maxVal, double currentVal, boolean showDec, boolean drawStr)
    {
        this(id, xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, null);
    }
    
    public ScathaProSlider(int id, int xPos, int yPos, int width, int height, String prefix, String suf, double minVal,
        double maxVal, double currentVal, boolean showDec, boolean drawStr, ISlider par)
    {
        super(id, xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, par);
    }
    
    @Override
    public void setElementX(int x)
    {
        this.xPosition = x;
    }
    
    @Override
    public void setElementY(int y)
    {
        this.yPosition = y;
    }
    
    @Override
    public void setElementWidth(int width)
    {
        this.width = width;
    }
    
    @Override
    public void setElementHeight(int height)
    {
        this.height = height;
    }
    
    @Override
    public int getElementX()
    {
        return this.xPosition;
    }
    
    @Override
    public int getElementY()
    {
        return this.yPosition;
    }
    
    @Override
    public int getElementWidth()
    {
        return this.width;
    }
    
    @Override
    public int getElementHeight()
    {
        return this.height;
    }
    
    @Override
    public boolean elementMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY))
        {
            this.playPressSound(Minecraft.getMinecraft().getSoundHandler());
            return true;
        }
        return false;
    }
    
    @Override
    public void elementMouseReleased(int mouseX, int mouseY)
    {
        super.mouseReleased(mouseX, mouseY);
    }
    
    @Override
    public void elementDraw(int mouseX, int mouseY)
    {
        this.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
    }
}
