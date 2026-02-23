package namelessju.scathapro.gui.overlay.elements;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiText extends GuiElement
{
    protected Component text;
    protected Font font;
    protected int color;
    
    public GuiText(Font font, int color, int x, int y, float scale)
    {
        this((Component) null, font, color, x, y, scale);
    }
    
    public GuiText(String text, Font font, int color, int x, int y, float scale)
    {
        this(Component.literal(text), font, color, x, y, scale);
    }
    
    public GuiText(Component text, Font font, int color, int x, int y, float scale)
    {
        super(x, y, scale);
        this.text = text;
        this.font = font;
        this.color = color;
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        if (text != null)
        {
            guiGraphics.drawString(font, text, 0, 0, color, true);
        }
    }
    
    @Override
    public int getWidth()
    {
        return text != null ? font.width(text) : 0;
    }
    
    @Override
    public int getHeight()
    {
        return font.lineHeight;
    }
    
    public void setText(String text)
    {
        this.text = Component.literal(text);
    }

    public void setText(Component text)
    {
        this.text = text;
    }

    public void setColor(int color)
    {
        this.color = color;
    }
}
