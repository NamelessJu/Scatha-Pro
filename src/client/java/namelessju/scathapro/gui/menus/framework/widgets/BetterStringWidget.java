package namelessju.scathapro.gui.menus.framework.widgets;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NullMarked;

@NullMarked
@SuppressWarnings("unused")
public class BetterStringWidget extends StringWidget
{
    public BetterStringWidget(Component message, Font font)
    {
        super(message, font);
    }

    public BetterStringWidget(int width, int height, Component message, Font font)
    {
        super(width, height, message, font);
    }

    public BetterStringWidget(int x, int y, int width, int height, Component message, Font font)
    {
        super(x, y, width, height, message, font);
    }

    @Override
    protected void extractScrollingStringOverContents(final ActiveTextCollector output, final Component message, final int margin)
    {
        // ignore the stupid ass margin that the vanilla class for some reason has only for scrolling text
        int left = this.getX();
        int right = this.getX() + this.getWidth();
        int top = this.getY();
        int bottom = this.getY() + this.getHeight();
        output.acceptScrollingWithDefaultCenter(message, left, right, top, bottom);
    }
}