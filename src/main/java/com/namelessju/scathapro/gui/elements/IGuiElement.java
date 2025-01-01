package com.namelessju.scathapro.gui.elements;

public interface IGuiElement
{
    public void setElementX(int x);
    public void setElementY(int y);
    public void setElementWidth(int width);
    public void setElementHeight(int height);
    
    public int getElementX();
    public int getElementY();
    public int getElementWidth();
    public int getElementHeight();
    
    public default void elementTick() {}
    public default boolean elementMouseClicked(int mouseX, int mouseY, int mouseButton) { return false; }
    public default void elementMouseReleased(int mouseX, int mouseY) {}
    public default void elementKeyTyped(char character, int code) {}
    public void elementDraw(int mouseX, int mouseY);
    
    public default boolean isHovered(int mouseX, int mouseY)
    {
        return mouseX >= getElementX() && mouseY >= getElementY()
            && mouseX < getElementX() + getElementWidth() && mouseY < getElementY() + getElementHeight();
    }
}
