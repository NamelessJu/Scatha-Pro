package namelessju.scathapro.gui.elements;

public class HoverArea extends ScathaProButton
{
    public HoverArea(int id, int x, int y, int width, int height, String text)
    {
        super(id, x, y, width, height, "");
        setText(text);
    }
    
    public void setText(String text)
    {
        super.displayString = "[Hover] " + text;
    }
    
    @Override
    public boolean elementMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        return false;
    }
}
