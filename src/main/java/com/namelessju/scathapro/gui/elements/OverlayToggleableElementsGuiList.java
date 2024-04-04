package com.namelessju.scathapro.gui.elements;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.overlay.Overlay.ToggleableOverlayElement;

public class OverlayToggleableElementsGuiList extends ScathaProGuiList
{
    public OverlayToggleableElementsGuiList(ScathaPro scathaPro, GuiScreen gui)
    {
        // super(gui, 150, gui.height - 40, 25);
        super(gui, 25);
        
        for (ToggleableOverlayElement toggleableElement : scathaPro.getOverlay().toggleableOverlayElements)
        {
            this.listEntries.add(new OverlayToggleableElementEntry(toggleableElement));
        }
    }
    
    
    private class OverlayToggleableElementEntry extends ListEntry
    {
        private ToggleableOverlayElement toggleableElement;
        private GuiButton button;
        
        public OverlayToggleableElementEntry(ToggleableOverlayElement toggleableElement)
        {
            this.toggleableElement = toggleableElement;
            
            addLabel(toggleableElement.elementName, 0, 5, getListWidth() - 100, 10);
            addButton(button = new GuiButton(0, getListWidth() - 105, 0, 70, 20, "?"));
            updateButtonText();
        }
        
        private void updateButtonText()
        {
            button.displayString = toggleableElement.isVisible() ? "ON" : "OFF";
        }
        
        @Override
        protected void onButtonPressed(GuiButton button)
        {
            switch (button.id)
            {
                case 0:
                    toggleableElement.toggle();
                    updateButtonText();
                    break;
            }
        }
    }
    
}