package namelessju.scathapro.gui.overlay.elements;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

public class OverlayContainer extends OverlayElement
{
    protected List<OverlayElement> children = new ArrayList<>();
    public Integer backgroundColor = null;
    public int padding = 0;
    
    public OverlayContainer(int x, int y, float scale)
    {
        super(x, y, scale);
    }
    
    @Override
    protected void extractContent(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        if (backgroundColor != null) guiGraphics.fill(0, 0, getWidth(), getHeight(), backgroundColor);
        
        guiGraphics.nextStratum();
        guiGraphics.pose().pushMatrix();
        if (padding != 0) guiGraphics.pose().translate(padding, padding);
        
        for (OverlayElement element : children)
        {
            element.extractRenderStateIfVisible(guiGraphics, deltaTracker);
        }
        
        guiGraphics.pose().popMatrix();
    }
    
    public void add(OverlayElement element)
    {
        children.add(element);
    }
    
    public void clearChildren()
    {
        children.clear();
    }
    
    public List<OverlayElement> getChildren()
    {
        return children;
    }
    
    @Override
    public int getWidth()
    {
        int width = 0;
        
        for (OverlayElement child : children)
        {
            if (!child.expandsContainerSize || !child.isVisible() || isElementEmptyContainer(child)) continue;
            
            int elementRequiredWidth = child.getX() + child.getScaledWidth() + child.marginRight;
            if (elementRequiredWidth > width) width = elementRequiredWidth;
        }
        
        return width + padding * 2;
    }
    
    @Override
    public int getHeight()
    {
        int height = 0;
        
        for (OverlayElement child : children)
        {
            if (!child.expandsContainerSize || !child.isVisible() || isElementEmptyContainer(child)) continue;
            
            int elementRequiredHeight = child.getY() + child.getScaledHeight() + child.marginBottom;
            if (elementRequiredHeight > height) height = elementRequiredHeight;
        }
        
        return height + padding * 2;
    }
    
    
    public static boolean isElementEmptyContainer(OverlayElement element)
    {
        if (!(element instanceof OverlayContainer container)) return false;
        if (container.children.isEmpty()) return true;
        
        for (OverlayElement child : container.children)
        {
            if (child.isVisible() && !(isElementEmptyContainer(child))) return false;
        }
        
        return true;
    }
}
