package namelessju.scathapro.gui.lists;

import namelessju.scathapro.gui.elements.BooleanSettingButton;
import namelessju.scathapro.gui.elements.ScathaProButton;
import namelessju.scathapro.gui.menus.ScathaProGui;
import namelessju.scathapro.managers.Config;
import namelessju.scathapro.overlay.Overlay.ToggleableOverlayElement;

public class OverlayComponentsGuiList extends ScathaProGuiList
{
    private ToggleableOverlayElement hoveredElement = null;
    
    public OverlayComponentsGuiList(ScathaProGui gui)
    {
        super(gui, 63, gui.height - 40, 25);
        
        this.listEntries.add(new OverlayBackgroundEntry());
        
        for (ToggleableOverlayElement toggleableElement : gui.scathaPro.getOverlay().toggleableOverlayElements)
        {
            this.listEntries.add(new OverlayToggleableElementEntry(toggleableElement));
        }
    }
    
    public ToggleableOverlayElement getHoveredElement()
    {
        return hoveredElement;
    }
    
    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks)
    {
        hoveredElement = null;
        
        super.drawScreen(mouseXIn, mouseYIn, partialTicks);

        if (!this.isMouseYWithinSlotBounds(mouseY))
        {
            hoveredElement = null;
        }
    }
    
    
    private class OverlayBackgroundEntry extends ListEntry
    {
        public OverlayBackgroundEntry()
        {
            addElement(new BooleanSettingButton(0, 0, 2, getListWidth(), 20, "Darkened Background", Config.Key.overlayBackgroundEnabled));
        }
    }
    
    private class OverlayToggleableElementEntry extends ListEntry
    {
        private ToggleableOverlayElement toggleableElement;
        private ScathaProButton button;
        
        public OverlayToggleableElementEntry(ToggleableOverlayElement toggleableElement)
        {
            this.toggleableElement = toggleableElement;
            
            addElement(button = new ScathaProButton(0, 0, 2, getListWidth(), 20, ""));
            updateButtonText();
        }
        
        private void updateButtonText()
        {
            button.displayString = toggleableElement.elementName + ": " + (toggleableElement.isVisible() ? "ON" : "OFF");
        }
        
        @Override
        protected void onButtonPressed(ScathaProButton button)
        {
            switch (button.id)
            {
                case 0:
                    toggleableElement.toggle();
                    updateButtonText();
                    break;
            }
        }
        
        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
            
            if (isEntryWithinSlotBounds(y, slotHeight) && button.isHovered(mouseX, mouseY))
            {
                hoveredElement = toggleableElement;
            }
        }
    }
    
}