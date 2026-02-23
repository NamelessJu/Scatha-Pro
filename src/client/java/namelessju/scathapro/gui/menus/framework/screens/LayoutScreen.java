package namelessju.scathapro.gui.menus.framework.screens;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.lists.ScathaProGuiList;
import namelessju.scathapro.gui.menus.framework.widgets.lists.TwoColumnGuiList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public abstract class LayoutScreen extends ScathaProScreen
{
    private HeaderAndFooterLayout layout = null;
    private @Nullable ScathaProGuiList list = null;
    
    public LayoutScreen(ScathaPro scathaPro, Component titleComponent, boolean addModTitle, Screen parentScreen)
    {
        super(scathaPro, titleComponent, addModTitle, parentScreen);
    }
    
    @Override
    protected void init()
    {
        layout = new HeaderAndFooterLayout(this);
        initLayout(layout);
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }
    
    protected @NonNull HeaderAndFooterLayout getLayout()
    {
        if (layout == null) throw new IllegalStateException("Cannot access layout before it is initialized");
        return layout;
    }
    
    protected abstract void initLayout(@NonNull HeaderAndFooterLayout layout);
    
    protected final void addTitleHeader()
    {
        layout.addToHeader(titleWidget, LayoutSettings::alignHorizontallyCenter);
    }
    
    protected final void addTitleHeader(LayoutElement... headerElements)
    {
        if (headerElements == null || headerElements.length == 0)
        {
            addTitleHeader();
            return;
        }
        
        LinearLayout headerLayout = LinearLayout.vertical().spacing(12);
        headerLayout.addChild(titleWidget, LayoutSettings::alignHorizontallyCenter);
        
        LinearLayout headerWidgetsLayout = LinearLayout.horizontal();
        if (headerElements.length > 1) headerWidgetsLayout.spacing(8);
        for (LayoutElement element : headerElements) headerWidgetsLayout.addChild(element);
        headerLayout.addChild(headerWidgetsLayout, LayoutSettings::alignHorizontallyCenter);
        
        headerLayout.arrangeElements();
        layout.setHeaderHeight(24 + headerLayout.getHeight());
        layout.addToHeader(headerLayout);
    }
    
    protected final TwoColumnGuiList addScrollList()
    {
        return addScrollList(new TwoColumnGuiList(minecraft, this, layout));
    }
    
    protected final <T extends ScathaProGuiList> T addScrollList(T list)
    {
        layout.addToContents(list);
        this.list = list;
        return list;
    }
    
    protected final void addDoneButtonFooter()
    {
        addFooter(doneButton());
    }
    
    protected final void addDoneButtonFooterWithWidget(LayoutElement footerElement)
    {
        LinearLayout footerLayout = LinearLayout.vertical().spacing(12);
        footerLayout.addChild(footerElement, LayoutSettings::alignHorizontallyCenter);
        footerLayout.addChild(doneButton(), LayoutSettings::alignHorizontallyCenter);
        addLayoutFooter(footerLayout);
    }
    
    protected final void addLayoutFooter(Layout layout)
    {
        layout.arrangeElements();
        addFooter(layout);
    }
    
    protected final void addFooter(LayoutElement layoutElement)
    {
        layout.setFooterHeight(13 + layoutElement.getHeight());
        layout.addToFooter(layoutElement);
    }
    
    @Override
    protected void repositionElements()
    {
        this.layout.arrangeElements();
        if (list != null) list.updateSize(width, layout);
    }
    
    
    protected static class GridBuilder
    {
        public final static int WIDTH = 308;
        
        public final int columns;
        private final GridLayout grid;
        private int currentIndex = 0;
        private int currentGap = -1;
        
        public GridBuilder()
        {
            this(2);
        }
        
        public GridBuilder(int columns)
        {
            this.columns = columns;
            grid = new GridLayout();
            grid.defaultCellSetting().paddingHorizontal(4).paddingBottom(4).alignHorizontallyCenter();
        }
        
        public GridLayout getGrid()
        {
            return grid;
        }
        
        public void addToContent(HeaderAndFooterLayout layout)
        {
            layout.addToContents(grid);
        }
        
        private LayoutSettings.LayoutSettingsImpl advanceIndex(int columnSpan, @Nullable LayoutSettings layoutSettings)
        {
            int cellYBefore = currentIndex / columns;
            
            currentIndex += columnSpan;
            
            LayoutSettings.LayoutSettingsImpl layout = (layoutSettings != null ? layoutSettings : grid.defaultCellSetting()).getExposed();
            if (currentGap > 0)
            {
                layout = layout.copy().paddingTop(currentGap - layout.paddingBottom);
            }
            
            if (currentGap >= 0 && (currentIndex / columns) > cellYBefore)
                currentGap = -1;
            
            return layout;
        }
        
        public <T extends LayoutElement> T addElement(T layoutElement, int columnSpan, @Nullable LayoutSettings layoutSettings)
        {
            int cellY = currentIndex / columns;
            int cellX = currentIndex % columns;
            if (cellX + columnSpan > columns)
            {
                cellY++;
                cellX = 0;
                currentIndex = Mth.roundToward(currentIndex, columns);
                currentGap = -1;
            }
            
            LayoutSettings.LayoutSettingsImpl layout = advanceIndex(columnSpan, layoutSettings);
            return grid.addChild(layoutElement, cellY, cellX, 1, columnSpan, layout);
        }
        
        public void addEmptyCell()
        {
            advanceIndex(1, null);
        }
        
        public <T extends AbstractWidget> T addSingleCell(T widget)
        {
            return addSingleCell(widget, true, true);
        }
        
        public <T extends AbstractWidget> T addSingleCell(T widget, boolean setWidth, boolean setHeight)
        {
            LayoutSettings.LayoutSettingsImpl layout = grid.defaultCellSetting().getExposed();
            if (setWidth) widget.setWidth((WIDTH - (columns - 1) * (layout.paddingRight + layout.paddingLeft)) / columns);
            if (setHeight) widget.setHeight(20);
            return addElement(widget, 1, null);
        }
        
        public <T extends AbstractWidget> T addFullWidth(T widget)
        {
            return addFullWidth(widget, true, true);
        }
        
        public <T extends AbstractWidget> T addFullWidth(T widget, boolean setWidth, boolean setHeight)
        {
            if (setWidth) widget.setWidth(WIDTH);
            if (setHeight) widget.setHeight(20);
            return addElement(widget, columns, null);
        }
        
        public void addGap()
        {
            addAbsoluteGap(getDefaultGapHeight());
        }
        
        public void addMultipliedGap(float multiplier)
        {
            addAbsoluteGap(Math.round(getDefaultGapHeight() * multiplier));
        }
        
        public void addAbsoluteGap(int height)
        {
            currentGap = height;
        }
        
        private int getDefaultGapHeight()
        {
            LayoutSettings.LayoutSettingsImpl defaultLayout = grid.defaultCellSetting().getExposed();
            return 20 + 2*(defaultLayout.paddingBottom + defaultLayout.paddingTop);
        }
    }
}
