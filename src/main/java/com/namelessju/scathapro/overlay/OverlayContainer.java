package com.namelessju.scathapro.overlay;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;

public class OverlayContainer extends OverlayElement {
    
    protected List<OverlayElement> elements = new ArrayList<OverlayElement>();
    protected Alignment contentAlignment = null;
    public int backgroundColor = -1;
    public int padding = 0;

    public OverlayContainer(int x, int y, float scale) {
        super(x, y, scale);
    }
    
    @Override
    protected void drawSpecific() {
        if (backgroundColor >= 0) Gui.drawRect(0, 0, getWidth(false), getHeight(false), backgroundColor);

        for (OverlayElement element : elements) {
            element.draw(this);
        }
    }
    
    public void add(OverlayElement element) {
        elements.add(element);
    }
    
    public void setContentAlignment(Alignment alignment) {
        this.contentAlignment = alignment;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public int getWidth(boolean scaled) {
        int width = 0;
        
        for (OverlayElement element : elements) {
            if (!element.isVisible()) continue;
            
            int elementWidth = element.getWidth();
            if (element.getAlignment() != null) {
                switch (element.getAlignment()) {
                    case CENTER:
                        elementWidth = element.getWidth() / 2;
                        break;
                    case RIGHT:
                        elementWidth = 0;
                        break;
                }
            }
            int elementRequiredWidth = element.getX() + elementWidth;
            if (elementRequiredWidth > width) width = elementRequiredWidth;
        }
        
        return (int) Math.round((width + padding * 2) * (scaled ? scale : 1));
    }

    @Override
    public int getHeight(boolean scaled) {
        int height = 0;
        
        for (OverlayElement element : elements) {
            if (!element.isVisible()) continue;
            int elementRequiredHeight = element.getY() + element.getHeight();
            if (elementRequiredHeight > height) height = elementRequiredHeight;
        }
        
        return (int) Math.round((height + padding * 2) * (scaled ? scale : 1));
    }
    
    public Alignment getContentAlignment() {
        return contentAlignment;
    }

}
