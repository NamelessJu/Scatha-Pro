package namelessju.scathapro.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class ScathaProButton extends GuiButton implements IGuiElement, ITooltipElement
{
    private final Tooltip tooltip = new Tooltip();
    
    public ScathaProButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
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
    public int getElementHeight()
    {
        return this.height;
    }
    
    @Override
    public int getElementWidth()
    {
        return this.width;
    }
    
    @Override
    public boolean elementMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (this.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY))
        {
            this.playPressSound(Minecraft.getMinecraft().getSoundHandler());
            if (this instanceof IClickActionButton) ((IClickActionButton) this).click();
            return true;
        }
        return false;
    }
    
    @Override
    public void elementDraw(int mouseX, int mouseY)
    {
        this.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
    }
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        super.drawButton(mc, mouseX, mouseY);
        
        if (this.visible && this.hovered) this.tooltip.requestRender();
    }
    
    @Override
    public Tooltip getTooltip()
    {
        return tooltip;
    }
}
