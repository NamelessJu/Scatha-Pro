package namelessju.scathapro.gui.overlay.elements;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class OverlayText extends OverlayElement
{
    protected final Font font;
    protected Component text;
    protected int color;

    public OverlayText(Font font, int color, int x, int y, float scale)
    {
        this((Component) null, font, color, x, y, scale);
    }

    public OverlayText(String text, Font font, int color, int x, int y, float scale)
    {
        this(Component.literal(text), font, color, x, y, scale);
    }

    public OverlayText(Component text, Font font, int color, int x, int y, float scale)
    {
        super(x, y, scale);
        this.text = text;
        this.font = font;
        this.color = color;
    }

    @Override
    protected void extractContent(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        if (text == null) return;
        guiGraphics.text(font, text, 0, 0, color, true);
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