package com.namelessju.scathapro.overlay;

import net.minecraft.client.renderer.GlStateManager;

public abstract class OverlayElement {

    protected int x, y;
    protected int translationX, translationY;
    protected float scale;
    protected Alignment alignment = null;
    protected boolean visible = true;
    
    public enum Alignment {
        LEFT, CENTER, RIGHT;
    }
    
    public OverlayElement(int x, int y, float scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;
    }
    
    public void draw() {
        draw(null);
    }
    
    @SuppressWarnings("incomplete-switch")
    public void draw(OverlayContainer container) {
        if (!visible) return;
        
        Alignment renderAlignment = alignment;
        
        int positionTranslationX = x + translationX;
        int positionTranslationY = y + translationY;
        
        if (container != null) {
            Alignment containerAlignment = container.getContentAlignment();
            
            if (renderAlignment == null) renderAlignment = containerAlignment;
            
            if (containerAlignment != null) {
                switch (containerAlignment) {
                    case LEFT:
                        positionTranslationX = container.padding + x + translationX;
                        break;
                    case CENTER:
                        positionTranslationX = container.getWidth(false) / 2;
                        break;
                    case RIGHT:
                        positionTranslationX = container.getWidth(false) - container.padding - (x + translationX);
                        break;
                }
            }
            
            positionTranslationY = container.padding + y + translationY;
        }
        
        int alignmentTranslation = 0;
        
        if (renderAlignment != null) {
            switch (renderAlignment) {
                case CENTER:
                    alignmentTranslation = - (int) Math.ceil(getWidth() / 2f);
                    break;
                case RIGHT:
                    alignmentTranslation = - getWidth();
                    break;
            }
        }

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(positionTranslationX + alignmentTranslation, positionTranslationY, 0);
        GlStateManager.scale(scale, scale, scale);
        drawSpecific();
        GlStateManager.popMatrix();
        
        // Reset color to white, because Minecraft's rendering sucks and might take the last color used to render a string to render some image afterwards...
        GlStateManager.color(1f, 1f, 1f);
    }
    
    protected abstract void drawSpecific();
    
    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setTranslation(int x, int y) {
        this.translationX = x;
        this.translationY = y;
    }
    
    public void setScale(float scale) {
        this.scale = scale;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return getWidth(true);
    }
    
    public int getHeight() {
        return getHeight(true);
    }
    
    public abstract int getWidth(boolean scaled);
    public abstract int getHeight(boolean scaled);

    public Alignment getAlignment() {
        return alignment;
    }

    public boolean isVisible() {
        return visible;
    }
}
