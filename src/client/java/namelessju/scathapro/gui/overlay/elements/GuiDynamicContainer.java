package namelessju.scathapro.gui.overlay.elements;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.jspecify.annotations.Nullable;

public class GuiDynamicContainer extends GuiContainer
{
    public enum Direction
    {
        VERTICAL, HORIZONTAL;
    }
    
    public Direction direction;
    protected Alignment contentAlignment = Alignment.LEFT;
    
    public GuiDynamicContainer(int x, int y, float scale, Direction direction)
    {
        super(x, y, scale);
        if (direction == null) throw new IllegalArgumentException("GuiDynamicContainer direction cannot be null!");
        this.direction = direction;
    }
    
    public void setContentAlignment(Alignment alignment)
    {
        this.contentAlignment = alignment != null ? alignment : Alignment.LEFT;
    }
    
    public Alignment getContentAlignment()
    {
        return this.contentAlignment;
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        int contentWidth = getContentWidth();
        
        if (backgroundColor != null) guiGraphics.fill(0, 0, getWidth(contentWidth), getHeight(), backgroundColor);
        
        boolean firstVisible = true;
        int previousChildMargin = 0;
        
        guiGraphics.nextStratum();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(padding, padding);
        
        for (GuiElement child : children)
        {
            if (!child.isVisible() || isElementEmptyContainer(child)) continue;
            
            int directionOffset = 0;
            
            guiGraphics.pose().pushMatrix();
            switch (direction)
            {
                case VERTICAL:
                    directionOffset = firstVisible ? 0 : Math.max(child.getY(), previousChildMargin);
                    switch (contentAlignment)
                    {
                        case CENTER:
                            guiGraphics.pose().translate((float) contentWidth / 2, directionOffset);
                            break;
                        case RIGHT:
                            guiGraphics.pose().translate(contentWidth - child.getX(), directionOffset);
                            break;
                        default:
                            guiGraphics.pose().translate(child.getX(), directionOffset);
                            break;
                    }
                    
                    child.forceRender(guiGraphics, deltaTracker, false, true, contentAlignment);
                    
                    previousChildMargin = child.marginBottom;
                    break;
                
                case HORIZONTAL:
                    directionOffset = firstVisible ? 0 : Math.max(child.getX(), previousChildMargin);
                    guiGraphics.pose().translate(directionOffset, child.getY());
                    
                    child.forceRender(guiGraphics, deltaTracker, false, true, Alignment.LEFT);
                    
                    previousChildMargin = child.marginRight;
                    break;
            }
            guiGraphics.pose().popMatrix();
            
            firstVisible = false;
            
            switch (direction)
            {
                case VERTICAL:
                    guiGraphics.pose().translate(0, directionOffset + child.getScaledHeight());
                    break;
                
                case HORIZONTAL:
                    guiGraphics.pose().translate(directionOffset + child.getScaledWidth(), 0);
                    break;
            }
        }
        
        guiGraphics.pose().popMatrix();
    }
    
    @Override
    public int getWidth()
    {
        return getWidth(getContentWidth());
    }

    private int getWidth(int contentWidth)
    {
        return contentWidth + padding * 2;
    }
    
    public int getContentWidth()
    {
        int width = 0;
        
        boolean firstVisible = true;
        int previousChildMargin = 0;
        
        for (GuiElement child : children)
        {
            if (!child.expandsContainerSize || !child.isVisible() || isElementEmptyContainer(child)) continue;
            
            int currentWidth;
            switch (direction)
            {
                case VERTICAL:
                    currentWidth = child.getX() + child.getScaledWidth() + child.marginRight;
                    if (currentWidth > width) width = currentWidth;
                    break;
                    
                case HORIZONTAL:
                    int directionOffset = firstVisible ? 0 : Math.max(child.getX(), previousChildMargin);
                    currentWidth = directionOffset + child.getScaledWidth();
                    
                    width += currentWidth;
                    
                    firstVisible = false;
                    previousChildMargin = child.marginRight;
                    break;
            }
        }
        
        return width;
    }
    
    @Override
    public int getHeight()
    {
        return getHeight(getContentHeight());
    }

    public int getHeight(int contentHeight)
    {
        return contentHeight + padding * 2;
    }
    
    public int getContentHeight()
    {
        int height = 0;
        
        boolean firstVisible = true;
        int previousChildMargin = 0;
        
        for (GuiElement child : children)
        {
            if (!child.expandsContainerSize || !child.isVisible() || isElementEmptyContainer(child)) continue;
            
            int currentHeight;
            switch (direction)
            {
                case VERTICAL:
                    int directionOffset = firstVisible ? 0 : Math.max(child.getY(), previousChildMargin);
                    currentHeight = directionOffset + child.getScaledHeight();
                    
                    height += currentHeight;
                    
                    firstVisible = false;
                    previousChildMargin = child.marginBottom;
                    break;
                    
                case HORIZONTAL:
                    currentHeight = child.getY() + child.getScaledHeight() + child.marginBottom;
                    if (currentHeight > height) height = currentHeight;
                    break;
            }
        }
        
        return height;
    }
    
    public void setResponsivePosition(Window window, @Nullable Float screenXPercentage, @Nullable Float screenYPercentage,
                                      int defaultX, int defaultY, GuiElement.@Nullable Alignment contentAlignment)
    {
        int positionX, positionY;
        
        if (screenXPercentage != null && 0f <= screenXPercentage && screenXPercentage <= 1f)
        {
            positionX = Math.round((window.getGuiScaledWidth() - getScaledWidth()) * screenXPercentage);
        }
        else positionX = defaultX;
        
        if (screenYPercentage != null && 0f <= screenYPercentage && screenYPercentage <= 1f)
        {
            positionY = Math.round((window.getGuiScaledHeight() - getScaledHeight()) * screenYPercentage);
        }
        else positionY = defaultY;
        
        setPosition(positionX, positionY);
        
        if (contentAlignment != null)
        {
            setContentAlignment(contentAlignment);
        }
        else
        {
            setContentAlignment(GuiElement.Alignment.LEFT);
            if (screenXPercentage != null)
            {
                if (0.495f <= screenXPercentage && screenXPercentage < 0.505f) setContentAlignment(GuiElement.Alignment.CENTER);
                else if (screenXPercentage > 0.5f) setContentAlignment(GuiElement.Alignment.RIGHT);
            }
        }
    }
}
