package namelessju.scathapro.gui.menus.framework.widgets.lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class TwoColumnGuiList extends ScathaProGuiList
{
    private final static int GAP = 10;
    
    private final Screen screen;
    private Entry currentSingleColumnEntry = null;
    
    public TwoColumnGuiList(Minecraft minecraft, Screen screen, HeaderAndFooterLayout layout)
    {
        super(minecraft, screen, layout);
        this.screen = screen;
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public <T extends AbstractWidget> T addSingleColumn(T widget)
    {
        widget.setSize((getRowWidth() - GAP) / 2, DEFAULT_ENTRY_CONTENT_HEIGHT);
        
        if (currentSingleColumnEntry == null)
        {
            widget.setX(0);
            currentSingleColumnEntry = new Entry(widget);
            addEntry(currentSingleColumnEntry);
        }
        else
        {
            widget.setX(getRowWidth()/2 + GAP/2);
            currentSingleColumnEntry.addChild(widget);
            currentSingleColumnEntry = null;
        }
        return widget;
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public <T extends AbstractWidget> T addDoubleColumn(T widget)
    {
        addEntry(new Entry(widget));
        widget.setRectangle(getRowWidth(), DEFAULT_ENTRY_CONTENT_HEIGHT, 0, 0);
        return widget;
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public StringWidget addTitle(Component component)
    {
        return addCentered(new StringWidget(
            0, DEFAULT_ENTRY_CONTENT_HEIGHT /2 - screen.getFont().lineHeight/2,
            screen.getFont().width(component.getVisualOrderText()), screen.getFont().lineHeight,
            component, screen.getFont()
        ));
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public <T extends AbstractWidget> T addCentered(T widget)
    {
        addEntry(new Entry(new Entry.PositionedChild(widget, getRowWidth() / 2, widget.getY(), true)));
        return widget;
    }
    
    @Override
    public int addEntry(Entry entry, int i)
    {
        if (entry != currentSingleColumnEntry) currentSingleColumnEntry = null;
        return super.addEntry(entry, i);
    }
}
