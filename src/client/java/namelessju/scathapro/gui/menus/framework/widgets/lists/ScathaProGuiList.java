package namelessju.scathapro.gui.menus.framework.widgets.lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ScathaProGuiList extends ContainerObjectSelectionList<ScathaProGuiList.Entry>
{
    public static final int DEFAULT_ENTRY_CONTENT_HEIGHT = 20;
    public static final int DEFAULT_ENTRY_PADDING_BOTTOM = 5;
    
    public ScathaProGuiList(Minecraft minecraft, Screen screen, HeaderAndFooterLayout layout)
    {
        this(minecraft, screen, layout.getHeaderHeight(), layout.getContentHeight(), DEFAULT_ENTRY_CONTENT_HEIGHT + DEFAULT_ENTRY_PADDING_BOTTOM);
    }
    
    public ScathaProGuiList(Minecraft minecraft, Screen screen, HeaderAndFooterLayout layout, int defaultEntryHeight)
    {
        this(minecraft, screen, layout.getHeaderHeight(), layout.getContentHeight(), defaultEntryHeight);
    }
    
    public ScathaProGuiList(Minecraft minecraft, Screen screen, int contentY, int contentHeight, int defaultEntryHeight)
    {
        super(minecraft, screen.width, contentHeight, contentY, defaultEntryHeight);
        this.centerListVertically = false;
    }
    
    @Override
    public int getRowWidth()
    {
        return 310;
    }
    
    @Override
    public int addEntry(Entry entry)
    {
        return super.addEntry(entry);
    }
    
    @Override
    public int addEntry(Entry entry, int height)
    {
        return super.addEntry(entry, height);
    }
    
    public static class Entry extends ContainerObjectSelectionList.Entry<Entry>
    {
        private final List<PositionedChild> positionedChildren = Lists.newArrayList();
        private final List<AbstractWidget> widgets = Lists.newArrayList();
        
        public Entry() {}
        
        public Entry(@NonNull AbstractWidget... widgets)
        {
            for (AbstractWidget widget : widgets)
            {
                addChild(widget);
            }
        }
        
        public Entry(@NonNull PositionedChild... children)
        {
            for (PositionedChild child : children)
            {
                addPositionedChild(child);
            }
        }
        
        public void addChild(AbstractWidget widget)
        {
            addPositionedChild(new PositionedChild(widget, widget.getX(), widget.getY(), false));
        }
        
        public void addCenteredChild(AbstractWidget widget, int x)
        {
            addPositionedChild(new PositionedChild(widget, x, widget.getY(), true));
        }
        
        public void addPositionedChild(int x, int y, @NonNull AbstractWidget widget)
        {
            addPositionedChild(new PositionedChild(widget, x, y, false));
        }
        
        public void addPositionedChild(PositionedChild child)
        {
            positionedChildren.add(child);
            widgets.add(child.widget);
        }
        
        @Override
        public void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean bl, float partialTicks)
        {
            for (PositionedChild child : positionedChildren)
            {
                child.widget.setPosition(
                    getX() + child.x + (child.isCentered ? -child.widget.getWidth()/2 : 0),
                    getContentY() + child.y
                );
                child.widget.render(guiGraphics, mouseX, mouseY, partialTicks);
            }
        }
        
        @Override
        public @NonNull List<? extends GuiEventListener> children()
        {
            return widgets;
        }
        
        @Override
        public @NonNull List<? extends NarratableEntry> narratables()
        {
            return widgets;
        }
        
        public record PositionedChild(@NonNull AbstractWidget widget, int x, int y, boolean isCentered) {}
    }
}
